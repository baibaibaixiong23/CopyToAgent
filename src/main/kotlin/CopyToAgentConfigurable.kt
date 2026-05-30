package ztf.extend

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.DefaultComboBoxModel

class CopyToAgentConfigurable : BoundConfigurable("复制到 AI 助手") {

    private val formatModel = DefaultComboBoxModel(arrayOf("claude", "opencode"))
    private val pathTypeModel = DefaultComboBoxModel(arrayOf("relative", "absolute"))

    override fun createPanel() = panel {
        val settings = CopyToAgentSettings.instance.state

        row(MyMessageBundle.message("settings.format")) {
            comboBox(formatModel)
                .bindItem({ settings.format }) { settings.format = it ?: "claude" }
        }
        row(MyMessageBundle.message("settings.pathType")) {
            comboBox(pathTypeModel)
                .bindItem({ settings.pathType }) { settings.pathType = it ?: "relative" }
        }
        row {
            checkBox(MyMessageBundle.message("settings.showNotification"))
                .bindSelected({ settings.showNotification }, { settings.showNotification = it })
        }
    }
}
