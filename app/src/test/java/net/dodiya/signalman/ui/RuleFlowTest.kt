package net.dodiya.signalman.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.dodiya.signalman.data.AppInfoRepository
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase
import net.dodiya.signalman.ui.ImportExportState
import net.dodiya.signalman.ui.RuleViewModel
import net.dodiya.signalman.ui.SettingsViewModel
import net.dodiya.signalman.ui.rulelist.RuleListScreen
import net.dodiya.signalman.ui.theme.SignalmanTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RuleFlowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val appInfoRepository = mockk<AppInfoRepository>(relaxed = true)
    private val repository = mockk<RuleRepository>(relaxed = true)
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val matchRuleUseCase = MatchRuleUseCase()
    private val transformUrlUseCase = TransformUrlUseCase()
    private val settingsViewModel = mockk<SettingsViewModel>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    lateinit var viewModel: RuleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { repository.allRules } returns MutableStateFlow(emptyList())
        every { preferenceManager.globalDefaultPackage } returns MutableStateFlow(null)
        every { preferenceManager.isAutoRuleGenerationEnabled } returns MutableStateFlow(true)
        every { settingsViewModel.hiddenBrowsers } returns MutableStateFlow(emptySet())
        every { settingsViewModel.importExportState } returns MutableStateFlow(ImportExportState.Idle as ImportExportState)

        viewModel =
            RuleViewModel(
                appInfoRepository = appInfoRepository,
                repository = repository,
                preferenceManager = preferenceManager,
                matchRuleUseCase = matchRuleUseCase,
                transformUrlUseCase = transformUrlUseCase,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun testAddRuleFlow() {
        var navigatedToEdit = false
        var capturedName = ""
        var capturedUrl = ""

        composeTestRule.setContent {
            SignalmanTheme {
                RuleListScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onAddRule = { name, url ->
                        navigatedToEdit = true
                        capturedName = name
                        capturedUrl = url
                    },
                    onEditRule = {},
                    onNavigateToImportExport = {},
                )
            }
        }

        // Open Add Rule Dialog
        composeTestRule.onNodeWithTag("add_rule_fab").performClick()
        composeTestRule.waitForIdle()

        // Verify dialog is shown (should have rule_name_input)
        composeTestRule.onNodeWithTag("rule_name_input").assertExists()

        // Fill dialog
        composeTestRule.onNodeWithTag("rule_name_input").performTextInput("My Test Rule")
        composeTestRule.onNodeWithTag("example_url_input").performTextInput("https://test.com")
        composeTestRule.waitForIdle()

        // Click Next
        composeTestRule.onNodeWithTag("dialog_next_button").performClick()
        composeTestRule.waitForIdle()

        // Verify callback
        assert(navigatedToEdit) { "Callback onAddRule was not called" }
        assert(capturedName == "My Test Rule")
        assert(capturedUrl == "https://test.com")
    }
}
