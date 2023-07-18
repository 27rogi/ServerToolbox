package space.ryzhenkov.servertoolbox.config

interface VersionableConfig {
    fun hasActualConfigVersion(): Boolean
}