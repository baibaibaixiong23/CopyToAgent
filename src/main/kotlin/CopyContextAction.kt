/*
 * Copyright 2026 ztf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ztf.extend
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.function.Consumer

class CopyContextAction(
    private val clipboardWriter: Consumer<String> = Consumer { content ->
        com.intellij.openapi.ide.CopyPasteManager.getInstance()
            .setContents(java.awt.datatransfer.StringSelection(content))
    }
) : AnAction(), DumbAware {

    companion object {
        private val LOG = Logger.getInstance(CopyContextAction::class.java)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val files = getFilesFromContext(e)
        e.presentation.isEnabledAndVisible = !files.isNullOrEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = getFilesFromContext(e) ?: return
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext)
            ?: FileEditorManager.getInstance(project).selectedTextEditor

        try {
            ReadAction
                .nonBlocking<ContextLinkBuilder.Result> {
                    if (editor != null && files.size == 1 && !files[0].isDirectory) {
                        buildReference(editor, files[0])
                    } else {
                        buildMultiFileReferences(project, files)
                    }
                }
                .finishOnUiThread(ModalityState.defaultModalityState()) { result ->
                    handleResult(project, result)
                }
                .submit(AppExecutorUtil.getAppExecutorService())
        } catch (ex: RuntimeException) {
            LOG.error("Failed to copy context link", ex)
            notifyWarning(MyMessageBundle.message("notification.copy.failed"))
        }
    }

    private fun getFilesFromContext(e: AnActionEvent): Array<VirtualFile>? {
        val files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)
        if (!files.isNullOrEmpty()) return files
        val file = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        if (file != null) return arrayOf(file)
        val project = e.project ?: return null
        val selectedFiles = FileEditorManager.getInstance(project).selectedFiles
        return if (selectedFiles.isNotEmpty()) selectedFiles else null
    }

    private fun buildMultiFileReferences(
        project: com.intellij.openapi.project.Project,
        files: Array<VirtualFile>
    ): ContextLinkBuilder.Result {
        val settings = CopyToAgentSettings.instance.state
        val references = files.joinToString("\n") { file ->
            val path = resolveFilePath(project, file, settings.pathType)
            val suffix = if (file.isDirectory && !path.endsWith("/")) "/" else ""
            "@$path$suffix"
        }
        return ContextLinkBuilder.Result.Success(references)
    }

    private fun buildReference(
        editor: com.intellij.openapi.editor.Editor,
        file: com.intellij.openapi.vfs.VirtualFile
    ): ContextLinkBuilder.Result {
        val project = editor.project
        if (project == null) return ContextLinkBuilder.Result.Failure("notification.cannotGetFilePath")
        val settings = CopyToAgentSettings.instance.state
        val filePath = resolveFilePath(project, file, settings.pathType)

        val selectionModel = editor.selectionModel
        val startLine: Int
        val endLine: Int
        if (selectionModel.hasSelection()) {
            startLine = editor.document.getLineNumber(selectionModel.selectionStart) + 1
            var rawEndLine = editor.document.getLineNumber(selectionModel.selectionEnd) + 1
            if (rawEndLine > startLine && editor.offsetToLogicalPosition(selectionModel.selectionEnd).column == 0) {
                rawEndLine--
            }
            endLine = rawEndLine
        } else {
            startLine = -1
            endLine = -1
        }

        return ContextLinkBuilder.buildContextLink(filePath, startLine, endLine, settings.format)
    }

    internal fun handleResult(project: com.intellij.openapi.project.Project, result: ContextLinkBuilder.Result) {
        when (result) {
            is ContextLinkBuilder.Result.Success -> {
                try {
                    clipboardWriter.accept(result.reference)
                    if (CopyToAgentSettings.instance.state.showNotification) {
                        val count = result.reference.lines().size
                        val msg = if (count > 1)
                            MyMessageBundle.message("notification.copied.multi", count)
                        else
                            MyMessageBundle.message("notification.copied", result.reference)
                        notifyInfo(msg)
                    }
                } catch (ex: RuntimeException) {
                    LOG.warn("Failed to write context link to clipboard", ex)
                    notifyWarning(MyMessageBundle.message("notification.copy.failed"))
                }
            }
            is ContextLinkBuilder.Result.Failure -> {
                notifyWarning(MyMessageBundle.message(result.messageKey))
            }
        }
    }

    private fun resolveFilePath(
        project: com.intellij.openapi.project.Project,
        file: com.intellij.openapi.vfs.VirtualFile,
        pathType: String
    ): String {
        if (pathType == "absolute") return file.path

        val contentRoots = ProjectRootManager.getInstance(project).contentRoots
        for (root in contentRoots) {
            val rootPath = root.path
            if (file.path.startsWith(rootPath)) {
                val relative = file.path.removePrefix(rootPath)
                return if (relative.startsWith("/")) relative.substring(1) else relative
            }
        }
        return file.path
    }

    private fun notifyInfo(message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CopyToAgent")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(null)
    }

    private fun notifyWarning(message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CopyToAgent")
            .createNotification(message, NotificationType.WARNING)
            .notify(null)
    }
}
