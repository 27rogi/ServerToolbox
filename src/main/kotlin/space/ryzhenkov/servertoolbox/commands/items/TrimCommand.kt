package space.ryzhenkov.servertoolbox.commands.items

import net.minecraft.command.argument.RegistryKeyArgumentType
import net.minecraft.item.ArmorItem
import net.minecraft.item.trim.ArmorTrimMaterial
import net.minecraft.item.trim.ArmorTrimPattern
import net.minecraft.nbt.NbtString
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.LiteralCommandBuilder
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class TrimCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "material", true, unique = true),
        CommandArgument(this, "pattern", true, unique = true),
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, RegistryKeyArgumentType.registryKey(RegistryKeys.TRIM_MATERIAL)) { arg1 ->
                argument(arguments[1].key, RegistryKeyArgumentType.registryKey(RegistryKeys.TRIM_PATTERN)) { arg2 ->
                    runs {
                        setTrim(
                            source.playerOrThrow,
                            arg1.invoke(this),
                            arg2.invoke(this)
                        )
                    }
                }
            }
        }
    }

    private fun setTrim(
        player: ServerPlayerEntity,
        materialKey: RegistryKey<ArmorTrimMaterial>,
        patternKey: RegistryKey<ArmorTrimPattern>
    ) {
        if (player.mainHandStack.isEmpty) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "empty")
                    .getMessage()
            )
        }

        if (player.mainHandStack.item !is ArmorItem) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "not_armor")
                    .getMessage()
            )
        }

        if (!player.world.registryManager.get(RegistryKeys.TRIM_MATERIAL).contains(materialKey) ||
            !player.world.registryManager.get(RegistryKeys.TRIM_PATTERN).contains(patternKey)
        ) {
            return player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "wrong_materials")
                    .getMessage()
            )
        }

        player.mainHandStack.nbt?.remove("Trim")
        val nbt = player.mainHandStack.getOrCreateSubNbt("Trim")
        nbt.put("material", NbtString.of(materialKey.value.toString()))
        nbt.put("pattern", NbtString.of(patternKey.value.toString()))
    }
}