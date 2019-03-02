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
        private const val LABEL_RECEPTION_LUNCH = "Reception & Lunch"
        private const val LABEL_BARANUGAMANA = "Baranugamana"
        private const val LABEL_HASTAGRANTHI = "Hastagranthi"

        private const val TYPE_HENNA = "henna"
        private const val TYPE_BARANUGAMANA = "baranugamana"
        private const val TYPE_HASTAGRANTHI = "handshake"
        private const val TYPE_MEAL = "meal"

        private const val COLOR_HENNA = 0xff908d46.toInt()
        private const val COLOR_KEYNOTE = 0xfffdc93e.toInt()
        private const val COLOR_MEAL = 0xff9bdd7c.toInt()
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
                        title = LABEL_RECEPTION_LUNCH,
                        type = TYPE_MEAL,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2019-03-13T13:30+05:30"),
                        endTime = ZonedDateTime.parse("2019-03-13T15:30+05:30")
                ),
                Block(
                        title = LABEL_BARANUGAMANA,
                        type = TYPE_BARANUGAMANA,
                        color = COLOR_MEAL,
                        startTime = ZonedDateTime.parse("2019-03-13T17:30+05:30"),
                        endTime = ZonedDateTime.parse("2019-03-13T19:30+05:30")
                ),
                Block(
                        title = LABEL_HASTAGRANTHI,
                        type = TYPE_HASTAGRANTHI,
                        color = COLOR_KEYNOTE,
                        startTime = ZonedDateTime.parse("2019-03-13T23:03+05:30"),
                        endTime = ZonedDateTime.parse("2019-03-13T23:18+05:30")
                )
        )
    }

    override fun getAgenda(): List<Block> = blocks
}
