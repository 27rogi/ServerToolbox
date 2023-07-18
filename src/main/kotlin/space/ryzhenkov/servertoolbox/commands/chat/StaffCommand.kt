package space.ryzhenkov.servertoolbox.commands.chat

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.core.server.players
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class StaffCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf()

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            runs {
                sendStaffMessage(source.playerOrThrow)
            }
        }
    }

    private fun sendStaffMessage(player: ServerPlayerEntity) {
        val staff: ArrayList<String> = arrayListOf()
        player.server.players.forEach {
            if (Permissions.check(
                    it,
                    "server-toolbox.staff",
                    PermissionLevel.BAN_RIGHTS.level
                )
            ) staff.add(it.name.string)
        }
        if (staff.isEmpty()) {
            player.sendMessage(CommandResultMessage(ResultTypes.WARNING, this, "empty").getMessage())
            return
        }
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(Text.literal(staff.size.toString()), Text.literal(staff.joinToString(", ")))
                .getMessage()
        )
    }
}