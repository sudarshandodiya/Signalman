package net.dodiya.signalman.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "signalman_prefs")

class PreferenceManager(
    private val context: Context,
) {
    companion object {
        private val KEY_GLOBAL_DEFAULT_PACKAGE = stringPreferencesKey("global_default_package")
        private val KEY_AUTO_RULE_GENERATION_ENABLED = booleanPreferencesKey("auto_rule_generation_enabled")
        private val KEY_HIDDEN_BROWSERS = stringSetPreferencesKey("hidden_browsers")
    }

    val hiddenBrowsers: Flow<Set<String>> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }.map { preferences ->
                preferences[KEY_HIDDEN_BROWSERS] ?: emptySet()
            }

    suspend fun setHiddenBrowsers(hidden: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIDDEN_BROWSERS] = hidden
        }
    }

    val globalDefaultPackage: Flow<String?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }.map { preferences ->
                preferences[KEY_GLOBAL_DEFAULT_PACKAGE]
            }

    val isAutoRuleGenerationEnabled: Flow<Boolean> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }.map { preferences ->
                preferences[KEY_AUTO_RULE_GENERATION_ENABLED] ?: true
            }

    suspend fun setGlobalDefault(packageName: String?) {
        context.dataStore.edit { preferences ->
            if (packageName == null) {
                preferences.remove(KEY_GLOBAL_DEFAULT_PACKAGE)
            } else {
                preferences[KEY_GLOBAL_DEFAULT_PACKAGE] = packageName
            }
        }
    }

    suspend fun setAutoRuleGenerationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_RULE_GENERATION_ENABLED] = enabled
        }
    }
}
