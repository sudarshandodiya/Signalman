package net.dodiya.signalman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.data.AppInfoRepository
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase

class RuleViewModel(
    private val appInfoRepository: AppInfoRepository,
    private val repository: RuleRepository,
    private val preferenceManager: PreferenceManager,
    val matchRuleUseCase: MatchRuleUseCase,
    val transformUrlUseCase: TransformUrlUseCase,
) : ViewModel() {
    val allRules: StateFlow<List<Rule>>

    companion object {
        private const val SUBSCRIBE_TIMEOUT_MS = 5000L
    }

    val installedApps: StateFlow<List<AppInfo>> =
        appInfoRepository.installedApps
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
                initialValue = emptyList(),
            )

    val globalDefaultPackage: StateFlow<String?> =
        preferenceManager.globalDefaultPackage
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
                initialValue = null,
            )

    val isAutoRuleGenerationEnabled: StateFlow<Boolean> =
        preferenceManager.isAutoRuleGenerationEnabled
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
                initialValue = true,
            )

    init {
        allRules =
            repository.allRules.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
                initialValue = emptyList(),
            )
        viewModelScope.launch {
            appInfoRepository.loadInstalledApps()
        }
    }

    fun setGlobalDefault(packageName: String?) {
        viewModelScope.launch {
            preferenceManager.setGlobalDefault(packageName)
        }
    }

    fun setAutoRuleGenerationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setAutoRuleGenerationEnabled(enabled)
        }
    }

    fun insert(rule: Rule) =
        viewModelScope.launch {
            repository.insert(rule)
        }

    fun update(rule: Rule) =
        viewModelScope.launch {
            repository.update(rule)
        }

    fun delete(rule: Rule) =
        viewModelScope.launch {
            repository.delete(rule)
        }
}
