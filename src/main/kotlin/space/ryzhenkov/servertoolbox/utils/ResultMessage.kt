package space.ryzhenkov.servertoolbox.utils

class ResultMessage(
    override val type: ResultTypes,
    override val key: String,
) : AbstractResultMessage() {
    override var translationString: String = "server-toolbox.messages.${key}"

    init {
        decorate()
    }
}