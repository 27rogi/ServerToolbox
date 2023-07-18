package space.ryzhenkov.servertoolbox

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.ryzhenkov.servertoolbox.commands.Commands
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.events.PlayerEvents
import space.ryzhenkov.servertoolbox.events.ServerEvents

object ServerToolbox : ModInitializer {
    val logger: Logger = LoggerFactory.getLogger("server-toolbox")

    override fun onInitialize() {
        println()
        logger.info(
            "Currently running Server Toolbox ${
                FabricLoader.getInstance().getModContainer("server-toolbox").get().metadata.version
            } on ${FabricLoader.getInstance().environmentType}"
        )
        Configs.loadAll()
        logger.info("Cleaned up ${Configs.KITS.cleanupTimeouts()} expired kit timeout entries")
        arrayOf(
            Commands,
            PlayerEvents,
            ServerEvents
        ).forEach {
            it.register()
        }
        println()
    }
}