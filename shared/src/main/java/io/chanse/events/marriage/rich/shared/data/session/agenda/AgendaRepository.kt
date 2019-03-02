package io.chanse.events.marriage.rich.shared.data.session.agenda

import io.chanse.events.marriage.rich.model.Block
import org.threeten.bp.ZonedDateTime

/**
 * Single point of access to agenda data for the presentation layer.
 */
interface AgendaRepository {
    fun getAgenda(): List<Block>
}

class DefaultAgendaRepository : AgendaRepository {

    companion object {
        private const val LABEL_HENNA = "Mehandi"
        private const val LABEL_RECEPTION_DINNER = "Reception & Dinner"
        private const val LABEL_REGISTRATION = "Registration"
        private const val LABEL_KEYNOTE = "Keynote"
        private const val LABEL_SESSIONS = "Sessions"
        private const val LABEL_BREAKFAST = "Breakfast"
        private const val LABEL_LUNCH = "Lunch"
        private const val LABEL_TEA_BREAK = "Tea Break"
        private const val LABEL_PARTY = "Party"

        private const val TYPE_HENNA = "henna"
        private const val TYPE_REGISTRATION = "badge"
        private const val TYPE_KEYNOTE = "keynote"
        private const val TYPE_SESSIONS = "session"
        private const val TYPE_MEAL = "meal"
        private const val TYPE_AFTER_HOURS = "after_hours"

        private const val COLOR_HENNA = 0xff908d46.toInt()
        private const val COLOR_REGISTRATION = 0xffe6e6e6.toInt()
        private const val COLOR_KEYNOTE = 0xfffdc93e.toInt()
        private const val COLOR_SESSIONS = 0xff73bbf5.toInt()
        private const val COLOR_MEAL = 0xff9bdd7c.toInt()
        private const val COLOR_AFTER_HOURS = 0xff202124.toInt()
    }

    private val blocks by lazy {
        listOf(
                Block(
                        title = LABEL_HENNA,
                        type = TYPE_HENNA,
                        color = COLOR_HENNA,
                        startTime = ZonedDateTime.parse("2019-03-11T10:00+05:30"),
                        endTime = ZonedDateTime.parse("2019-03-11T13:00+05:30")
                ),
                Block(
                        title = LABEL_RECEPTION_DINNER,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2019-03-11T19:30+05:30"),
                        endTime = ZonedDateTime.parse("2019-03-11T22:00+05:30")
                ),
                Block(
                        title = LABEL_BREAKFAST,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2018-11-07T08:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T10:00-08:00")
                ),
                Block(
                        title = LABEL_REGISTRATION,
                        type = TYPE_REGISTRATION,
                        color = COLOR_REGISTRATION,
                        startTime = ZonedDateTime.parse("2018-11-07T08:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T10:00-08:00")
                ),
                Block(
                        title = LABEL_KEYNOTE,
                        type = TYPE_KEYNOTE,
                        color = COLOR_KEYNOTE,
                        startTime = ZonedDateTime.parse("2018-11-07T10:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T11:00-08:00")
                ),
                Block(
                        title = LABEL_SESSIONS,
                        type = TYPE_SESSIONS,
                        color = COLOR_SESSIONS,
                        startTime = ZonedDateTime.parse("2018-11-07T11:15-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T12:45-08:00")
                ),
                Block(
                        title = LABEL_LUNCH,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2018-11-07T12:45-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T14:00-08:00")
                ),
                Block(
                        title = LABEL_SESSIONS,
                        type = TYPE_SESSIONS,
                        color = COLOR_SESSIONS,
                        startTime = ZonedDateTime.parse("2018-11-07T14:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T15:30-08:00")
                ),
                Block(
                        title = LABEL_TEA_BREAK,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2018-11-07T15:30-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T16:00-08:00")
                ),
                Block(
                        title = LABEL_SESSIONS,
                        type = TYPE_SESSIONS,
                        color = COLOR_SESSIONS,
                        startTime = ZonedDateTime.parse("2018-11-07T16:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T18:20-08:00")
                ),
                Block(
                        title = LABEL_PARTY,
                        type = TYPE_AFTER_HOURS,
                        color = COLOR_AFTER_HOURS,
                        isDark = true,
                        startTime = ZonedDateTime.parse("2018-11-07T18:20-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-07T22:20-08:00")
                ),
                Block(
                        title = LABEL_BREAKFAST,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2018-11-08T08:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T09:30-08:00")
                ),
                Block(
                        title = LABEL_REGISTRATION,
                        type = TYPE_REGISTRATION,
                        color = COLOR_REGISTRATION,
                        startTime = ZonedDateTime.parse("2018-11-08T08:00-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T09:30-08:00")
                ),
                Block(
                        title = LABEL_SESSIONS,
                        type = TYPE_SESSIONS,
                        color = COLOR_SESSIONS,
                        startTime = ZonedDateTime.parse("2018-11-08T09:30-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T11:50-08:00")
                ),
                Block(
                        title = LABEL_LUNCH,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2018-11-08T11:50-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T13:05-08:00")
                ),
                Block(
                        title = LABEL_SESSIONS,
                        type = TYPE_SESSIONS,
                        color = COLOR_SESSIONS,
                        startTime = ZonedDateTime.parse("2018-11-08T13:05-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T15:25-08:00")
                ),
                Block(
                        title = LABEL_TEA_BREAK,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2018-11-08T15:25-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T15:55-08:00")
                ),
                Block(
                        title = LABEL_SESSIONS,
                        type = TYPE_SESSIONS,
                        color = COLOR_SESSIONS,
                        startTime = ZonedDateTime.parse("2018-11-08T15:55-08:00"),
                        endTime = ZonedDateTime.parse("2018-11-08T17:25-08:00")
                )
        )
    }

    override fun getAgenda(): List<Block> = blocks
}
