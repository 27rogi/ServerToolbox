package space.ryzhenkov.servertoolbox.commands.chat

import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.core.text.broadcastText
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class ClearChatCommand(
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
                    clearPlayerChat(source, arg.invoke(this).getPlayer(this.source))
                }
            }
            runs {
                clearChat(source)
            }
        }
    }

    private fun cleanupMessage(): MutableText {
        return Text.literal("\n".repeat(100))
    }

    private fun clearPlayerChat(source: ServerCommandSource, player: ServerPlayerEntity) {
        player.sendMessage(
            CommandResultMessage(ResultTypes.OTHER, this, "player")
                .addArgs(cleanupMessage(), Text.literal(source.name))
                .getMessage()
        )
    }

    private fun clearChat(source: ServerCommandSource) {
        source.server.broadcastText(
            CommandResultMessage(ResultTypes.OTHER, this)
                .addArgs(cleanupMessage())
                .getMessage()
        )
    }
}