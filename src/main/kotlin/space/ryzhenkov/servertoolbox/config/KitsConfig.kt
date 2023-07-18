package space.ryzhenkov.servertoolbox.config

import me.lortseam.completeconfig.api.ConfigEntry
import me.lortseam.completeconfig.data.Config
import me.lortseam.completeconfig.data.ConfigOptions
import net.minecraft.server.network.ServerPlayerEntity
import space.ryzhenkov.servertoolbox.config.objects.kits.Kit
import space.ryzhenkov.servertoolbox.config.objects.kits.KitTimeout
import java.time.LocalDateTime

class KitsConfig : Config(
    ConfigOptions.mod("server-toolbox").branch(arrayOf("kits"))
        .fileHeader(
            "This config stores all kit related information\n" +
                    "Documentation: https://27rogi.gitbook.io/server-toolbox/configuration/kits"
        )
), VersionableConfig {
    @ConfigEntry(comment = "List of all available kits")
    var kits: MutableMap<String, Kit> = mutableMapOf()

    @ConfigEntry(comment = "List of players that have used kit with timeout")
    private var timeouts: MutableMap<String, MutableMap<String, KitTimeout>?> = mutableMapOf()

    fun getKits(): Array<String> {
        return kits.keys.toTypedArray()
    }

    // TODO: Players must have special NBT tag that allows us to check their kit timeouts
    // If kit should tick offline then we save player last login datetime and check it after he joins
    fun hasTimeout(player: ServerPlayerEntity, kitName: String): Boolean {
        if (timeouts[player.gameProfile.name]?.get(kitName) == null) return false

        val kitTimeout = timeouts[player.gameProfile.name]!![kitName]!!
        if (kitTimeout.cooldown != null) {
            return kitTimeout.cooldown == 0
        }
        val expired = kitTimeout.isDateExpired()
        if (expired) {
            timeouts[player.gameProfile.name]!!.remove(kitName)
        }
        return !expired
    }

    fun getTimeout(player: ServerPlayerEntity, kitName: String): KitTimeout? {
        return timeouts[player.gameProfile.name]?.get(kitName)
    }

    fun addTimeout(player: ServerPlayerEntity, kitName: String): Boolean {
        if (!kits.containsKey(kitName)) return false
        if (!timeouts.containsKey(player.gameProfile.name)) {
            timeouts[player.gameProfile.name] = mutableMapOf()
        }
        timeouts[player.gameProfile.name]!![kitName] = KitTimeout().apply {
            setDateTime(LocalDateTime.now().plusSeconds(kits[kitName]!!.timeout.toLong()))
        }
        return true
    }

    fun removeTimeout(player: ServerPlayerEntity, kitName: String): Boolean {
        if (!timeouts.containsKey(player.gameProfile.name)) return false
        if (!timeouts[player.gameProfile.name]!!.containsKey(kitName)) return false
        timeouts[player.gameProfile.name]!!.remove(kitName)
        return true
    }

    /**
     * Removes all expired timeout entries from configuration, prevents file growth
     * This function reloads configuration!
     **/
    fun cleanupTimeouts(): Int {
        var removedCount = 0
        with(timeouts.iterator()) {
            val timeoutIterator = this
            forEach { playerTimeouts ->
                if (!playerTimeouts.value.isNullOrEmpty()) {
                    with(playerTimeouts.value!!.iterator()) {
                        forEach { playerTimeout ->
                            if (playerTimeout.value.isDateExpired()) {
                                remove()
                                if (playerTimeouts.value.isNullOrEmpty()) timeoutIterator.remove() // remove entire player entry if it got empty
                                removedCount++
                            }
                        }
                    }
                } else {
                    remove()
                }
            }
        }
        Configs.saveAndLoad(this)
        return removedCount
    }

    @ConfigEntry(comment = "Do not touch, required correct config detection!")
    private var configVersion: Int = 1

    override fun hasActualConfigVersion(): Boolean {
        return configVersion == 1
    }
}