package space.ryzhenkov.servertoolbox.events

import eu.pb4.placeholders.api.PlaceholderContext
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn
import net.minecraft.text.Text
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.entity.changePos
import net.silkmc.silk.core.event.PlayerEvents
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.config.GeneralConfig
import space.ryzhenkov.servertoolbox.utils.Registrable
import space.ryzhenkov.servertoolbox.utils.ResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

object PlayerEvents : Registrable("player events") {
    override fun onRegister(): Boolean {
        if (GeneralConfig.Modules.spawn) {
            if (GeneralConfig.Spawn.respawnOnSpawn) respawnOnSpawn()
        }
        if (GeneralConfig.Modules.motd) showMotdOnLogin()
        return true
    }

    @OptIn(ExperimentalSilkApi::class)
    private fun showMotdOnLogin() {
        PlayerEvents.postLogin.listen { event ->
            if (!Permissions.check(
                    event.player,
                    "server-toolbox.show.motd",
                    PermissionLevel.COMMAND_RIGHTS.level
                )
            ) return@listen
            event.player.sendMessage(
                Configs.MESSAGES
                    .getFormattedMessage(
                        Configs.MESSAGES.motd,
                        PlaceholderContext.of(event.player.gameProfile, event.player.server)
                    )
            )
        }
    }

    private fun respawnOnSpawn() {
        ServerPlayerEvents.AFTER_RESPAWN.register(AfterRespawn { oldPlayer, player, _ ->
            if (!Permissions.check(
                    player,
                    "server-toolbox.spawn.respawn",
                    PermissionLevel.NONE.level
                )
            ) return@AfterRespawn
            if (!GeneralConfig.Spawn.ignoreSpawnPoint && player.spawnPointPosition != null) {
                return@AfterRespawn
            }
            val spawnLocationKey = GeneralConfig.Spawn.getSpawnLocationKey(oldPlayer)
            val spawnLocation = GeneralConfig.Spawn.spawnLocations[spawnLocationKey]
                ?: return@AfterRespawn
            player.changePos(
                spawnLocation.location[0],
                spawnLocation.location[1],
                spawnLocation.location[2],
                player.server.worlds.first { it.registryKey.value.toString() == spawnLocationKey },
                spawnLocation.head,
                0F
            )
            player.sendMessage(
                ResultMessage(ResultTypes.SUCCESS, "respawn")
                    .addArgs(
                        Text.literal(spawnLocationKey),
                        Text.literal("X: ${spawnLocation.location[0]}, Y: ${spawnLocation.location[1]}, Z: ${spawnLocation.location[2]}")
                    )
                    .getMessage()
            )
        })
    }
}