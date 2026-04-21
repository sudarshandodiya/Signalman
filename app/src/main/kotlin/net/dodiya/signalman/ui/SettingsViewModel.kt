package net.dodiya.signalman.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.RuleExporter
import net.dodiya.signalman.data.RuleRepository

sealed class ImportExportState {
    object Idle : ImportExportState()

    object Loading : ImportExportState()

    data class Success(
        val message: String,
    ) : ImportExportState()

    data class Error(
        val message: String,
    ) : ImportExportState()
}

class SettingsViewModel(
    private val preferenceManager: PreferenceManager,
    private val ruleRepository: RuleRepository,
    private val context: Context,
) : ViewModel() {
    val hiddenBrowsers: StateFlow<Set<String>> =
        preferenceManager.hiddenBrowsers
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet(),
            )

    private val _importExportState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val importExportState: StateFlow<ImportExportState> = _importExportState.asStateFlow()

    private val _pendingImportRules = MutableStateFlow<List<Rule>?>(null)
    val pendingImportRules: StateFlow<List<Rule>?> = _pendingImportRules.asStateFlow()

    private val _pendingImportUri = MutableStateFlow<Uri?>(null)
    val pendingImportUri: StateFlow<Uri?> = _pendingImportUri.asStateFlow()

    fun setHiddenBrowsers(hidden: Set<String>) {
        viewModelScope.launch {
            preferenceManager.setHiddenBrowsers(hidden)
        }
    }

    fun exportRules(uri: Uri) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            try {
                ruleRepository.allRules.collect { rules ->
                    if (rules.isEmpty()) {
                        _importExportState.value = ImportExportState.Error("No rules to export")
                        return@collect
                    }

                    val json = RuleExporter.exportRules(rules)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                    _importExportState.value = ImportExportState.Success("Exported ${rules.size} rules successfully")
                    return@collect
                }
            } catch (e: java.io.IOException) {
                android.util.Log.e("SettingsViewModel", "Export failed", e)
                _importExportState.value = ImportExportState.Error("Export failed: ${e.message}")
            } catch (e: SecurityException) {
                android.util.Log.e("SettingsViewModel", "Export failed: Permission denied", e)
                _importExportState.value = ImportExportState.Error("Export failed: Permission denied")
            }
        }
    }

    fun loadImportPreview(uri: Uri) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            try {
                val json =
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().readText()
                    } ?: throw java.io.IOException("Could not read file")

                val rules = RuleExporter.importRules(json)
                if (rules.isNullOrEmpty()) {
                    _importExportState.value = ImportExportState.Error("Invalid file format or no rules found")
                    return@launch
                }

                _pendingImportUri.value = uri
                _pendingImportRules.value = rules
                _importExportState.value = ImportExportState.Idle
            } catch (e: java.io.IOException) {
                android.util.Log.e("SettingsViewModel", "Import preview failed", e)
                _importExportState.value = ImportExportState.Error("Import failed: ${e.message}")
            } catch (e: SecurityException) {
                android.util.Log.e("SettingsViewModel", "Import preview failed: Permission denied", e)
                _importExportState.value = ImportExportState.Error("Import failed: Permission denied")
            }
        }
    }

    fun importSelectedRules(rules: List<Rule>) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            try {
                ruleRepository.insertAll(rules)
                _importExportState.value = ImportExportState.Success("Imported ${rules.size} rules successfully")
                _pendingImportRules.value = null
                _pendingImportUri.value = null
            } catch (e: java.io.IOException) {
                android.util.Log.e("SettingsViewModel", "Import failed", e)
                _importExportState.value = ImportExportState.Error("Import failed: ${e.message}")
            } catch (e: SecurityException) {
                android.util.Log.e("SettingsViewModel", "Import failed: Permission denied", e)
                _importExportState.value = ImportExportState.Error("Import failed: Permission denied")
            }
        }
    }

    fun importAllRules(replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            try {
                val uri = _pendingImportUri.value ?: throw java.io.IOException("No file selected")

                val json =
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().readText()
                    } ?: throw java.io.IOException("Could not read file")

                val rules = RuleExporter.importRules(json)
                if (rules.isNullOrEmpty()) {
                    _importExportState.value = ImportExportState.Error("Invalid file format or no rules found")
                    return@launch
                }

                if (replaceExisting) {
                    ruleRepository.deleteAll()
                }

                ruleRepository.insertAll(rules)
                _importExportState.value = ImportExportState.Success("Imported ${rules.size} rules successfully")
                _pendingImportRules.value = null
                _pendingImportUri.value = null
            } catch (e: java.io.IOException) {
                android.util.Log.e("SettingsViewModel", "Import failed", e)
                _importExportState.value = ImportExportState.Error("Import failed: ${e.message}")
            } catch (e: SecurityException) {
                android.util.Log.e("SettingsViewModel", "Import failed: Permission denied", e)
                _importExportState.value = ImportExportState.Error("Import failed: Permission denied")
            }
        }
    }

    fun resetImportExportState() {
        _importExportState.value = ImportExportState.Idle
    }

    fun clearPendingImportRules() {
        _pendingImportRules.value = null
        _pendingImportUri.value = null
    }
}
