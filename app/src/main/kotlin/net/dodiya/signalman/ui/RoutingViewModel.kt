package net.dodiya.signalman.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.domain.CleanUrlUseCase
import net.dodiya.signalman.domain.CreateAutoRuleUseCase
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase

sealed class RoutingEvent {
    data class RouteToApp(
        val uri: Uri,
        val targetPackage: String?,
    ) : RoutingEvent()

    data class ShowOverlay(
        val uri: Uri,
        val matchedRules: List<Rule>,
        val hiddenBrowsers: Set<String> = emptySet(),
    ) : RoutingEvent()

    object Finish : RoutingEvent()
}

class RoutingViewModel(
    private val repository: RuleRepository,
    private val preferenceManager: PreferenceManager,
    private val matchRuleUseCase: MatchRuleUseCase,
    private val transformUrlUseCase: TransformUrlUseCase,
    private val createAutoRuleUseCase: CreateAutoRuleUseCase,
    private val cleanUrlUseCase: CleanUrlUseCase,
) : ViewModel() {
    private val _events = MutableSharedFlow<RoutingEvent>()
    val events: SharedFlow<RoutingEvent> = _events.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun handleIncomingUrl(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // Global setting: clean tracking parameters before matching and routing.
                val isGlobalCleanEnabled = preferenceManager.isCleanUrlsEnabled.first()
                val baseUri = if (isGlobalCleanEnabled) cleanUrlUseCase(uri) else uri
                val urlString = baseUri.toString()
                val rules = repository.cachedRules
                val matchedRules = matchRuleUseCase(urlString, rules)
                val hiddenBrowsers = preferenceManager.hiddenBrowsers.first()

                when {
                    matchedRules.size == 1 -> {
                        val rule = matchedRules[0]
                        val finalUri = if (rule.isCleanUrl) cleanUrlUseCase(baseUri) else baseUri
                        val routedUri = transformUrlUseCase(finalUri, rule)
                        _events.emit(RoutingEvent.RouteToApp(routedUri, rule.targetPackage))
                    }
                    matchedRules.size > 1 -> {
                        _events.emit(RoutingEvent.ShowOverlay(baseUri, matchedRules, hiddenBrowsers))
                    }
                    else -> {
                        val isAutoEnabled = preferenceManager.isAutoRuleGenerationEnabled.first()
                        if (isAutoEnabled) {
                            _events.emit(RoutingEvent.ShowOverlay(baseUri, emptyList(), hiddenBrowsers))
                        } else {
                            val globalDefault = preferenceManager.globalDefaultPackage.first()
                            _events.emit(RoutingEvent.RouteToApp(baseUri, globalDefault))
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalStateException) {
                // Log and fallback to system default
                android.util.Log.e("RoutingViewModel", "Error handling URL", e)
                _events.emit(RoutingEvent.RouteToApp(uri, null))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun applyRule(
        uri: Uri,
        rule: Rule,
    ) {
        viewModelScope.launch {
            val finalUri = if (rule.isCleanUrl) cleanUrlUseCase(uri) else uri
            val routedUri = transformUrlUseCase(finalUri, rule)
            _events.emit(RoutingEvent.RouteToApp(routedUri, rule.targetPackage))
        }
    }

    fun handleSelection(
        uri: Uri,
        pkgName: String,
        mode: Int,
    ) {
        viewModelScope.launch {
            createAutoRuleUseCase(uri, pkgName, mode)
            _events.emit(RoutingEvent.RouteToApp(uri, pkgName))
        }
    }
}
