package space.ryzhenkov.servertoolbox.commands.items

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.Text.Serializer
import net.silkmc.silk.commands.LiteralCommandBuilder
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class LoreCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "line", false),
        CommandArgument(this, "text", true)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, IntegerArgumentType.integer(1)) {
                requires { checkRequirements(it, "set") }
                suggestList {
                    if (it.source.player != null) suggestLoreLines(it.source.player!!.mainHandStack) else null
                }
                argument(arguments[1].key, StringArgumentType.greedyString()) {
                    runs {
                        setLore(
                            source.playerOrThrow,
                            StringArgumentType.getString(this, arguments[1].key),
                            IntegerArgumentType.getInteger(this, arguments[0].key)
                        )
                    }
                }
            }
            argument(arguments[1].key, StringArgumentType.greedyString()) {
                runs {
                    setLore(source.playerOrThrow, StringArgumentType.getString(this, arguments[1].key))
                }
            }
        }
    }

    private fun suggestLoreLines(item: ItemStack): Iterable<Int>? {
        val nbtLore = item.getSubNbt("display") ?: return null
        if (!nbtLore.contains("Lore")) return null
        val arr = arrayListOf<Int>()
        nbtLore.getList("Lore", NbtElement.STRING_TYPE.toInt()).forEachIndexed { index, _ ->
            arr.add(index + 1)
        }
        return arr.asIterable()
    }

    private fun setLore(player: ServerPlayerEntity, lore: String, line: Int = -1) {
        if (player.mainHandStack.isEmpty) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "empty")
                    .getMessage()
            )
        }

        val nbtLore = player.mainHandStack.getSubNbt("display") ?: player.mainHandStack.getOrCreateSubNbt("display")

        var list = NbtList()
        if (nbtLore!!.contains("Lore")) {
            list = nbtLore.getList("Lore", NbtElement.STRING_TYPE.toInt())
        }

        if (line != -1 && list.size <= line - 1) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "wrong_line")
                    .getMessage()
            )
        }

        val formattedText = TextParserUtils.formatText(lore).copy()
        if (!formattedText.style.isItalic) formattedText.styled { it.withItalic(false) }

        val loreJson = NbtString.of(Serializer.toJson(formattedText))
        if (line == -1) list.add(loreJson)
        else list[line - 1] = loreJson
        nbtLore.put("Lore", list)

        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(
                    Text.literal((if (line == -1) nbtLore.size + 1 else line).toString()),
                    formattedText
                )
                .getMessage()
        )
    }
}