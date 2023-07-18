package space.ryzhenkov.servertoolbox.commands.chat

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.config.objects.broadcaster.Broadcast
import space.ryzhenkov.servertoolbox.config.objects.broadcaster.BroadcastMessage
import space.ryzhenkov.servertoolbox.utils.Broadcaster
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class BroadcasterCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(
            this,
            "literal",
            true,
            literals = arrayOf(
                "create", "remove", "reload", "messages set", "messages add", "messages remove", "debug"
            )
        ),
        CommandArgument(this, "broadcast", false, unique = true),
        CommandArgument(this, "timeout", false),
        CommandArgument(this, "text", false),
        CommandArgument(this, "line", false),
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            literal("create") {
                requires { checkRequirements(it.playerOrThrow, "admin") }
                argument<String>(arguments[1].key) { broadcastId ->
                    argument(arguments[2].key, IntegerArgumentType.integer(1)) { broadcastTimeout ->
                        runs { createBroadcast(source, broadcastId.invoke(this), broadcastTimeout.invoke(this)) }
                    }
                }
            }
            literal("remove") {
                requires { checkRequirements(it.playerOrThrow, "edit") }
                argument<String>(arguments[1].key) { broadcastId ->
                    suggestList { suggestBroadcasts(it.source) }
                    runs { removeBroadcast(source, broadcastId.invoke(this)) }
                }
            }
            literal("reload") {
                requires { checkRequirements(it.playerOrThrow, "admin") }
                runs {
                    Broadcaster.applyChangesSafely(source.server) {
                        Configs.BROADCASTER.load()
                    }
                    source.sendMessage(
                        CommandResultMessage(
                            ResultTypes.SUCCESS,
                            this@BroadcasterCommand,
                            "reload"
                        ).getMessage()
                    )
                }
            }
            literal("messages") {
                requires { checkRequirements(it.playerOrThrow, "edit") }
                argument<String>(arguments[1].key) { broadcastId ->
                    suggestList { suggestBroadcasts(it.source) }
                    literal("add") {
                        argument(arguments[3].key, StringArgumentType.greedyString()) { textArg ->
                            runs { setBroadcastMessage(source, broadcastId.invoke(this), textArg.invoke(this), 0) }
                        }
                    }
                    literal("set") {
                        argument(arguments[4].key, IntegerArgumentType.integer(0)) { lineArg ->
                            argument(arguments[3].key, StringArgumentType.greedyString()) { textArg ->
                                runs {
                                    setBroadcastMessage(
                                        source,
                                        broadcastId.invoke(this),
                                        textArg.invoke(this),
                                        lineArg.invoke(this)
                                    )
                                }
                            }
                        }
                    }
                    literal("remove") {
                        argument(arguments[4].key, IntegerArgumentType.integer(0)) { lineArg ->
                            runs { removeBroadcastMessage(source, broadcastId.invoke(this), lineArg.invoke(this)) }
                        }
                    }
                }
            }
            literal("debug") {
                requires { checkRequirements(it.playerOrThrow, "admin") }
                runs {
                    source.sendMessage(
                        Text.of(
                            arrayListOf(
                                "Tasks registered: ${Broadcaster.tasks.size}",
                                "Order entries size: ${Broadcaster.orders.size}"
                            ).joinToString("\n")
                        )
                    )
                }
            }
        }
    }

    private fun suggestBroadcasts(source: ServerCommandSource): Iterable<String> {
        return Configs.BROADCASTER.broadcasts.filter {
            checkRequirements(source, "edit.${it.key}")
        }.keys.asIterable()
    }

    private fun removeBroadcastMessage(source: ServerCommandSource, broadcastId: String, index: Int) {
        if (Configs.BROADCASTER.broadcasts[broadcastId]?.messages?.getOrNull(index) == null) {
            return source.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "not_found").getMessage())
        }
        Broadcaster.applyChangesSafely(source.server, broadcastId) {
            Configs.BROADCASTER.broadcasts[broadcastId]!!.messages.removeAt(index)
            Configs.saveAndLoad(Configs.BROADCASTER)
        }
        source.sendMessage(CommandResultMessage(ResultTypes.SUCCESS, this, "messages.removed").getMessage())
    }

    private fun setBroadcastMessage(source: ServerCommandSource, broadcastId: String, text: String, index: Int) {
        if (!Configs.BROADCASTER.broadcasts.containsKey(broadcastId)) {
            return source.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "not_found").getMessage())
        }
        val message = BroadcastMessage().apply { lines = text.split("|") }
        Broadcaster.applyChangesSafely(source.server, broadcastId) {
            Configs.BROADCASTER.broadcasts[broadcastId]?.messages?.getOrNull(index).let { broadcastMessage ->
                if (broadcastMessage != null) {
                    Configs.BROADCASTER.broadcasts[broadcastId]!!.messages[index] = message
                } else {
                    Configs.BROADCASTER.broadcasts[broadcastId]!!.messages.add(message)
                }
            }
            Configs.saveAndLoad(Configs.BROADCASTER)
        }
        source.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "messages.added")
                .addArgs(
                    Placeholders.parseText(
                        TextParserUtils.formatText(text.replace("|", "\n")),
                        PlaceholderContext.of(source.player?.gameProfile, source.server)
                    ).copy()
                )
                .getMessage()
        )
    }

    private fun createBroadcast(source: ServerCommandSource, broadcastId: String, broadcastTimeout: Int) {
        if (Configs.BROADCASTER.broadcasts.containsKey(broadcastId)) {
            return source.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "already_exists").getMessage())
        }
        Broadcaster.applyChangesSafely(source.server) {
            Configs.BROADCASTER.broadcasts[broadcastId] = Broadcast().apply {
                timeout = broadcastTimeout.toLong()
            }
            Configs.saveAndLoad(Configs.BROADCASTER)
        }
        source.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "created")
                .addArgs(
                    Text.literal(broadcastId)
                )
                .getMessage()
        )
    }

    private fun removeBroadcast(source: ServerCommandSource, broadcastId: String) {
        if (!Configs.BROADCASTER.broadcasts.containsKey(broadcastId)) {
            return source.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "not_found").getMessage())
        }
        Broadcaster.applyChangesSafely(source.server, broadcastId) {
            Configs.BROADCASTER.broadcasts.remove(broadcastId)
            Configs.saveAndLoad(Configs.BROADCASTER)
        }
        source.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "removed")
                .addArgs(
                    Text.literal(broadcastId)
                )
                .getMessage()
        )
    }
}