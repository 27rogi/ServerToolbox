package space.ryzhenkov.servertoolbox.events

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import space.ryzhenkov.servertoolbox.ServerToolbox
import space.ryzhenkov.servertoolbox.config.GeneralConfig
import space.ryzhenkov.servertoolbox.utils.Broadcaster
import space.ryzhenkov.servertoolbox.utils.Registrable

object ServerEvents : Registrable("server events") {
    override fun onRegister(): Boolean {
        if (GeneralConfig.Modules.broadcaster) registerBroadcasterEvents()
        return true
    }

    private fun registerBroadcasterEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            Broadcaster.registerTasks(server, true)
        })
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleEvents.ServerStopping {
            ServerToolbox.logger.info("Stopping ${Broadcaster.tasks.keys.size} running tasks...")
            Broadcaster.cancelTasks(true)
            Broadcaster.timer.cancel()
        })
    }
}