package csense.kotlin.annotations.idea.startup;

import com.intellij.openapi.components.*
import csense.kotlin.logger.*


interface StartupService {
    companion object {
        val instance: StartupService?
            get() = ServiceManager.getService(StartupService::class.java)
    }
}


class StartupServiceImpl : StartupService {
    init {
        L.usePrintAsLoggers()
    }
}
