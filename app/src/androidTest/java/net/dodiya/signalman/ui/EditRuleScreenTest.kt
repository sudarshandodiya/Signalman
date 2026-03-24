package net.dodiya.signalman.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import net.dodiya.signalman.data.Filter
import net.dodiya.signalman.data.MatchType
import org.junit.Rule
import org.junit.Test

class EditRuleScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editRuleScreen_showsInitialState() {
        val initialState =
            EditRuleUiState(
                name = "Test Rule",
                filters = listOf(Filter("example.com", MatchType.CONTAINS)),
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Test Rule").assertIsDisplayed()
        composeTestRule.onNodeWithText("example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Condition").assertIsDisplayed()
    }

    @Test
    fun editRuleScreen_typingExampleUrl_triggersEvent() {
        var lastEvent: EditRuleEvent? = null
        val initialState = EditRuleUiState()

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                onEvent = { lastEvent = it },
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Example URL for live testing").performTextInput("https://test.com")

        // Assert that the event was triggered
        assert(lastEvent is EditRuleEvent.ExampleUrlChanged)
        assert((lastEvent as EditRuleEvent.ExampleUrlChanged).url == "https://test.com")
    }

    @Test
    fun editRuleScreen_previewMatch_showsMatch() {
        val initialState =
            EditRuleUiState(
                exampleUrl = "https://match.com",
                isPreviewMatch = true,
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Matches").assertIsDisplayed()
    }

    @Test
    fun editRuleScreen_previewMatch_showsNoMatch() {
        val initialState =
            EditRuleUiState(
                exampleUrl = "https://nomatch.com",
                isPreviewMatch = false,
            )

        composeTestRule.setContent {
            EditRuleScreen(
                uiState = initialState,
                onEvent = {},
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText("Does Not Match").assertIsDisplayed()
    }

    @Test
    fun editRuleScreen_saveButton_triggersSave() {
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
