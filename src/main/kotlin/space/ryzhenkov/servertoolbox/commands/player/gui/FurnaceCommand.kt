package space.ryzhenkov.servertoolbox.commands.player.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.FurnaceScreenHandler
import net.minecraft.screen.ScreenHandler
import space.ryzhenkov.servertoolbox.commands.components.CommandScreenBase

class FurnaceCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandScreenBase() {
    override val type: Item = Items.FURNACE
    override fun getScreenHandler(syncId: Int, inventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return object : FurnaceScreenHandler(
            syncId,
            inventory,
        ) {
            override fun canUse(player: PlayerEntity?): Boolean {
                return true
            }
        }
    }

}