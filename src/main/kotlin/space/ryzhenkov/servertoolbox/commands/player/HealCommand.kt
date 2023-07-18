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

class HealCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "player", false)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, EntityArgumentType.player()) { playerArg ->
                requires { checkRequirements(it, "others", PermissionLevel.BAN_RIGHTS.level) }
                runs {
                    healPlayer(playerArg.invoke(this).getPlayer(this.source))
                }
            }
            runs {
                healPlayer(source.playerOrThrow)
            }
        }
    }

    private fun healPlayer(player: ServerPlayerEntity) {
        player.health = player.maxHealth
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(player.displayName.copy())
                .getMessage()
        )
    }
}