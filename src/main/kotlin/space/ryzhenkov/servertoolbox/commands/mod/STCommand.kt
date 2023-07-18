package space.ryzhenkov.servertoolbox.commands.mod

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.Commands
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.config.BroadcasterConfig
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.utils.Broadcaster
import space.ryzhenkov.servertoolbox.utils.ResultTypes
import space.ryzhenkov.servertoolbox.utils.UniqueColors

class STCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override val arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(
            this,
            "literal",
            required = false,
            unique = true,
            literals = arrayOf("help", "reload")
        )
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            literal("help") {
                requires { checkRequirements(it, "help", PermissionLevel.COMMAND_RIGHTS.level) }
                runs {
                    showHelp(this)
                }
            }
            literal("reload") {
                requires { checkRequirements(it, "reload", PermissionLevel.OWNER.level) }
                runs {
                    for (config in Configs.registry) {
                        if (config is BroadcasterConfig) {
                            Broadcaster.cancelTasks(true)
                            config.load()
                            Broadcaster.registerTasks(source.server, true)
                        } else {
                            config.load()
                        }
                    }
                    source.sendMessage(CommandResultMessage(ResultTypes.SUCCESS, this@STCommand).getMessage())
                }
            }
            runs {
                showInfo(this)
            }
        }
    }

    private fun showHelp(context: CommandContext<ServerCommandSource>) {
        for (category in Commands.Categories.entries) {
            val availableCommands = category.items.filter { command ->
                if (context.source.player != null) {
                    command.checkRequirements(context.source.player!!, permissionLevelFallback = command.permissionLevel)
                } else {
                    command.checkRequirements(context.source, permissionLevelFallback = command.permissionLevel)
                }
            }
            if (availableCommands.isNotEmpty()) {
                context.source.sendMessage(Text.empty().apply {
                    append("\n [ ")
                    append(Text.translatable(getTranslationKey("categories.${category.name}.name")))
                    append(" ] ")
                    append("\n")
                    styled { it.withColor(UniqueColors.INFORMATIVE).withFormatting(Formatting.BOLD) }
                })
                availableCommands.forEachIndexed { index, command ->
                    context.source.sendMessage(
                        if (index == category.items.size) { command.getCommandUsageText().append("\n") }
                        else { command.getCommandUsageText() }
                    )
                }
            }
        }
    }

    private fun showInfo(context: CommandContext<ServerCommandSource>) {
        val version = FabricLoader.getInstance().getModContainer("server-toolbox").get().metadata.version.friendlyString
        context.source.sendMessage(
            Text.literal("Server Toolbox ")
                .styled { it.withColor(UniqueColors.INFORMATIVE) }
                .append(
                    Text.literal(version).styled { it.withColor(UniqueColors.EXTRA).withFormatting(Formatting.BOLD) })
                .append(
                    Text.translatable(
                        getTranslationKey("info.usage"),
                        Text.literal("/st help").styled { it.withColor(UniqueColors.WARNING) })
                        .formatted(Formatting.GRAY)
                )
                .append(Text.translatable(getTranslationKey("info.farewell")).formatted(Formatting.GRAY))
        )
    }
}