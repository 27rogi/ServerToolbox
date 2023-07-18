package space.ryzhenkov.servertoolbox.commands.chat

import eu.pb4.placeholders.api.PlaceholderContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.config.Configs

class RulesCommand(
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
                    showMessage(arg.invoke(this).getPlayer(this.source))
                }
            }
            runs {
                showMessage(source.playerOrThrow)
            }
        }
    }

    private fun showMessage(player: ServerPlayerEntity) {
        player.sendMessage(
            Configs.MESSAGES
                .getFormattedMessage(Configs.MESSAGES.rules, PlaceholderContext.of(player.gameProfile, player.server))
        )
    }
}