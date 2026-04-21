package net.dodiya.signalman.ui.editrule

import android.util.Patterns
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
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase

@Suppress("TooManyFunctions")
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

    private val stateManager = RuleEditorStateManager(_uiState)

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
                    extractRuleData(rule)
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

    private fun extractRuleData(rule: Rule) {
        val (replacePattern, replacement, urlComponentReplacements) =
            when (val tm = rule.transformMode) {
                is TransformMode.Simple -> Triple(tm.replacePattern, tm.replacement, emptyList())
                is TransformMode.Advanced -> Triple("", "", tm.urlComponentReplacements)
            }
        val isTransformEnabled =
            when (val tm = rule.transformMode) {
                is TransformMode.Simple -> tm.replacePattern.isNotEmpty() || tm.replacement.isNotEmpty()
                is TransformMode.Advanced -> tm.urlComponentReplacements.any { it.replacement.isNotEmpty() }
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
    }

    fun onEvent(event: EditRuleEvent) {
        when (event) {
            is EditRuleEvent.NameChanged -> handleNameChanged(event.name)
            is EditRuleEvent.ExampleUrlChanged -> handleExampleUrlChanged(event.url)
            is EditRuleEvent.TargetPackageChanged -> handleTargetPackageChanged(event.packageName)
            is EditRuleEvent.FilterChanged -> handleFilterChanged(event.index, event.filter)
            is EditRuleEvent.FilterAdded -> handleFilterAdded(event.filter)
            is EditRuleEvent.FilterRemoved -> handleFilterRemoved(event.index)
            is EditRuleEvent.LogicalOperatorChanged -> handleLogicalOperatorChanged(event.operator)
            is EditRuleEvent.TransformEnabledChanged -> handleTransformEnabledChanged(event.enabled)
            is EditRuleEvent.ReplacePatternChanged -> handleReplacePatternChanged(event.pattern)
            is EditRuleEvent.ReplacementChanged -> handleReplacementChanged(event.replacement)
            is EditRuleEvent.UrlComponentReplacementChanged -> handleUrlComponentReplacementChanged(event.replacement)
            is EditRuleEvent.TransformModeChanged -> handleTransformModeChanged(event.mode)
            EditRuleEvent.SaveRule -> saveRule()
        }
        stateManager.validateAndSetSaveEnabled()
    }

    private fun handleNameChanged(name: String) {
        stateManager.updateBasicFields(name = name)
    }

    private fun handleExampleUrlChanged(url: String) {
        val processedUrl =
            if (url.isNotBlank() && !url.contains("://")) {
                "https://$url"
            } else {
                url
            }
        val isValid = processedUrl.isBlank() || Patterns.WEB_URL.matcher(processedUrl).matches()
        _uiState.update { it.copy(exampleUrl = processedUrl, isExampleUrlValid = isValid) }
        updatePreview()
    }

    private fun handleTargetPackageChanged(packageName: String) {
        stateManager.updateBasicFields(targetPackage = packageName)
    }

    private fun handleFilterChanged(
        index: Int,
        filter: Filter,
    ) {
        stateManager.updateFilter(index, filter)
        updatePreview()
    }

    private fun handleFilterAdded(filter: Filter) {
        stateManager.addFilter(filter)
        updatePreview()
    }

    private fun handleFilterRemoved(index: Int) {
        stateManager.removeFilter(index)
        updatePreview()
    }

    private fun handleLogicalOperatorChanged(operator: net.dodiya.signalman.data.LogicalOperator) {
        stateManager.updateLogicalOperator(operator)
        updatePreview()
    }

    private fun handleTransformEnabledChanged(enabled: Boolean) {
        stateManager.updateTransformConfig(enabled = enabled)
        updatePreview()
    }

    private fun handleReplacePatternChanged(pattern: String) {
        val currentState = _uiState.value
        stateManager.updateTransformPattern(pattern, currentState.replacement)
        updatePreview()
    }

    private fun handleReplacementChanged(replacement: String) {
        val currentState = _uiState.value
        stateManager.updateTransformPattern(currentState.replacePattern, replacement)
        updatePreview()
    }

    private fun handleUrlComponentReplacementChanged(replacement: List<net.dodiya.signalman.data.UrlComponentReplacement>) {
        stateManager.updateUrlComponentReplacement(replacement)
        updatePreview()
    }

    private fun handleTransformModeChanged(mode: TransformMode) {
        stateManager.updateTransformConfig(mode = mode)
        updatePreview()
    }

    private fun updatePreview() {
        val state = _uiState.value
        if (state.exampleUrl.isBlank() || !state.isExampleUrlValid) {
            _uiState.update { it.copy(isPreviewMatch = false, previewTransformedUrl = null, individualFilterMatches = emptyList()) }
            return
        }

        val rule = Rule(name = "Preview", filters = state.filters, logicalOperator = state.logicalOperator, targetPackage = "")
        val isMatch = matchRuleUseCase(state.exampleUrl, listOf(rule)).isNotEmpty()
        val individualMatches = computeIndividualMatches(state.exampleUrl, state.filters)
        val transformedUrl = computeTransformedUrl(state, isMatch, rule)

        _uiState.update {
            it.copy(isPreviewMatch = isMatch, previewTransformedUrl = transformedUrl, individualFilterMatches = individualMatches)
        }
    }

    private fun computeIndividualMatches(
        exampleUrl: String,
        filters: List<Filter>,
    ): List<Boolean?> =
        filters.map { filter ->
            if (filter.pattern.isBlank()) {
                null
            } else {
                val singleFilterRule = Rule(name = "FilterPreview", filters = listOf(filter), targetPackage = "")
                matchRuleUseCase(exampleUrl, listOf(singleFilterRule)).isNotEmpty()
            }
        }

    private fun computeTransformedUrl(
        state: EditRuleUiState,
        isMatch: Boolean,
        rule: Rule,
    ): String? {
        if (!state.isTransformEnabled || !isMatch) return null
        val transformMode =
            when (state.transformMode) {
                is TransformMode.Simple -> TransformMode.Simple(replacePattern = state.replacePattern, replacement = state.replacement)
                is TransformMode.Advanced -> TransformMode.Advanced(urlComponentReplacements = state.urlComponentReplacements)
            }
        return transformUrlUseCase(state.exampleUrl.toUri(), rule.copy(transformMode = transformMode)).toString()
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
