package space.ryzhenkov.servertoolbox.config.objects

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class SpawnLocation {
    var location: Array<Double> = arrayOf()
    var head: Float = 0F
    var primary: Boolean? = null
    var ignorePrimary: Boolean? = null
}