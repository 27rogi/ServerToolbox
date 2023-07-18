package space.ryzhenkov.servertoolbox.config

import me.lortseam.completeconfig.data.Config
import space.ryzhenkov.servertoolbox.ServerToolbox

object Configs {
    val GENERAL = GeneralConfig()
    val KITS = KitsConfig()
    val MESSAGES = MessagesConfig()
    val BROADCASTER = BroadcasterConfig()

    val registry = arrayListOf(
        GENERAL,
        KITS,
        MESSAGES,
        BROADCASTER
    )

    fun loadAll() {
        for (config in registry) {
            ServerToolbox.logger.info("Loading data from `${config.branch.joinToString("/")}.conf`")
            config.load()
            if (config is VersionableConfig) {
                if (!config.hasActualConfigVersion()) ServerToolbox.logger.error(
                    "Your config file `${
                        config.branch.joinToString(
                            "/"
                        )
                    }.conf` has an outdated version, it may cause problems! Be sure to check migration guide!"
                )
            }
        }
    }

    fun saveAndLoad(config: Config? = null) {
        if (config == null) {
            for (entry in registry) {
                entry.save()
                entry.load()
            }
            return
        }
        config.save()
        config.load()
    }
}