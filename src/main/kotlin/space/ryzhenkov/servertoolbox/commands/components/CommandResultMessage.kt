package space.ryzhenkov.servertoolbox.commands.components

import space.ryzhenkov.servertoolbox.utils.AbstractResultMessage
import space.ryzhenkov.servertoolbox.utils.ResultTypes

class CommandResultMessage(
    override val type: ResultTypes,
    command: CommandBase,
    override val key: String? = null
) : AbstractResultMessage() {
    override var translationString = "server-toolbox.messages.command.${command.literal}"

    init {
        if (!key.isNullOrEmpty()) {
            translationString += ".$key"
        }
        decorate()
    }
}