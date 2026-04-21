package net.dodiya.signalman.domain

import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.Rule
import java.util.regex.PatternSyntaxException

class MatchRuleUseCase {
    operator fun invoke(
        urlString: String,
        rules: List<Rule>,
    ): List<Rule> {
        return rules.filter { rule ->
            if (!rule.isEnabled) return@filter false
            if (rule.filters.isEmpty()) return@filter false

            val results =
                rule.filters.map { filter ->
                    try {
                        when (filter.matchType) {
                            MatchType.CONTAINS -> urlString.contains(filter.pattern, ignoreCase = true)
                            MatchType.EQUALS -> urlString.equals(filter.pattern, ignoreCase = true)
                            MatchType.STARTS_WITH -> urlString.startsWith(filter.pattern, ignoreCase = true)
                            MatchType.ENDS_WITH -> urlString.endsWith(filter.pattern, ignoreCase = true)
                            MatchType.REGEX -> {
                                try {
                                    Regex(filter.pattern, RegexOption.IGNORE_CASE).containsMatchIn(urlString)
                                } catch (e: PatternSyntaxException) {
                                    android.util.Log.e("MatchRuleUseCase", "Invalid Regex pattern: ${filter.pattern}", e)
                                    false
                                }
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        android.util.Log.e("MatchRuleUseCase", "Error matching filter: ${filter.matchType}", e)
                        false
                    }
                }

            if (rule.logicalOperator == LogicalOperator.AND) {
                results.all { it }
            } else {
                results.any { it }
            }
        }
    }
}
