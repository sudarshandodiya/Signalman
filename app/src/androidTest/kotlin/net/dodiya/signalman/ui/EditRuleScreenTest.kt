package net.dodiya.signalman.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.ui.editrule.EditRuleEvent
import net.dodiya.signalman.ui.editrule.EditRuleScreen
import net.dodiya.signalman.ui.editrule.EditRuleUiState
import org.junit.Rule
import org.junit.Test

class EditRuleScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editRuleScreenShowsInitialState() {
        val initialState =
            EditRuleUiState(
                name = "Test Rule",
                filters = listOf(Filter("example.com", MatchType.CONTAINS)),
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                installedApps = emptyList(),
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onAllNodesWithText("Test Rule").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Condition").assertIsDisplayed()
    }

    @Test
    fun editRuleScreenSwitchesToTransformationTab() {
        val initialState =
            EditRuleUiState(
                name = "Test Rule",
                filters = listOf(Filter("example.com", MatchType.CONTAINS)),
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                installedApps = emptyList(),
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Transformation").performClick()
        composeTestRule.onNodeWithText("Enable Transformation").assertIsDisplayed()
    }

    @Test
    fun editRuleScreenPreviewMatchShowsMatch() {
        val initialState =
            EditRuleUiState(
                exampleUrl = "https://match.com",
                isPreviewMatch = true,
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                installedApps = emptyList(),
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Matches").assertIsDisplayed()
    }

    @Test
    fun editRuleScreenPreviewMatchShowsNoMatch() {
        val initialState =
            EditRuleUiState(
                exampleUrl = "https://nomatch.com",
                isPreviewMatch = false,
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                installedApps = emptyList(),
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Does Not Match").assertIsDisplayed()
    }

    @Test
    fun editRuleScreenSaveButtonTriggersSave() {
        var saved = false
        val initialState =
            EditRuleUiState(
                name = "Valid Rule",
                filters = listOf(Filter("pattern", MatchType.CONTAINS)),
                isSaveEnabled = true,
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                installedApps = emptyList(),
                onEvent = {
                    if (it is EditRuleEvent.SaveRule) saved = true
                },
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Save").performClick()
        assert(saved)
    }
}
