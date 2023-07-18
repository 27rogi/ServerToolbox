package space.ryzhenkov.servertoolbox.commands.world

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes


class SignCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "line"),
        CommandArgument(this, "text"),
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, IntegerArgumentType.integer(1, 4)) {
                argument(arguments[1].key, StringArgumentType.greedyString()) {
                    runs {
                        writeToSign(
                            this.source.playerOrThrow,
                            IntegerArgumentType.getInteger(this, arguments[0].key),
                            StringArgumentType.getString(this, arguments[1].key),
                        )
                    }
                }
            }
        }
    }

    private fun writeToSign(player: ServerPlayerEntity, line: Int, text: String) {
        val hit: HitResult = player.raycast(
            20.0,
            0F,
            false
        )

        if (hit.type != HitResult.Type.BLOCK) {
            player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "not_block")
                    .getMessage()
            )
            return
        }

        val blockHit = hit as BlockHitResult
        if (player.world.getBlockEntity(blockHit.blockPos) !is SignBlockEntity) {
            player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "not_sign")
                    .getMessage()
            )
            return
        }

        val sign = player.world.getBlockEntity(hit.blockPos) as SignBlockEntity
        val isColorful = checkRequirements(player.commandSource, "formatting", PermissionLevel.COMMAND_RIGHTS.level)
        sign.changeText({
            it.withMessage(
                line - 1,
                if (isColorful) TextParserUtils.formatText(text) else Text.literal(text)
            )
        }, sign.isPlayerFacingFront(player))
        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(
                    Text.literal(line.toString()),
                    TextParserUtils.formatText(text).copy()
                )
                .getMessage()
        )
    }
}