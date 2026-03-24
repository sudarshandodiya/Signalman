package net.dodiya.signalman.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.dodiya.signalman.data.AppInfo
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase

class RuleViewModel(
    private val application: Application,
    private val repository: RuleRepository,
    private val preferenceManager: PreferenceManager,
    val matchRuleUseCase: MatchRuleUseCase,
    val transformUrlUseCase: TransformUrlUseCase,
) : ViewModel() {
    val allRules: StateFlow<List<Rule>>

    companion object {
        private const val SUBSCRIBE_TIMEOUT_MS = 5000L
    }

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

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
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = application.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

            val apps =
                resolveInfos
                    .map {
                        AppInfo(
                            name = it.loadLabel(pm).toString(),
                            packageName = it.activityInfo.packageName,
                            icon = it.loadIcon(pm),
                        )
                    }.distinctBy { it.packageName }
                    .sortedBy { it.name }

            _installedApps.value = apps
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

    fun setAlwaysChoiceForUrl(
        url: String,
        packageName: String,
    ) {
        viewModelScope.launch {
            preferenceManager.setAlwaysChoiceForUrl(url, packageName)
        }
    }

    fun setAlwaysChoiceForDomain(
        domain: String,
        packageName: String,
    ) {
        viewModelScope.launch {
            preferenceManager.setAlwaysChoiceForDomain(domain, packageName)
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
