package net.dodiya.signalman

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.dodiya.signalman.ui.EditRuleScreen
import net.dodiya.signalman.ui.EditRuleViewModel
import net.dodiya.signalman.ui.ImportExportScreen
import net.dodiya.signalman.ui.OverlayChooser
import net.dodiya.signalman.ui.RoutingEvent
import net.dodiya.signalman.ui.RoutingViewModel
import net.dodiya.signalman.ui.RuleListScreen
import net.dodiya.signalman.ui.RuleViewModel
import net.dodiya.signalman.ui.SettingsScreen
import net.dodiya.signalman.ui.SettingsViewModel
import net.dodiya.signalman.ui.theme.SignalmanTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    private val viewModel: RuleViewModel by viewModel()
    private val routingViewModel: RoutingViewModel by viewModel()

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeRoutingEvents()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val intentData: Uri? = intent.data
        if (intent.action == Intent.ACTION_VIEW && intentData != null) {
            routingViewModel.handleIncomingUrl(intentData)
        }
    }

    private fun observeRoutingEvents() {
        lifecycleScope.launch {
            val event = withTimeoutOrNull(2000L) {
                routingViewModel.events.first()
            }
            when (event) {
                is RoutingEvent.RouteToApp -> {
                    routeUrl(event.uri, event.targetPackage)
                    finish()
                }
                is RoutingEvent.ShowOverlay -> {
                    showOverlayUi(event.uri, event.matchedRules, event.hiddenBrowsers)
                }
                RoutingEvent.Finish -> finish()
                null -> showUi()
            }
        }
    }

    private fun routeUrl(
        uri: Uri,
        targetPackage: String?,
    ) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            targetPackage?.takeIf { it.isNotBlank() }?.let { setPackage(it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        try {
            startActivity(intent)
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } catch (e: ActivityNotFoundException) {
            android.util.Log.e(TAG, "Error routing URL to package: $targetPackage", e)
            try {
                intent.setPackage(null) // Strip the package restriction for the fallback
                startActivity(Intent.createChooser(intent, "Open with"))
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            } catch (fallbackException: ActivityNotFoundException) {
                android.util.Log.e(TAG, "Critical failure: Could not even open with chooser", fallbackException)
            }
        }
    }

    private fun showOverlayUi(
        uri: Uri,
        matchedRules: List<net.dodiya.signalman.data.Rule>,
        hiddenBrowsers: Set<String> = emptySet(),
    ) {
        val pm = packageManager

        val urlIntent = Intent(Intent.ACTION_VIEW, uri)
        val activities =
            pm
                .queryIntentActivities(urlIntent, PackageManager.MATCH_ALL)
                .filter { it.activityInfo.packageName != packageName }
                .filter { !hiddenBrowsers.contains(it.activityInfo.packageName) }

        setContent {
            SignalmanTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    OverlayChooser(
                        uri = uri,
                        matchedRules = matchedRules,
                        browserActivities = activities,
                        onRuleSelection = { rule ->
                            routingViewModel.applyRule(uri, rule)
                        },
                        onAppSelection = { pkgName, mode ->
                            routingViewModel.handleSelection(uri, pkgName, mode)
                        },
                        onDismiss = { finish() },
                    )
                }
            }
        }
    }

    private fun showUi() {
        setContent {
            SignalmanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SignalmanApp(viewModel)
                }
            }
        }
    }

    @Composable
    private fun SignalmanApp(viewModel: RuleViewModel) {
        val navController = rememberNavController()
        val settingsViewModel: SettingsViewModel = koinViewModel()
        NavHost(navController = navController, startDestination = "ruleList") {
            composable("ruleList") {
                RuleListScreen(
                    viewModel = viewModel,
                    onAddRule = { name, exampleUrl ->
                        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                        val encodedUrl =
                            if (exampleUrl.isNotBlank()) {
                                URLEncoder.encode(
                                    exampleUrl,
                                    StandardCharsets.UTF_8.toString(),
                                )
                            } else {
                                null
                            }
                        val route =
                            if (encodedUrl != null) {
                                "editRule/-1?name=$encodedName&exampleUrl=$encodedUrl"
                            } else {
                                "editRule/-1?name=$encodedName"
                            }
                        navController.navigate(route)
                    },
                    onEditRule = { ruleId -> navController.navigate("editRule/$ruleId") },
                    onNavigateToSettings = { navController.navigate("settings") },
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToImportExport = { navController.navigate("importExport") },
                    viewModel = settingsViewModel,
                )
            }
            composable("importExport") {
                ImportExportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = settingsViewModel,
                )
            }
            composable(
                "editRule/{ruleId}?name={name}&exampleUrl={exampleUrl}",
                arguments =
                    listOf(
                        navArgument("ruleId") { type = NavType.IntType },
                        navArgument("name") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("exampleUrl") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
            ) { backStackEntry ->
                val editViewModel: EditRuleViewModel = koinViewModel()
                val uiState by editViewModel.uiState.collectAsState()
                EditRuleScreen(
                    uiState = uiState,
                    onEvent = editViewModel::onEvent,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
