package space.ryzhenkov.servertoolbox.commands.player

import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.utils.ResultTypes
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class KitsCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "kit", true),
        CommandArgument(this, "player", false),
        // TODO: Implement timer that ticks only when player is online
        // CommandArgument(this, "ticksOffline", false, unique = true)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, StringArgumentType.word()) { nameArg ->
                argument(arguments[1].key, EntityArgumentType.player()) { playerArg ->
                    requires { checkRequirements(it, "others", PermissionLevel.BAN_RIGHTS.level) }
                    runs {
                        getKit(playerArg.invoke(this).getPlayer(source), nameArg.invoke(this), source)
                    }
                }
                suggestList { suggestKits(it.source.playerOrThrow) }
                runs {
                    getKit(source.playerOrThrow, nameArg.invoke(this))
                }
            }
            runs {
                getKits(source.playerOrThrow)
            }
        }
    }

    fun suggestKits(player: ServerPlayerEntity): Iterable<String> {
        return Configs.KITS.kits.keys.filter { kitName ->
            if (Configs.KITS.kits[kitName] != null) {
                return@filter checkRequirements(player, "kit.$kitName", PermissionLevel.BAN_RIGHTS.level)
            }
            return@filter false
        }.asIterable()
    }

    private fun getKits(player: ServerPlayerEntity) {
        val availableKits = arrayListOf<String>()
        for ((kitName) in Configs.KITS.kits) {
            if (checkRequirements(player, "kit.$kitName", PermissionLevel.BAN_RIGHTS.level)) {
                availableKits.add(kitName)
            }
        }
        if (availableKits.isEmpty()) {
            return player.sendMessage(CommandResultMessage(ResultTypes.WARNING, this, "kits").getMessage())
        }
        player.sendMessage(
            CommandResultMessage(ResultTypes.OTHER, this, "list")
                .addArgs(Text.literal(availableKits.joinToString(", "))).getMessage()
        )
    }

    private fun getKit(player: ServerPlayerEntity, kitName: String, source: ServerCommandSource? = null) {
        if (!Configs.KITS.kits.containsKey(kitName)) {
            return player.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "not_found").getMessage())
        }
        if (source != null) {
            if (!checkRequirements(source, "kit.$kitName", PermissionLevel.BAN_RIGHTS.level)) {
                return source.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "no_permissions").getMessage())
            }
        } else {
            if (!checkRequirements(player, "kit.$kitName", PermissionLevel.BAN_RIGHTS.level)) {
                return player.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "no_permissions").getMessage())
            }
        }
        if (!checkRequirements(player, "bypass.$kitName", PermissionLevel.BAN_RIGHTS.level)) {
            if (Configs.KITS.hasTimeout(player, kitName) && source == null) {
                return player.sendMessage(
                    CommandResultMessage(ResultTypes.ERROR, this, "timeout")
                        .addArgs(
                            Text.literal(kitName),
                            Text.literal(
                                Configs.KITS.getTimeout(player, kitName)!!.getParsedDateTime()
                                    .until(LocalDateTime.now(), ChronoUnit.SECONDS).toString()
                            )
                        )
                        .getMessage()
                )
            }
            Configs.KITS.addTimeout(player, kitName)
        }
        Configs.KITS.kits[kitName]?.items?.forEach {
            val item = it.getItem(player)
            if (player.inventory.getStack(it.slot).isEmpty) {
                player.inventory.setStack(it.slot, item)
            } else {
                if (player.inventory.emptySlot == -1) player.dropStack(item) // if player can't obtain item then we drop it
                else player.giveItemStack(item)
            }
        }
        Configs.saveAndLoad()
        return player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "get")
                .addArgs(Text.literal(kitName))
                .getMessage()
        )
    }
}