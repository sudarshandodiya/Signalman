package net.dodiya.signalman.ui.editrule

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.dodiya.signalman.data.AppInfoRepository
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.LogicalOperator
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponentReplacement
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase

data class EditRuleUiState(
    val name: String = "",
    val filters: List<Filter> = listOf(Filter("", MatchType.CONTAINS)),
    val logicalOperator: LogicalOperator = LogicalOperator.AND,
    val targetPackage: String? = null,
    val isTransformEnabled: Boolean = false,
    val replacePattern: String = "",
    val replacement: String = "",
    val urlComponentReplacements: List<UrlComponentReplacement> = emptyList(),
    val transformMode: TransformMode = TransformMode.Simple(),
    val exampleUrl: String = "",
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

class EditRuleViewModel(
    private val repository: RuleRepository,
    private val matchRuleUseCase: MatchRuleUseCase,
    private val transformUrlUseCase: TransformUrlUseCase,
    private val appInfoRepository: AppInfoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val ruleId: Int = savedStateHandle.get<Int>("ruleId") ?: -1
    private val initialName: String? = savedStateHandle.get<String>("name")
    private val initialExampleUrl: String? = savedStateHandle.get<String>("exampleUrl")

    private val _uiState = MutableStateFlow(EditRuleUiState())
    val uiState: StateFlow<EditRuleUiState> = _uiState.asStateFlow()

    val installedApps: StateFlow<List<net.dodiya.signalman.data.AppInfo>> =
        appInfoRepository.installedApps
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList(),
            )

    init {
        loadRule()
        viewModelScope.launch {
            appInfoRepository.loadInstalledApps()
        }
    }

    private fun loadRule() {
        if (ruleId != -1) {
            viewModelScope.launch {
                repository.getRule(ruleId)?.let { rule ->
                    val (replacePattern, replacement, urlComponentReplacements) =
                        when (val tm = rule.transformMode) {
                            is TransformMode.Simple ->
                                Triple(
                                    tm.replacePattern,
                                    tm.replacement,
                                    emptyList<UrlComponentReplacement>(),
                                )
                            is TransformMode.Advanced ->
                                Triple(
                                    "",
                                    "",
                                    tm.urlComponentReplacements,
                                )
                        }
                    val isTransformEnabled =
                        when (val tm = rule.transformMode) {
                            is TransformMode.Simple ->
                                tm.replacePattern.isNotEmpty() || tm.replacement.isNotEmpty()
                            is TransformMode.Advanced ->
                                tm.urlComponentReplacements.any { it.replacement.isNotEmpty() }
                        }
                    _uiState.update { currentState ->
                        currentState.copy(
                            name = rule.name,
                            filters = rule.filters.ifEmpty { listOf(Filter("", MatchType.CONTAINS)) },
                            logicalOperator = rule.logicalOperator,
                            targetPackage = rule.targetPackage,
                            isTransformEnabled = isTransformEnabled,
                            replacePattern = replacePattern,
                            replacement = replacement,
                            urlComponentReplacements = urlComponentReplacements,
                            transformMode = rule.transformMode,
                            exampleUrl = rule.exampleUrl ?: initialExampleUrl ?: "",
                        )
                    }
                    updatePreview()
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    name = initialName ?: "",
                    exampleUrl = initialExampleUrl ?: "",
                )
            }
            updatePreview()
        }
    }

    fun onEvent(event: EditRuleEvent) {
        when (event) {
            is EditRuleEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name) }
            }
            is EditRuleEvent.ExampleUrlChanged -> {
                _uiState.update { it.copy(exampleUrl = event.url) }
                updatePreview()
            }
            is EditRuleEvent.FilterChanged -> {
                _uiState.update {
                    val newFilters = it.filters.toMutableList()
                    if (event.index in newFilters.indices) {
                        newFilters[event.index] = event.filter
                    }
                    it.copy(filters = newFilters)
                }
                updatePreview()
            }
            is EditRuleEvent.FilterAdded -> {
                _uiState.update { it.copy(filters = it.filters + event.filter) }
                updatePreview()
            }
            is EditRuleEvent.FilterRemoved -> {
                _uiState.update {
                    val newFilters = it.filters.toMutableList()
                    if (event.index in newFilters.indices) {
                        newFilters.removeAt(event.index)
                    }
                    it.copy(filters = newFilters)
                }
                updatePreview()
            }
            is EditRuleEvent.LogicalOperatorChanged -> {
                _uiState.update { it.copy(logicalOperator = event.operator) }
                updatePreview()
            }
            is EditRuleEvent.TargetPackageChanged -> {
                _uiState.update { it.copy(targetPackage = event.packageName) }
            }
            is EditRuleEvent.TransformEnabledChanged -> {
                _uiState.update { it.copy(isTransformEnabled = event.enabled) }
                updatePreview()
            }
            is EditRuleEvent.ReplacePatternChanged -> {
                _uiState.update { it.copy(replacePattern = event.pattern) }
                updatePreview()
            }
            is EditRuleEvent.ReplacementChanged -> {
                _uiState.update { it.copy(replacement = event.replacement) }
                updatePreview()
            }
            is EditRuleEvent.UrlComponentReplacementChanged -> {
                _uiState.update { state ->
                    val currentReplacements = state.urlComponentReplacements.toMutableList()
                    val index = currentReplacements.indexOfFirst { it.component == event.replacement.component }
                    if (index >= 0) {
                        if (event.replacement.replacement.isEmpty() && !event.replacement.isEnabled) {
                            currentReplacements.removeAt(index)
                        } else {
                            currentReplacements[index] = event.replacement
                        }
                    } else if (event.replacement.isEnabled || event.replacement.replacement.isNotEmpty()) {
                        currentReplacements.add(event.replacement)
                    }
                    state.copy(urlComponentReplacements = currentReplacements)
                }
                updatePreview()
            }
            is EditRuleEvent.TransformModeChanged -> {
                _uiState.update { it.copy(transformMode = event.mode) }
                updatePreview()
            }
            EditRuleEvent.SaveRule -> {
                saveRule()
            }
        }
        validateSave()
    }

    private fun updatePreview() {
        val state = _uiState.value
        if (state.exampleUrl.isBlank()) {
            _uiState.update { it.copy(isPreviewMatch = false, previewTransformedUrl = null, individualFilterMatches = emptyList()) }
            return
        }

        val rule =
            Rule(
                name = "Preview",
                filters = state.filters,
                logicalOperator = state.logicalOperator,
                targetPackage = "",
            )
        val isMatch = matchRuleUseCase(state.exampleUrl, listOf(rule)).isNotEmpty()

        val individualMatches =
            state.filters.map { filter ->
                if (filter.pattern.isBlank()) {
                    null
                } else {
                    val singleFilterRule =
                        Rule(
                            name = "FilterPreview",
                            filters = listOf(filter),
                            targetPackage = "",
                        )
                    matchRuleUseCase(state.exampleUrl, listOf(singleFilterRule)).isNotEmpty()
                }
            }

        var transformedUrl: String? = null
        if (state.isTransformEnabled && isMatch) {
            val transformMode =
                when (state.transformMode) {
                    is TransformMode.Simple ->
                        TransformMode.Simple(
                            replacePattern = state.replacePattern,
                            replacement = state.replacement,
                        )
                    is TransformMode.Advanced ->
                        TransformMode.Advanced(
                            urlComponentReplacements = state.urlComponentReplacements,
                        )
                }
            val transformRule = rule.copy(transformMode = transformMode)
            transformedUrl = transformUrlUseCase(state.exampleUrl.toUri(), transformRule).toString()
        }

        _uiState.update {
            it.copy(
                isPreviewMatch = isMatch,
                previewTransformedUrl = transformedUrl,
                individualFilterMatches = individualMatches,
            )
        }
    }

    private fun validateSave() {
        val state = _uiState.value
        val isValid = state.name.isNotBlank() && state.filters.all { it.pattern.isNotBlank() }
        _uiState.update { it.copy(isSaveEnabled = isValid) }
    }

    private fun saveRule() {
        viewModelScope.launch {
            val state = _uiState.value
            val transformMode =
                when (state.transformMode) {
                    is TransformMode.Simple ->
                        TransformMode.Simple(
                            replacePattern = state.replacePattern,
                            replacement = state.replacement,
                        )
                    is TransformMode.Advanced ->
                        TransformMode.Advanced(
                            urlComponentReplacements = state.urlComponentReplacements,
                        )
                }
            val rule =
                Rule(
                    id = if (ruleId == -1) 0 else ruleId,
                    name = state.name,
                    filters = state.filters,
                    logicalOperator = state.logicalOperator,
                    targetPackage = state.targetPackage ?: "",
                    transformMode = transformMode,
                    exampleUrl = state.exampleUrl.ifEmpty { null },
                )
            if (ruleId == -1) {
                repository.insert(rule)
            } else {
                repository.update(rule)
            }
        }
    }
}
