package space.ryzhenkov.servertoolbox.config.objects.kits

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setLore
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import space.ryzhenkov.servertoolbox.utils.JsonUtils

@ConfigSerializable
class KitItem {
    var identifier: String = "minecraft:air"
    var slot: Int = 0
    var count: Int = 1
    var enchantments: MutableMap<String, Int>? = null
    var name: String? = null
    var lore: Array<String>? = null

    // TODO: Implement automated NBT to config data parser
    // var nbt: Array<String>? = null
    var durability: Int? = null

    fun getItem(player: ServerPlayerEntity? = null): ItemStack {
        return itemStack(Registries.ITEM.get(Identifier(identifier)), this@KitItem.count) {
            if (durability != null) damage = durability!!
            if (this@KitItem.name != null) {
                val formattedName = JsonUtils.parseTextFromJsonWithFallback(
                    this@KitItem.name!!,
                    TextParserUtils.formatText(this@KitItem.name)
                )
                if (player == null) setCustomName(formattedName)
                else setCustomName(Placeholders.parseText(formattedName, PlaceholderContext.of(player)))
            }
            if (!lore.isNullOrEmpty()) {
                val collection: MutableList<Text> = mutableListOf()
                for (loreLine in lore!!) {
                    val formattedLore =
                        JsonUtils.parseTextFromJsonWithFallback(loreLine, TextParserUtils.formatText(loreLine))
                    if (player == null) collection.add(formattedLore)
                    else collection.add(Placeholders.parseText(formattedLore, PlaceholderContext.of(player)))
                }
                setLore(collection)
            }
            if (!enchantments.isNullOrEmpty()) {
                this@KitItem.enchantments!!.forEach {
                    addEnchantment(Registries.ENCHANTMENT.get(Identifier(it.key)), it.value)
                }
            }
        }
    }
}