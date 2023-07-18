package space.ryzhenkov.servertoolbox.commands.components

import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.utils.ResultTypes

abstract class CommandScreenBase : CommandBase() {
    abstract override val literal: String
    abstract override val aliases: Array<String>?
    abstract override val permissionLevel: Int
    abstract val type: Item

    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "player", false)
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, EntityArgumentType.player()) {
                requires { checkRequirements(it, "others", PermissionLevel.BAN_RIGHTS.level) }
                runs {
                    open(source, EntityArgumentType.getPlayer(this, arguments[0].key))
                }
            }
            runs {
                open(source, source.playerOrThrow)
            }
        }
    }

    abstract fun getScreenHandler(syncId: Int, inventory: PlayerInventory, player: PlayerEntity): ScreenHandler

    private fun open(source: ServerCommandSource, player: ServerPlayerEntity) {
        val screen = SimpleNamedScreenHandlerFactory(
            { syncId, inventory, playerEntity -> getScreenHandler(syncId, inventory, playerEntity) },
            Text.translatable(type.translationKey)
        )
        player.openHandledScreen(screen)
        if (source.player != null && source.player!!.uuidAsString != player.uuidAsString) {
            source.player!!.sendMessage(
                CommandResultMessage(ResultTypes.SUCCESS, this)
                    .addArgs(Text.translatable(type.translationKey), player.name.copy())
                    .getMessage()
            )
        }
    }
}