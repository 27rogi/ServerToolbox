package space.ryzhenkov.servertoolbox.commands.player

import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class GameModeCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "mode", unique = true),
        CommandArgument(this, "player", false)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument("mode", IntegerArgumentType.integer(0, 3)) { modeArg ->
                suggestListWithTooltips {
                    arrayListOf(
                        Pair(0, Message { "SURVIVAL" }),
                        Pair(1, Message { "CREATIVE" }),
                        Pair(2, Message { "ADVENTURE" }),
                        Pair(3, Message { "SPECTATOR" }),
                    )
                }
                argument("player", EntityArgumentType.player()) { playerArg ->
                    requires { checkRequirements(it, "others", PermissionLevel.COMMAND_RIGHTS.level) }
                    runs {
                        changeMode(modeArg.invoke(this), playerArg.invoke(this).getPlayer(this.source), true)
                    }
                }
                runs {
                    changeMode(modeArg.invoke(this), this.source.playerOrThrow)
                }
            }
        }
    }


    private fun changeMode(mode: Int, player: ServerPlayerEntity, other: Boolean = false) {
        val gamemode = when (mode) {
            0 -> GameMode.SURVIVAL
            1 -> GameMode.CREATIVE
            2 -> GameMode.ADVENTURE
            3 -> GameMode.SPECTATOR
            else -> {
                GameMode.SURVIVAL
            }
        }

        val permission = if (other) "others.modes.${gamemode.name}" else "modes.${gamemode.name}"
        if (!checkRequirements(player.commandSource, permission, PermissionLevel.COMMAND_RIGHTS.level)) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this)
                    .addArgs(Text.literal(gamemode.name))
                    .getMessage()
            )
        }
        player.changeGameMode(gamemode)

        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(player.displayName.copy(), gamemode.translatableName.copy())
                .getMessage()
        )
    }
}