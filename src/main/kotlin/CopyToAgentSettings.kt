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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "ztf.extend.CopyToAgentSettings",
    storages = [Storage("CopyToAgent.xml")]
)
class CopyToAgentSettings : PersistentStateComponent<CopyToAgentSettings.State> {

    data class State(
        var format: String = "claude",
        var pathType: String = "relative",
        var showNotification: Boolean = false
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        val instance: CopyToAgentSettings
            get() = ApplicationManager.getApplication().getService(CopyToAgentSettings::class.java)
    }
}
