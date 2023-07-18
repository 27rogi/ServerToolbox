package space.ryzhenkov.servertoolbox.utils

import com.google.gson.JsonSyntaxException
import net.minecraft.text.Text

object JsonUtils {
    fun parseTextFromJsonWithFallback(jsonString: String, fallbackText: Text): Text {
        return try {
            val result = Text.Serializer.fromJson(jsonString)
            result!!
        } catch (err: JsonSyntaxException) {
            fallbackText
        }
    }
}