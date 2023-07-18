package space.ryzhenkov.servertoolbox.commands.player

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.nbt.NbtElement
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.config.objects.kits.Kit
import space.ryzhenkov.servertoolbox.config.objects.kits.KitItem
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class KitCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "literal", false, literals = arrayOf("add", "remove", "reset")),
        CommandArgument(this, "kit", true),
        CommandArgument(this, "timeout", false, unique = true),
        CommandArgument(this, "player", false),
        // TODO: Implement timer that ticks only when player is online
        // CommandArgument(this, "ticksOffline", false, unique = true)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            literal("add") {
                requires { checkRequirements(it, "add", PermissionLevel.BAN_RIGHTS.level) }
                argument<String>(arguments[1].key) { nameArg ->
                    argument(arguments[2].key, IntegerArgumentType.integer(0)) { timeoutArg ->
                        runs {
                            addKit(source.playerOrThrow, nameArg.invoke(this), timeoutArg.invoke(this))
                        }
                    }
                }
            }
            literal("remove") {
                requires { checkRequirements(it, "remove", PermissionLevel.BAN_RIGHTS.level) }
                argument<String>(arguments[1].key) { nameArg ->
                    suggestList { suggestKits(it.source.playerOrThrow) }
                    runs {
                        removeKit(source, nameArg.invoke(this))
                    }
                }
            }
            literal("reset") {
                requires { checkRequirements(it, "reset", PermissionLevel.BAN_RIGHTS.level) }
                argument<String>(arguments[1].key) { nameArg ->
                    suggestList { suggestKits(it.source.playerOrThrow) }
                    argument(arguments[3].key, EntityArgumentType.player()) { playerArg ->
                        requires { checkRequirements(it, "others.reset", PermissionLevel.BAN_RIGHTS.level) }
                        runs {
                            resetKit(playerArg.invoke(this).getPlayer(this.source), nameArg.invoke(this))
                        }
                    }
                    runs {
                        resetKit(source.playerOrThrow, nameArg.invoke(this))
                    }
                }
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

    private fun removeKit(source: ServerCommandSource, kitName: String) {
        if (!Configs.KITS.kits.containsKey(kitName)) {
            return source.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "not_found").getMessage())
        }
        Configs.KITS.kits.remove(kitName)
        Configs.saveAndLoad(Configs.KITS)
        source.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "removed")
                .addArgs(
                    Text.literal(kitName)
                )
                .getMessage()
        )
    }

    private fun resetKit(player: ServerPlayerEntity, kitName: String) {
        if (!Configs.KITS.kits.containsKey(kitName)) {
            return player.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "not_found").getMessage())
        }
        if (!Configs.KITS.hasTimeout(player, kitName)) {
            return player.sendMessage(CommandResultMessage(ResultTypes.ERROR, this, "no_timeout").getMessage())
        }
        Configs.KITS.removeTimeout(player, kitName)
        Configs.saveAndLoad(Configs.KITS)
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "reset")
                .addArgs(
                    Text.literal(kitName),
                    Text.literal(player.gameProfile.name)
                )
                .getMessage()
        )
    }

    private fun addKit(player: ServerPlayerEntity, kitName: String, timeoutArg: Int) {
        val kit = Kit().apply {
            timeout = timeoutArg
        }
        player.inventory.main.forEachIndexed { index, itemStack ->
            if (!itemStack.isEmpty) {
                kit.items = kit.items.plus(KitItem().apply {
                    if (itemStack.hasCustomName()) name = Text.Serializer.toJson(itemStack.name)
                    identifier = Registries.ITEM.getEntry(itemStack.item).key.get().value.toString()
                    slot = index
                    durability = itemStack.damage
                    count = itemStack.count
                    if (itemStack.getSubNbt("display") != null) {
                        val list: MutableList<String> = mutableListOf()
                        itemStack.getSubNbt("display")!!.getList("Lore", NbtElement.STRING_TYPE.toInt()).forEach {
                            list.add(Text.of(it.asString()).string)
                        }
                        lore = list.toTypedArray()
                    }
                    if (itemStack.hasEnchantments()) {
                        enchantments = hashMapOf()
                        EnchantmentHelper.fromNbt(itemStack.enchantments!!).forEach {
                            (enchantments as HashMap<String, Int>)[Registries.ENCHANTMENT.getEntry(it.key).key.get().value.toString()] =
                                it.value
                        }
                    }
                })
            }
        }
        Configs.KITS.kits[kitName] = kit
        Configs.saveAndLoad()
        return player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this, "add")
                .addArgs(Text.literal(kitName))
                .getMessage()
        )
    }
}