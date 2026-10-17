package com.posemaster.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppFlowTest {
 @get:Rule val compose = createAndroidComposeRule<MainActivity>()
 @Test fun guestCanFindReadAndSaveGuide() {
  compose.onNodeWithText("Continue").performScrollTo().performClick()
  compose.onNodeWithText("Continue").performScrollTo().performClick()
  compose.onNodeWithText("Start exploring").performScrollTo().performClick()
  compose.onNodeWithText("Explore",useUnmergedTree=true).performClick()
  compose.onNodeWithText("Search poses, moods, and moments").performTextInput("Shared horizon")
  val resultCard=hasText("Shared horizon") and !hasSetTextAction()
  compose.onNode(hasScrollToIndexAction()).performScrollToNode(resultCard)
  compose.onNode(resultCard).assertExists().performClick()
  compose.onNodeWithText("← Back to collection").assertExists()
  compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Save favorite"))
  compose.onNodeWithText("Save favorite").performClick()
  compose.onNodeWithText("Favorites",useUnmergedTree=true).performClick()
  compose.waitUntil(10000) {compose.onAllNodesWithText("Shared horizon").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText("Shared horizon").assertExists()
 }
}
