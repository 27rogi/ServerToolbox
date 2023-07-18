package space.ryzhenkov.servertoolbox.utils

import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

abstract class AbstractResultMessage {
    abstract val type: ResultTypes
    abstract val key: String?

    abstract var translationString: String
    private var style = Style.EMPTY
    open var args: ArrayList<MutableText> = arrayListOf()

    fun decorate() {
        translationString += when (type) {
            ResultTypes.ERROR -> ".error"
            ResultTypes.SUCCESS -> ".success"
            ResultTypes.WARNING -> ".warning"
            ResultTypes.OTHER -> ".other"
        }
        style = when (type) {
            ResultTypes.ERROR -> style.withColor(UniqueColors.DANGEROUS)
            ResultTypes.SUCCESS -> style.withColor(UniqueColors.SUCCESS)
            ResultTypes.WARNING -> style.withColor(UniqueColors.WARNING)
            ResultTypes.OTHER -> style.withColor(UniqueColors.INFORMATIVE)
        }
    }

    fun addArgs(vararg items: MutableText): AbstractResultMessage {
        items.forEach {
            args.add(it.styled { getArgumentStyle(type) })
        }
        return this
    }

    fun getMessage(fallback: String? = null, vararg items: MutableText): MutableText {
        return Text.translatableWithFallback(translationString, fallback, *args.toTypedArray(), *items).setStyle(style)
    }

    companion object {
        fun getArgumentStyle(type: ResultTypes): Style {
            var style = Style.EMPTY
            style = when (type) {
                ResultTypes.ERROR -> style.withColor(UniqueColors.WARNING)
                ResultTypes.SUCCESS -> style.withColor(Formatting.WHITE)
                ResultTypes.WARNING -> style.withColor(UniqueColors.INFORMATIVE)
                ResultTypes.OTHER -> style.withColor(UniqueColors.EXTRA)
            }
            return style
        }
    }
}