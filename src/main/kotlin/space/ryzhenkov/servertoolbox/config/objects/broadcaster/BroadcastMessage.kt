package space.ryzhenkov.servertoolbox.config.objects.broadcaster

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class BroadcastMessage {
    // TODO: Add ability to send different types of messages (bossbar, actionbar, etc...)
    // var type: String = ""
    var lines: List<String> = listOf()
}