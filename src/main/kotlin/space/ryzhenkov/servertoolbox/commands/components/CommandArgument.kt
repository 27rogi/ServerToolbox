package space.ryzhenkov.servertoolbox.commands.components

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import space.ryzhenkov.servertoolbox.utils.UniqueColors

class CommandArgument(
    val command: CommandBase,
    val key: String,
    val required: Boolean = true,
    val unique: Boolean = false,
    val literals: Array<String>? = null,
) {
    fun getName(): String {
        return if (unique) {
            "server-toolbox.commands.${command.literal}.arguments.$key.name"
        } else {
            "server-toolbox.arguments.$key.name"
        }
    }

    fun getDescription(): String {
        return if (unique) {
            "server-toolbox.commands.${command.literal}.arguments.$key.description"
        } else {
            "server-toolbox.arguments.$key.description"
        }
    }

    fun getBrackets(): Pair<String, String> {
        return if (!literals.isNullOrEmpty())
            Pair("[", "]")
        else if (required)
            Pair("<", ">")
        else
            Pair("[<", ">]")
    }

    fun getArgumentLiteral(): MutableText {
        val brackets = getBrackets()
        val styledKey = if (literals.isNullOrEmpty()) Text.translatableWithFallback(
            getName(),
            key
        ) else Text.literal(literals.joinToString(" | "))
        styledKey.styled {
            return@styled if (!literals.isNullOrEmpty())
                it.withColor(Formatting.WHITE)
            else if (required)
                it.withColor(UniqueColors.WARNING)
            else
                it.withColor(Formatting.DARK_GRAY)
        }
        return Text.literal(brackets.first).append(styledKey).append(brackets.second).styled {
            it.withColor(Formatting.GRAY)
        }
    }
}
