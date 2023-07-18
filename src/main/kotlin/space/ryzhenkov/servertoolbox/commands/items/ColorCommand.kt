package space.ryzhenkov.servertoolbox.commands.items

import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.item.DyeableItem
import net.minecraft.nbt.NbtInt
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes
import java.awt.Color

class ColorCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "color", true)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, StringArgumentType.greedyString()) {
                runs {
                    setColor(source.playerOrThrow, StringArgumentType.getString(this, arguments[0].key))
                }
            }
        }
    }

    private fun setColor(player: ServerPlayerEntity, color: String) {
        if (!(color.startsWith("#") || color.length == 3 || color.length == 4 || color.length == 6 || color.length == 8)) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this)
                    .getMessage()
            )
        }

        if (player.mainHandStack.isEmpty) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "empty")
                    .getMessage()
            )
        }

        if (player.mainHandStack.item !is DyeableItem) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "not_dyeable")
                    .getMessage()
            )
        }

        val nbt = player.mainHandStack.getOrCreateSubNbt("display")
        val hexColor = getColorFromHex(color)
        nbt.put("color", NbtInt.of(hexColor))

        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .getMessage(null, Text.literal(color).styled { it.withColor(hexColor) })
        )
    }

    /*
     * Code below is borrowed from library https://github.com/silentsoft/csscolor4j/tree/main
     * Authored by https://github.com/silentsoft
     */

    private fun fill(hex: String): String {
        if (hex.length == 3 || hex.length == 4) {
            var value = ""
            for (letter in hex.toCharArray()) {
                value += java.lang.String.valueOf(charArrayOf(letter, letter))
            }
            return value
        }
        return hex
    }

    private fun getColorFromHex(hexColor: String): Int {
        val value = hexColor.trim { it <= ' ' }
        val hex = if (value.startsWith("#")) value.substring(1) else value
        val filledHex: String = fill(hex)
        return Color(
            filledHex.substring(0, 2).toInt(16),
            filledHex.substring(2, 4).toInt(16),
            filledHex.substring(4, 6).toInt(16)
        ).rgb
    }
}