package space.ryzhenkov.servertoolbox.commands.player.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandler
import space.ryzhenkov.servertoolbox.commands.components.CommandScreenBase


class EnderChestCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandScreenBase() {
    override val type: Item = Items.ENDER_CHEST
    override fun getScreenHandler(syncId: Int, inventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return GenericContainerScreenHandler.createGeneric9x3(
            syncId,
            inventory,
            player.enderChestInventory
        )
    }
}