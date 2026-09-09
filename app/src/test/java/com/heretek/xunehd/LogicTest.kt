package com.heretek.xunehd

import com.heretek.xunehd.data.model.PinKind
import com.heretek.xunehd.data.model.Rating
import com.heretek.xunehd.design.XuneAccent
import com.heretek.xunehd.design.components.firstLetterOf
import com.heretek.xunehd.ui.screens.formatTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {

    @Test
    fun `rating roundtrips through persistence values`() {
        assertEquals(Rating.NONE, Rating.from(0))
        assertEquals(Rating.HEART, Rating.from(1))
        assertEquals(Rating.BROKEN, Rating.from(2))
        assertEquals(Rating.NONE, Rating.from(99))
        assertEquals(3, Rating.entries.size) // tri-state: heart / broken / none
    }

    @Test
    fun `alphabet grouping handles junk`() {
        assertEquals('A', firstLetterOf("abba"))
        assertEquals('T', firstLetterOf(" The "))
        assertEquals('9', firstLetterOf("99 Luftballons"))
        assertEquals('#', firstLetterOf("!!!"))
        assertEquals('#', firstLetterOf(""))
        assertFalse(firstLetterOf("abba").isLowerCase())
    }

    @Test
    fun `time formats like the device`() {
        assertEquals("0:00", formatTime(0))
        assertEquals("0:59", formatTime(59_000))
        assertEquals("3:42", formatTime(222_000))
        assertEquals("61:02", formatTime(3_662_000))
    }

    @Test
    fun `accent ids are unique and pink is default`() {
        val ids = XuneAccent.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(0, XuneAccent.PINK.id)
        assertEquals(5, XuneAccent.entries.size)
    }

    @Test
    fun `pin kinds cover quickplay surfaces`() {
        assertTrue(PinKind.TRACK.name == "TRACK")
        assertTrue(PinKind.entries.size == 4)
    }
}
