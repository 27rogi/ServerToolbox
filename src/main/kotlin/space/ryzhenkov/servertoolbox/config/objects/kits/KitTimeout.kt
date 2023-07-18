package space.ryzhenkov.servertoolbox.config.objects.kits

import me.lortseam.completeconfig.api.ConfigEntries
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.time.LocalDateTime

@ConfigSerializable
@ConfigEntries(includeAll = true)
class KitTimeout {
    private var datetime: String? = null
    var cooldown: Int? = null

    fun setDateTime(dateTime: LocalDateTime?) {
        datetime = dateTime?.toString()
    }

    fun getDateTime(): String? {
        return datetime
    }

    fun getParsedDateTime(): LocalDateTime {
        return LocalDateTime.parse(datetime)
    }

    fun isDateExpired(): Boolean {
        return LocalDateTime.now().isAfter(LocalDateTime.parse(datetime))
    }
}