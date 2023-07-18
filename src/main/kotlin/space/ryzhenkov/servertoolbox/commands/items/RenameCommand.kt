package space.ryzhenkov.servertoolbox.commands.items

import com.mojang.brigadier.arguments.StringArgumentType
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.LiteralCommandBuilder
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class RenameCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "text", true)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, StringArgumentType.greedyString()) {
                runs {
                    setName(source.playerOrThrow, StringArgumentType.getString(this, arguments[0].key))
                }
            }
            runs {
                setName(source.playerOrThrow, null)
            }
        }
    }

    private fun setName(player: ServerPlayerEntity, name: String?) {
        if (player.mainHandStack.isEmpty) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this)
                    .getMessage()
            )
        }

        if (name.isNullOrEmpty()) {
            player.mainHandStack.removeCustomName()
            return player.sendMessage(
                CommandResultMessage(ResultTypes.SUCCESS, this, "removed")
                    .getMessage()
            )
        }

        val formattedName = TextParserUtils.formatText(name).copy().styled { it.withItalic(false) }
        player.mainHandStack.setCustomName(formattedName)
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(formattedName)
                .getMessage()
        )
    }
}