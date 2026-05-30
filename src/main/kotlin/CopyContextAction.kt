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

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext)
            ?: run {
                notifyWarning(MyMessageBundle.message("notification.no.active.editor"))
                return
            }

        val file = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext) ?: return

        try {
            ReadAction
                .nonBlocking<ContextLinkBuilder.Result> { buildReference(editor, file) }
                .finishOnUiThread(ModalityState.defaultModalityState()) { result ->
                    handleResult(project, result)
                }
                .submit(AppExecutorUtil.getAppExecutorService())
        } catch (ex: RuntimeException) {
            LOG.error("Failed to copy context link", ex)
            notifyWarning(MyMessageBundle.message("notification.copy.failed"))
        }
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
                        notifyInfo(MyMessageBundle.message("notification.copied", result.reference))
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
