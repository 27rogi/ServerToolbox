package space.ryzhenkov.servertoolbox.utils

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.MinecraftServer
import space.ryzhenkov.servertoolbox.ServerToolbox
import space.ryzhenkov.servertoolbox.config.Configs
import space.ryzhenkov.servertoolbox.config.objects.broadcaster.Broadcast
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import kotlin.concurrent.schedule

object Broadcaster {
    val timer: Timer = Timer("server-toolbox.broadcaster")
    val tasks: HashMap<String, TimerTask> = hashMapOf()
    val orders: HashMap<String, Int> = hashMapOf()

    fun registerTasks(server: MinecraftServer, verbose: Boolean) {
        for (it in Configs.BROADCASTER.broadcasts) {
            registerTask(it.key, it.value, server, true)
        }
        if (verbose) ServerToolbox.logger.info("Scheduled ${tasks.keys.size} broadcast(s)")
    }

    fun registerTask(id: String, broadcast: Broadcast, server: MinecraftServer, verbose: Boolean) {
        LocalDateTime.parse(broadcast.nextRun).let { datetime ->
            if (datetime.isBefore(LocalDateTime.now())) {
                if (verbose) ServerToolbox.logger.info("Schedule `$id` has outdated datetime, starting countdown from now.")
                broadcast.nextRun = LocalDateTime.now().plusSeconds(broadcast.timeout).toString()
                Configs.saveAndLoad(Configs.BROADCASTER)
            }
        }
        val scheduledDateTime = Date.from(LocalDateTime.parse(broadcast.nextRun).toInstant(ZonedDateTime.now().offset))
        tasks[id] = timer.schedule(scheduledDateTime) {
            val players = if (broadcast.requiresPermission) {
                server.playerManager.playerList.filter {
                    Permissions.check(
                        it,
                        "server-toolbox.broadcasts.$id.receive"
                    )
                }
            } else {
                server.playerManager.playerList
            }
            if (players.size >= broadcast.minPlayers) {
                if (broadcast.messages.isNotEmpty()) {
                    val message =
                        if (!broadcast.randomize) broadcast.messages[nextInOrder(id)] else broadcast.messages.random()
                    for (player in server.playerManager.playerList) {
                        player.sendMessage(
                            Placeholders.parseText(
                                TextParserUtils.formatText((broadcast.prefix ?: "") + message.lines.joinToString("\n")),
                                PlaceholderContext.of(player.gameProfile, server)
                            )
                        )
                    }
                }

            }
            this.cancel()
            registerTask(id, broadcast, server, false)
        }
        if (verbose) ServerToolbox.logger.info("Scheduled broadcast `$id` to repeat every ${broadcast.timeout} second(s)")
    }

    /**
     * This function makes sure that all tasks will be stopped before any wrapped code is executed
     * to prevent any sorts of errors or crashes after loading edited configuration files.
     */
    fun applyChangesSafely(server: MinecraftServer, changes: () -> Unit) {
        cancelTasks(true)
        changes()
        registerTasks(server, false)
    }

    /**
     * This function makes sure that specified task will be stopped before any wrapped code is executed
     * to prevent any sorts of errors or crashes after loading edited configuration files.
     */
    fun applyChangesSafely(server: MinecraftServer, broadcastId: String, changes: () -> Unit) {
        cancelTask(broadcastId, true)
        changes()
        registerTask(broadcastId, Configs.BROADCASTER.broadcasts[broadcastId]!!, server, false)
    }

    private fun nextInOrder(broadcastId: String): Int {
        if (!orders.containsKey(broadcastId)) orders[broadcastId] = 0
        if (Configs.BROADCASTER.broadcasts[broadcastId]!!.messages.getOrNull(orders[broadcastId]!! + 1) != null) {
            orders[broadcastId] = orders[broadcastId]!! + 1
        } else {
            orders[broadcastId] = 0
        }
        return orders[broadcastId]!!
    }

    fun cancelTask(broadcastId: String, shouldDelete: Boolean = true): Boolean {
        if (tasks.containsKey(broadcastId)) return false
        val res = tasks[broadcastId]!!.cancel()
        if (shouldDelete) tasks.remove(broadcastId)
        timer.purge()
        return res
    }

    fun cancelTasks(shouldDelete: Boolean = true) {
        with(tasks.iterator()) {
            forEach { (id, task) ->
                task.cancel()
                if (shouldDelete) remove()
            }
        }
        timer.purge()
    }
}