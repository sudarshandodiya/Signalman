package net.dodiya.signalman.domain

import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchRuleUseCaseTest {
    private val useCase = MatchRuleUseCase()

    @Test
    fun `test contains match`() {
        val rules =
            listOf(
                Rule(
                    name = "Test Rule",
                    filters = listOf(Filter("google.com", MatchType.CONTAINS)),
                    targetPackage = "com.android.chrome",
                ),
            )
        val result = useCase("https://www.google.com/search", rules)
        assertEquals(1, result.size)
        assertEquals("Test Rule", result[0].name)
    }

    @Test
    fun `test equals match`() {
        val rules =
            listOf(
                Rule(
                    name = "Exact Match",
                    filters = listOf(Filter("https://example.com", MatchType.EQUALS)),
                    targetPackage = "com.example.app",
                ),
            )
        val result = useCase("https://example.com", rules)
        assertEquals(1, result.size)

        val noResult = useCase("https://example.com/path", rules)
        assertTrue(noResult.isEmpty())
    }

    @Test
    fun `test regex match`() {
        val rules =
            listOf(
                Rule(
                    name = "Regex Rule",
                    filters = listOf(Filter(".*google\\..*", MatchType.REGEX)),
                    targetPackage = "com.android.chrome",
                ),
            )
        val result = useCase("https://google.co.uk", rules)
        assertEquals(1, result.size)
    }

    @Test
    fun `test AND operator`() {
        val rules =
            listOf(
                Rule(
                    name = "AND Rule",
                    filters =
                        listOf(
                            Filter("google", MatchType.CONTAINS),
                            Filter("search", MatchType.CONTAINS),
                        ),
                    logicalOperator = LogicalOperator.AND,
                    targetPackage = "com.android.chrome",
                ),
            )
        val match = useCase("https://google.com/search", rules)
        assertEquals(1, match.size)

        val noMatch = useCase("https://google.com/maps", rules)
        assertTrue(noMatch.isEmpty())
    }

    @Test
    fun `test OR operator`() {
        val rules =
            listOf(
                Rule(
                    name = "OR Rule",
                    filters =
                        listOf(
                            Filter("google", MatchType.CONTAINS),
                            Filter("bing", MatchType.CONTAINS),
                        ),
                    logicalOperator = LogicalOperator.OR,
                    targetPackage = "com.android.chrome",
                ),
            )
        assertEquals(1, useCase("https://google.com", rules).size)
        assertEquals(1, useCase("https://bing.com", rules).size)
        assertTrue(useCase("https://yahoo.com", rules).isEmpty())
    }

    @Test
    fun `test disabled rule`() {
        val rules =
            listOf(
                Rule(
                    name = "Disabled Rule",
                    filters = listOf(Filter("google", MatchType.CONTAINS)),
                    isEnabled = false,
                    targetPackage = "com.android.chrome",
                ),
            )
        val result = useCase("https://google.com", rules)
        assertTrue(result.isEmpty())
    }
}
