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

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.DefaultComboBoxModel

class CopyToAgentConfigurable : BoundConfigurable("CopyToAgent") {

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
