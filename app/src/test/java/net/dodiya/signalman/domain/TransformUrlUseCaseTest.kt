package net.dodiya.signalman.domain

import android.net.Uri
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TransformUrlUseCaseTest {
    private val useCase = TransformUrlUseCase()

    @Test
    fun `test simple replacement`() {
        val rule =
            Rule(
                name = "Nitter",
                filters = listOf(Filter("x.com", MatchType.CONTAINS)),
                targetPackage = "net.nitter",
                isTransformEnabled = true,
                replacePattern = "x.com",
                replacement = "nitter.net",
            )
        val uri = Uri.parse("https://x.com/post/123")
        val result = useCase(uri, rule)
        assertEquals("https://nitter.net/post/123", result.toString())
    }

    @Test
    fun `test regex replacement with groups`() {
        val rule =
            Rule(
                name = "Regex Transform",
                filters = listOf(Filter("x.com", MatchType.REGEX)),
                targetPackage = "net.nitter",
                isTransformEnabled = true,
                replacePattern = "(.*)x\\.com/(.*)",
                replacement = "$1nitter.net/$2",
            )
        val uri = Uri.parse("https://x.com/user/status/123")
        val result = useCase(uri, rule)
        assertEquals("https://nitter.net/user/status/123", result.toString())
    }

    @Test
    fun `test no transform when disabled`() {
        val rule =
            Rule(
                name = "Disabled Transform",
                filters = listOf(Filter("x.com", MatchType.CONTAINS)),
                targetPackage = "net.nitter",
                isTransformEnabled = false,
                replacePattern = "x.com",
                replacement = "nitter.net",
            )
        val uri = Uri.parse("https://x.com/post/123")
        val result = useCase(uri, rule)
        assertEquals("https://x.com/post/123", result.toString())
    }
}
