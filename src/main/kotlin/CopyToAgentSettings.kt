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
