package space.ryzhenkov.servertoolbox.commands.teleportation

import net.minecraft.command.argument.RegistryKeyArgumentType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.core.entity.changePos
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.config.GeneralConfig
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class SpawnCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "world", false)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, RegistryKeyArgumentType.registryKey(RegistryKeys.WORLD)) { worldArg ->
                requires { checkRequirements(it, "worlds", PermissionLevel.BAN_RIGHTS.level) }
                runs {
                    teleport(source.playerOrThrow, worldArg.invoke(this))
                }
            }
            runs {
                teleport(source.playerOrThrow, null)
            }
        }
    }

    private fun teleport(player: ServerPlayerEntity, argumentKey: RegistryKey<World>?) {
        val targetKey = argumentKey?.value?.toString() ?: GeneralConfig.Spawn.getSpawnLocationKey(player)
        val spawnLocation = GeneralConfig.Spawn.spawnLocations[targetKey]
        if (targetKey.isNullOrEmpty() || spawnLocation == null) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "not_found")
                    .addArgs(Text.literal(targetKey))
                    .getMessage()
            )
        }
        val worldKey = argumentKey ?: player.server.worldRegistryKeys.first { it.value.toString() == targetKey }
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "teleported")
                .addArgs(
                    Text.literal(worldKey.value.toString()),
                    Text.literal("X: ${spawnLocation.location[0]}, Y: ${spawnLocation.location[1]}, Z: ${spawnLocation.location[2]}")
                )
                .getMessage()
        )

        player.changePos(
            spawnLocation.location[0],
            spawnLocation.location[1],
            spawnLocation.location[2],
            player.server.getWorld(worldKey),
            spawnLocation.head,
            0F
        )
    }
}