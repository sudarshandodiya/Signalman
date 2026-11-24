package net.dodiya.signalman.domain

import android.net.Uri
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CleanUrlUseCaseTest {
    private val useCase = CleanUrlUseCase()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `strips utm parameters but keeps others`() {
        val result = useCase(Uri.parse("https://example.com/?utm_source=newsletter&utm_medium=email&id=42"))
        assertEquals("https://example.com/?id=42", result.toString())
    }

    @Test
    fun `strips common tracking params`() {
        val result = useCase(Uri.parse("https://example.com/?fbclid=abc&gclid=xyz&msclkid=123&page=1"))
        assertEquals("https://example.com/?page=1", result.toString())
    }

    @Test
    fun `keeps non tracking params`() {
        val result = useCase(Uri.parse("https://example.com/watch?v=abc123&t=60"))
        assertEquals("https://example.com/watch?v=abc123&t=60", result.toString())
    }

    @Test
    fun `cleaning is case insensitive`() {
        val result = useCase(Uri.parse("https://example.com/?UTM_SOURCE=news&FBCLID=x&id=1"))
        assertEquals("https://example.com/?id=1", result.toString())
    }

    @Test
    fun `no query returns same uri`() {
        val uri = Uri.parse("https://example.com/path")
        assertEquals(uri, useCase(uri))
    }

    @Test
    fun `only tracking params leaves no query`() {
        val result = useCase(Uri.parse("https://example.com/?utm_source=x"))
        assertEquals("https://example.com/", result.toString())
    }

    @Test
    fun `preserves fragment path and remaining params`() {
        val result = useCase(Uri.parse("https://example.com/post/1?utm_source=x&ref=home#section"))
        assertEquals("https://example.com/post/1?ref=home#section", result.toString())
    }

    @Test
    fun `preserves param order of kept params`() {
        val result = useCase(Uri.parse("https://example.com/?a=1&utm_source=x&b=2&utm_medium=y&c=3"))
        assertEquals("https://example.com/?a=1&b=2&c=3", result.toString())
    }
}
