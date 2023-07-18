package space.ryzhenkov.servertoolbox.config.objects

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class CommandCustomization {
    var literal: String? = null
    var permissionLevel: Int? = null
    var aliases: Array<String>? = null
    var disabled: Boolean? = null
}