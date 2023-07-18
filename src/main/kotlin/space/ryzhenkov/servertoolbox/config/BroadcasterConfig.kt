package space.ryzhenkov.servertoolbox.config

import me.lortseam.completeconfig.api.ConfigEntry
import me.lortseam.completeconfig.data.Config
import me.lortseam.completeconfig.data.ConfigOptions
import space.ryzhenkov.servertoolbox.config.objects.broadcaster.Broadcast

class BroadcasterConfig : Config(
    ConfigOptions.mod("server-toolbox").branch(arrayOf("broadcaster"))
        .fileHeader(
            "Simple broadcaster to satisfy your needs!\n" +
                    "Documentation: https://27rogi.gitbook.io/server-toolbox/configuration/broadcaster"
        )
), VersionableConfig {
    @ConfigEntry(comment = "List with all broadcasts.")
    var broadcasts: HashMap<String, Broadcast> = hashMapOf()

    @ConfigEntry(comment = "Do not touch, required correct config detection!")
    private var configVersion: Int = 1

    override fun hasActualConfigVersion(): Boolean {
        return configVersion == 1
    }
}