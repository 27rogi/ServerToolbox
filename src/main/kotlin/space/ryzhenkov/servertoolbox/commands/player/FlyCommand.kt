package space.ryzhenkov.servertoolbox.commands.player

import com.mojang.brigadier.arguments.FloatArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class FlyCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "literal", false, literals = arrayOf("speed")),
        CommandArgument(this, "speed", false),
        CommandArgument(this, "player", false),
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[2].key, EntityArgumentType.player()) { playerArg ->
                requires { checkRequirements(it, "others.toggle", PermissionLevel.COMMAND_RIGHTS.level) }
                runs { toggleFlight(playerArg.invoke(this).getPlayer(source)) }
            }
            literal("speed") {
                requires { checkRequirements(it, "speed", PermissionLevel.COMMAND_RIGHTS.level) }
                argument(arguments[1].key, FloatArgumentType.floatArg(0F, 1F)) { floatArg ->
                    suggestSingle { 0.05F } // suggest default value for fly speed
                    argument(arguments[2].key, EntityArgumentType.player()) { playerArg ->
                        requires { checkRequirements(it, "others.speed", PermissionLevel.COMMAND_RIGHTS.level) }
                        runs { changeSpeed(playerArg.invoke(this).getPlayer(source), floatArg.invoke(this)) }
                    }
                    runs { changeSpeed(source.playerOrThrow, floatArg.invoke(this)) }
                }
            }
            runs {
                toggleFlight(source.playerOrThrow)
            }
        }
    }

    private fun changeSpeed(player: ServerPlayerEntity, flySpeed: Float = 0.05F) {
        player.abilities.flySpeed = flySpeed
        player.sendAbilitiesUpdate()
        player.sendMessage(CommandResultMessage(ResultTypes.SUCCESS, this, "speed")
                .addArgs(Text.literal(player.abilities.flySpeed.toString()))
                .getMessage())
    }

    private fun toggleFlight(player: ServerPlayerEntity) {
        player.abilities.flying = !player.abilities.flying
        player.abilities.allowFlying = !player.abilities.allowFlying

        if (!player.abilities.allowFlying) {
            player.sendAbilitiesUpdate()
            return player.sendMessage(CommandResultMessage(ResultTypes.WARNING, this, "disabled")
                    .addArgs(player.displayName.copy())
                    .getMessage())
        }

        player.sendAbilitiesUpdate()
        player.sendMessage(CommandResultMessage(ResultTypes.SUCCESS, this, "enabled")
                .addArgs(player.displayName.copy())
                .getMessage())
    }
}