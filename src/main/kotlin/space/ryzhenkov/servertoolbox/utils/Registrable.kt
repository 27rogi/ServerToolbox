package space.ryzhenkov.servertoolbox.utils

import space.ryzhenkov.servertoolbox.ServerToolbox

abstract class Registrable(val name: String) {
    fun register(): Boolean {
        if (!onRegister()) throw Error("Unable to register `$name`, please report this bug to GitHub repo!")
        ServerToolbox.logger.info("Registered $name")
        return true
    }

    abstract fun onRegister(): Boolean
}