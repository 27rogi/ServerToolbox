package space.ryzhenkov.servertoolbox.config.objects.kits

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class Kit {
    var items: Array<KitItem> = arrayOf()
    var timeout: Int = 0
    // Must be uncommented once tick offline feature is implemented
    // var ticksIfOffline = true
}