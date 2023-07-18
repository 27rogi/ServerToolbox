package space.ryzhenkov.servertoolbox.commands.world

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.config.GeneralConfig
import space.ryzhenkov.servertoolbox.config.objects.SpawnLocation
import space.ryzhenkov.servertoolbox.utils.ResultTypes
import kotlin.math.round

class SetSpawnCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "primary", false, unique = true, literals = arrayOf("primary")),
        CommandArgument(this, "ignorePrimary", false, unique = true)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            literal(arguments[0].key) {
                requires { checkRequirements(it, "primary", PermissionLevel.BAN_RIGHTS.level) }
                runs {
                    setSpawn(this, isPrimary = true)
                }
            }
            argument<Boolean>(arguments[1].key) {
                requires { checkRequirements(it, "ignore", PermissionLevel.BAN_RIGHTS.level) }
                runs {
                    setSpawn(this, this.getArgument(arguments[1].key, Boolean::class.java))
                }
            }
            runs {
                setSpawn(this, false)
            }
        }
    }

    private fun setSpawn(
        context: CommandContext<ServerCommandSource>,
        isIgnoring: Boolean? = null,
        isPrimary: Boolean? = null
    ) {
        if (!context.source.isExecutedByPlayer) return context.source.sendMessage(Text.literal("Must be used by player"))
        val player = context.source.player!!

        GeneralConfig.Spawn.spawnLocations[player.world.registryKey.value.toString()] = SpawnLocation().apply {
            location = arrayOf(player.x.round(2), player.y.round(2), player.z.round(2))
            head = player.headYaw.round(2)
            ignorePrimary = isIgnoring
            primary = if (GeneralConfig.Spawn.spawnLocations.isEmpty()) true else isPrimary
        }

        context.source.world.setSpawnPos(player.blockPos, player.headYaw)
        Configs.saveAndLoad()

        val messageKey = if (isIgnoring == true) "ignoring" else if (isPrimary == true) "primary" else "set"
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, messageKey)
                .addArgs(
                    Text.literal(player.world.registryKey.value.toString()),
                    Text.literal("X: ${player.blockPos.x}, Y: ${player.blockPos.y}, Z: ${player.blockPos.z}"),
                    if (isIgnoring == true) Text.literal(isIgnoring.toString()) else Text.literal(isPrimary.toString())
                )
                .getMessage()
        )
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (round(this * multiplier) / multiplier).toFloat()
    }
}