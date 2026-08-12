package net.dodiya.signalman.ui.editrule

import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponentReplacement

data class EditRuleUiState(
    val name: String = "",
    val filters: List<Filter> = listOf(Filter("", MatchType.CONTAINS)),
    val logicalOperator: LogicalOperator = LogicalOperator.AND,
    val targetPackage: String? = null,
    val isCleanUrl: Boolean = false,
    val isTransformEnabled: Boolean = false,
    val replacePattern: String = "",
    val replacement: String = "",
    val urlComponentReplacements: List<UrlComponentReplacement> = emptyList(),
    val transformMode: TransformMode = TransformMode.Simple(),
    val exampleUrl: String = "",
    val isExampleUrlValid: Boolean = true,
    val isPreviewMatch: Boolean = false,
    val previewTransformedUrl: String? = null,
    val isSaveEnabled: Boolean = false,
    val individualFilterMatches: List<Boolean?> = emptyList(),
)

sealed class EditRuleEvent {
    data class NameChanged(
        val name: String,
    ) : EditRuleEvent()

    data class ExampleUrlChanged(
        val url: String,
    ) : EditRuleEvent()

    data class FilterChanged(
        val index: Int,
        val filter: Filter,
    ) : EditRuleEvent()

    data class FilterAdded(
        val filter: Filter,
    ) : EditRuleEvent()

    data class FilterRemoved(
        val index: Int,
    ) : EditRuleEvent()

    data class LogicalOperatorChanged(
        val operator: LogicalOperator,
    ) : EditRuleEvent()

    data class TargetPackageChanged(
        val packageName: String?,
    ) : EditRuleEvent()

    data class TransformEnabledChanged(
        val enabled: Boolean,
    ) : EditRuleEvent()

    data class CleanUrlChanged(
        val enabled: Boolean,
    ) : EditRuleEvent()

    data class ReplacePatternChanged(
        val pattern: String,
    ) : EditRuleEvent()

    data class ReplacementChanged(
        val replacement: String,
    ) : EditRuleEvent()

    data class UrlComponentReplacementChanged(
        val replacement: UrlComponentReplacement,
    ) : EditRuleEvent()

    data class TransformModeChanged(
        val mode: TransformMode,
    ) : EditRuleEvent()

    object SaveRule : EditRuleEvent()
}
