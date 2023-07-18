package space.ryzhenkov.servertoolbox.commands.components

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.RegistrableCommand
import net.silkmc.silk.commands.command
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.config.objects.CommandCustomization
import space.ryzhenkov.servertoolbox.utils.UniqueColors

abstract class CommandBase {
    abstract val literal: String
    abstract val aliases: Array<String>?
    abstract val permissionLevel: Int
    abstract val arguments: Array<CommandArgument>?

    fun register() {
        if (getCustomizedSettings()?.disabled == true) return
        wrapper()
        getCommandAliases().forEach {
            wrapper(it)
        }
    }

    private fun wrapper(
        commandLiteral: String = getCustomizedSettings()?.literal ?: literal
    ): RegistrableCommand<ServerCommandSource> {
        return command(commandLiteral) {
            requires { checkRequirements(it) }
            build(this)
        }
    }

    /**
     * Runs code once wrapper passed all checks
     */
    abstract fun build(builder: LiteralCommandBuilder<ServerCommandSource>)

    fun getCommandAliases(): Array<String> {
        val customAliases = getCustomizedSettings()?.aliases
        if (!customAliases.isNullOrEmpty()) return customAliases
        if (!aliases.isNullOrEmpty()) return aliases!!
        return arrayOf()
    }

    fun getCommandSuggestion(): String {
        var commandSuggestion = "/${literal}"
        if (!arguments.isNullOrEmpty()) {
            arguments!!.forEach {
                if (it.required) commandSuggestion += " ${it.key}"
            }
        }
        return commandSuggestion
    }

    fun getCommandArgumentText(argument: CommandArgument): MutableText {
        return argument.getArgumentLiteral().styled {
            val translation = Text.translatable(argument.getDescription())
            if (translation.string != argument.getDescription()) {
                return@styled it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, translation))
            } else {
                return@styled it
            }
        }
    }

    fun getCommandUsageText(): MutableText {
        val literal =
            if (getCustomizedSettings()?.literal.isNullOrEmpty()) literal else getCustomizedSettings()!!.literal!!
        return Text.literal("/${literal}").apply {
            arguments?.forEach { argument ->
                this.append(Text.literal(" ").append(getCommandArgumentText(argument)))
            }
        }
            .styled {
                val aliasesString = Text.literal(aliases?.joinToString(", ") ?: "-").formatted(Formatting.WHITE)
                val translation =
                    if (Text.translatable(getTranslationKey("description")).string != getTranslationKey("description")) {
                        Text.translatable(getTranslationKey("description"))
                    } else {
                        null
                    }
                val description = Text.translatable("server-toolbox.commands.aliases", aliasesString).apply {
                    styled {
                        style
                        style.withColor(UniqueColors.INFORMATIVE)
                    }
                    if (translation != null) {
                        append("\n")
                        append(translation.formatted(Formatting.GRAY))
                    }
                }

                it
                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
                    .withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getCommandSuggestion()))
            }
    }

    fun getCustomizedSettings(): CommandCustomization? {
        return if (Configs.GENERAL.commandCustomization.contains(literal)) {
            Configs.GENERAL.commandCustomization[literal]
        } else {
            null
        }
    }

    fun getTranslationKey(key: String): String {
        return "server-toolbox.commands.$literal.$key"
    }

    fun getPermission(suffix: String? = null): String {
        return "server-toolbox.commands.$literal${if (!suffix.isNullOrEmpty()) ".$suffix" else ""}"
    }

    /**
     * Performs a server/source permissions check using command details as base for string:
     * `server-toolbox.commands.(literal).(suffix)`
     */
    fun checkRequirements(
        source: ServerCommandSource,
        suffix: String = "use",
        permissionLevelFallback: Int? = null
    ): Boolean {
        return Permissions.check(
            source,
            getPermission(suffix),
            permissionLevelFallback ?: (getCustomizedSettings()?.permissionLevel ?: permissionLevel)
        )
    }

    /**
     * Performs a player permissions check using command details as base for string:
     * `server-toolbox.commands.(literal).(suffix)`
     */
    fun checkRequirements(
        player: ServerPlayerEntity,
        suffix: String = "use",
        permissionLevelFallback: Int? = null
    ): Boolean {
        return Permissions.check(
            player,
            getPermission(suffix),
            permissionLevelFallback ?: (getCustomizedSettings()?.permissionLevel ?: permissionLevel)
        )
    }
}