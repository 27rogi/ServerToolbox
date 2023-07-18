package space.ryzhenkov.servertoolbox.config

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import me.lortseam.completeconfig.api.ConfigEntry
import me.lortseam.completeconfig.data.Config
import me.lortseam.completeconfig.data.ConfigOptions
import net.minecraft.text.MutableText

class MessagesConfig : Config(
    ConfigOptions.mod("server-toolbox").branch(arrayOf("messages"))
        .fileHeader(
            "Unique messages are stored here, more info in documentation.\n" +
                    "Documentation: https://27rogi.gitbook.io/server-toolbox/configuration/messages"
        )
) {
    @ConfigEntry(comment = "This message will be shown to player once he joins the server")
    var motd: ArrayList<String> = arrayListOf(
        "<gradient:#008080:#12e38a>This server is using ServerToolbox",
        "<gray>You logged as <green>%player:name%<gray>, world time is <aqua>%world:time%",
        "<gray>Server has <yellow>%server:online% <gray>player(s) online"
    )

    // TODO: Add sections (like "3.1") and ability to show specific section to player
    @ConfigEntry(comment = "This message can be seen by players using /rules command")
    var rules: ArrayList<String> = arrayListOf(
        "<yellow>1. Be nice to others <red><3"
    )

    fun getFormattedMessage(strings: ArrayList<String>, context: PlaceholderContext): MutableText {
        return Placeholders.parseText(TextParserUtils.formatText(strings.joinToString("\n")), context).copy()
    }
}