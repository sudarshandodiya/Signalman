package net.dodiya.signalman.ui.editrule

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponentReplacement

/**
 * Handles UI state updates for the rule editor.
 * This class is responsible for all state mutations based on user interactions.
 */
class RuleEditorStateManager(
    private val uiState: MutableStateFlow<EditRuleUiState>,
) {
    // Basic field updates
    fun updateBasicFields(
        name: String? = null,
        exampleUrl: String? = null,
        targetPackage: String? = null,
    ) {
        uiState.update {
            it.copy(
                name = name ?: it.name,
                exampleUrl = exampleUrl ?: it.exampleUrl,
                targetPackage = targetPackage ?: it.targetPackage,
            )
        }
    }

    // Filter management
    fun updateFilter(
        index: Int,
        filter: Filter,
    ) {
        uiState.update {
            val newFilters = it.filters.toMutableList()
            if (index in newFilters.indices) {
                newFilters[index] = filter
            }
            it.copy(filters = newFilters)
        }
    }

    fun addFilter(filter: Filter) {
        uiState.update { it.copy(filters = it.filters + filter) }
    }

    fun removeFilter(index: Int) {
        uiState.update {
            val newFilters = it.filters.toMutableList()
            if (index in newFilters.indices) {
                newFilters.removeAt(index)
            }
            it.copy(filters = newFilters)
        }
    }

    fun updateLogicalOperator(operator: LogicalOperator) {
        uiState.update { it.copy(logicalOperator = operator) }
    }

    // Transform management
    fun updateTransformConfig(
        enabled: Boolean? = null,
        mode: TransformMode? = null,
    ) {
        uiState.update {
            it.copy(
                isTransformEnabled = enabled ?: it.isTransformEnabled,
                transformMode = mode ?: it.transformMode,
            )
        }
    }

    fun updateTransformPattern(
        pattern: String,
        replacement: String,
    ) {
        uiState.update { it.copy(replacePattern = pattern, replacement = replacement) }
    }

    fun updateUrlComponentReplacement(replacement: UrlComponentReplacement) {
        uiState.update { state ->
            val currentReplacements = state.urlComponentReplacements.toMutableList()
            val index = currentReplacements.indexOfFirst { it.component == replacement.component }
            val updatedReplacements =
                when {
                    index >= 0 && replacement.replacement.isEmpty() && !replacement.isEnabled -> {
                        currentReplacements.apply { removeAt(index) }
                    }
                    index >= 0 -> currentReplacements.apply { this[index] = replacement }
                    replacement.isEnabled || replacement.replacement.isNotEmpty() -> {
                        currentReplacements.apply { add(replacement) }
                    }
                    else -> currentReplacements
                }
            state.copy(urlComponentReplacements = updatedReplacements)
        }
    }

    // Validation
    fun validateAndSetSaveEnabled() {
        val state = uiState.value
        val isValid = state.name.isNotBlank() && state.filters.all { it.pattern.isNotBlank() }
        uiState.update { it.copy(isSaveEnabled = isValid) }
    }
}
