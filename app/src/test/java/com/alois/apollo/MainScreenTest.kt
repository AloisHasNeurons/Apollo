package com.alois.apollo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.alois.apollo.ui.MainScreen
import com.alois.apollo.ui.theme.ApolloTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `MainScreen displays the add workout floating action button`() {
        composeTestRule.setContent {
            ApolloTheme {
                MainScreen(onStartWorkout = {}, onViewSession = {})
            }
        }

        // Verify that the floating action button with the add icon is present
        composeTestRule.onNodeWithContentDescription("Add Workout").assertIsDisplayed()
    }
}
