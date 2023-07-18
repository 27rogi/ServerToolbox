package space.ryzhenkov.servertoolbox.config.objects.broadcaster

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.time.LocalDateTime

@ConfigSerializable
class Broadcast {
    var prefix: String? = null
    var requiresPermission: Boolean = false
    var timeout: Long = 1
    var randomize: Boolean = false
    var minPlayers: Int = 1
    var lastRun: String? = null
    var nextRun: String = LocalDateTime.now().toString()
    var messages: ArrayList<BroadcastMessage> = arrayListOf()

    fun schedule(callback: () -> Unit) {

    }
}