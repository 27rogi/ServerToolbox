package space.ryzhenkov.servertoolbox.commands.player.gui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.StonecutterScreenHandler
import space.ryzhenkov.servertoolbox.commands.components.CommandScreenBase

class StonecutterCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandScreenBase() {
    override val type: Item = Items.STONECUTTER
    override fun getScreenHandler(syncId: Int, inventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return object : StonecutterScreenHandler(
            syncId,
            inventory,
            ScreenHandlerContext.create(player.entityWorld, player.blockPos)
        ) {
            override fun canUse(player: PlayerEntity?): Boolean {
                return true
            }
        }
    }
}