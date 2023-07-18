package space.ryzhenkov.servertoolbox.commands.entity

import com.mojang.brigadier.arguments.StringArgumentType
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import space.ryzhenkov.servertoolbox.commands.components.CommandArgument
import space.ryzhenkov.servertoolbox.commands.components.CommandBase
import space.ryzhenkov.servertoolbox.commands.components.CommandResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes


class EntityRenameCommand(
    override val literal: String,
    override val aliases: Array<String>?,
    override val permissionLevel: Int,
) : CommandBase() {
    override var arguments: Array<CommandArgument> = arrayOf(
        CommandArgument(this, "name"),
    )

    override fun build(builder: LiteralCommandBuilder<ServerCommandSource>) {
        builder.apply {
            argument(arguments[0].key, StringArgumentType.greedyString()) { nameArg ->
                runs {
                    renameEntity(
                        this.source.playerOrThrow,
                        StringArgumentType.getString(this, arguments[0].key),
                    )
                }
            }
        }
    }

    private fun renameEntity(player: ServerPlayerEntity, name: String) {
        // Credits to @CorruptionHades for finding a way to get entity using raycast
        val camera = player.cameraEntity
        val eyePos = camera.eyePos
        val max: Double = 50.0
        val vec3d2 = camera.getRotationVec(1.0F).multiply(max)
        val hit = ProjectileUtil.raycast(
            camera,
            eyePos,
            eyePos.add(vec3d2),
            camera.boundingBox.stretch(vec3d2).expand(1.0),
            { true },
            max * max
        )

        if (hit?.type != HitResult.Type.ENTITY) {
            player.sendMessage(
                CommandResultMessage(ResultTypes.ERROR, this, "not_entity")
                    .getMessage()
            )
            return
        }

        val isColorful = checkRequirements(player.commandSource, "formatting", PermissionLevel.COMMAND_RIGHTS.level)
        val entityHit = hit
        entityHit.entity.customName = if (isColorful) Placeholders.parseText(
            TextParserUtils.formatText(name), PlaceholderContext.of(entityHit.entity)
        ) else Text.literal(name)
        entityHit.entity.isCustomNameVisible = true

        player.sendMessage(
            CommandResultMessage(ResultTypes.SUCCESS, this)
                .addArgs(
                    TextParserUtils.formatText(name).copy()
                )
                .getMessage()
        )
    }
}