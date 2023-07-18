package space.ryzhenkov.servertoolbox.commands.player

import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class FeedCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "player", false)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, EntityArgumentType.player()) { arg ->
                requires { checkRequirements(it, "others", PermissionLevel.BAN_RIGHTS.level) }
                runs {
                    saturatePlayer(arg.invoke(this).getPlayer(this.source))
                }
            }
            runs {
                saturatePlayer(source.playerOrThrow)
            }
        }
    }

    private fun saturatePlayer(player: ServerPlayerEntity) {
        val playerHunger = player.hungerManager
        playerHunger.foodLevel = 20
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(player.displayName.copy())
                .getMessage()
        )
    }
}