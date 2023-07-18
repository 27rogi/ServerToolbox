package space.ryzhenkov.servertoolbox.config

import me.lortseam.completeconfig.api.ConfigContainer.Transitive
import me.lortseam.completeconfig.api.ConfigEntries
import me.lortseam.completeconfig.api.ConfigEntry
import me.lortseam.completeconfig.api.ConfigGroup
import me.lortseam.completeconfig.data.Config
import me.lortseam.completeconfig.data.ConfigOptions
import net.minecraft.server.network.ServerPlayerEntity
import space.ryzhenkov.servertoolbox.config.objects.CommandCustomization
import space.ryzhenkov.servertoolbox.config.objects.SpawnLocation

class GeneralConfig : Config(
    ConfigOptions.mod("server-toolbox").branch(arrayOf("config"))
        .fileHeader(
            """
                    This is main file of the Server Toolbox, customize it as you want!
                    Documentation: https://27rogi.gitbook.io/server-toolbox
                """.trimIndent()
        )
), VersionableConfig {
    @Transitive
    @ConfigEntries(includeAll = true)
    object Modules : ConfigGroup {
        var motd: Boolean = true
        var rules: Boolean = true
        var kits: Boolean = true
        var colorfulAnvils: Boolean = true

        @ConfigEntry(comment = "As of now broadcaster can cause unexpected behaviour, use with caution!")
        var broadcaster: Boolean = false
        var spawn: Boolean = true
    }

    @Transitive
    @ConfigEntries(includeAll = true)
    object Spawn : ConfigGroup {
        @ConfigEntry(comment = "Player will be respawned on spawn on death if he has no spawnpoint")
        var respawnOnSpawn: Boolean = true

        @ConfigEntry(comment = "Ignores player current spawnpoint and respawns him on spawn coordinates")
        var ignoreSpawnPoint: Boolean = false

        @ConfigEntry(comment = "List of spawn locations in dimensions")
        var spawnLocations: MutableMap<String, SpawnLocation> = mutableMapOf()

        fun getPrimaryLocationKey(): String? {
            var key: String? = null
            for ((id, location) in spawnLocations) {
                if (location.primary == true) {
                    key = id
                    break
                }
            }
            return key
        }

        fun getSpawnLocationKey(player: ServerPlayerEntity): String? {
            var location = getPrimaryLocationKey()
            val playerWorldKey = player.world.registryKey.value.toString()
            if (spawnLocations.containsKey(playerWorldKey)) {
                spawnLocations[playerWorldKey]!!.let {
                    if (it.ignorePrimary == true) location = playerWorldKey
                }
            }
            return location
        }
    }

    @ConfigEntry(
        comment = "Option to customize commands, for more information check:\n" +
                "https://27rogi.gitbook.io/server-toolbox/commands/customization"
    )
    var commandCustomization: MutableMap<String, CommandCustomization> = mutableMapOf()

    //  TODO: Find a way to disable any command server-side (might be achievable using mixins?)
    //  @ConfigEntry(comment = "List of players with their homes.")
    //  var disabledCommands: Array<String> = arrayOf()

    @ConfigEntry(comment = "Do not touch, required correct config detection!")
    private var configVersion: Int = 1

    override fun hasActualConfigVersion(): Boolean {
        return configVersion == 1
    }
}