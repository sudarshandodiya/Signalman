package net.dodiya.signalman.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareIntentParserTest {
    @Test
    fun `extracts plain https url`() {
        assertEquals("https://example.com/path?q=1", extractSharedUrl("https://example.com/path?q=1"))
    }

    @Test
    fun `extracts url from surrounding text`() {
        assertEquals("http://example.com", extractSharedUrl("Look at this: http://example.com."))
    }

    @Test
    fun `adds scheme to bare domain`() {
        assertEquals("https://www.example.com/post/1", extractSharedUrl("www.example.com/post/1"))
    }

    @Test
    fun `returns null when there is no url`() {
        assertNull(extractSharedUrl("Just a note without a link"))
        assertNull(extractSharedUrl(null))
        assertNull(extractSharedUrl("  "))
    }
}
