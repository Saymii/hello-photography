package com.posemaster.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class AppFlowTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private fun onboard(role:String="everyone") {
  compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("welcome_continue"))
  compose.onNodeWithTag("welcome_continue").assertIsNotEnabled()
  compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("role_$role"))
  compose.onNodeWithTag("role_$role").performClick()
  compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("welcome_continue"))
  compose.onNodeWithTag("welcome_continue").performClick()
 }
 @Test fun guestCanFindReadAndSaveGuideAfterUpgrade() {
  onboard()
  compose.onNodeWithText("Explore Poses",useUnmergedTree=true).performClick()
  compose.onNodeWithTag("pose_search").performTextInput("Shared horizon")
  val resultCard=hasText("Shared horizon") and !hasSetTextAction()
  compose.onNodeWithTag("explore_list").performScrollToNode(resultCard)
  compose.onNode(resultCard).performClick()
  compose.onNodeWithText("← Back to collection").assertExists()
  compose.onNodeWithTag("pose_detail").performScrollToNode(hasText("Save favorite"))
  compose.waitUntil(10000){compose.onAllNodes(hasText("Save favorite") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText("Save favorite").performClick()
  compose.waitUntil(10000){compose.onAllNodesWithText("Update favorite").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText("Favorites",useUnmergedTree=true).performClick()
  compose.waitUntil(10000){compose.onAllNodesWithText("Shared horizon").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText("Shared horizon").assertExists()
 }
 @Test fun rolePersistsAcrossActivityRecreation() {
  onboard("photographer")
  compose.onNodeWithText("WELCOME, PHOTOGRAPHER").assertExists()
  compose.activityRule.scenario.recreate()
  compose.onNodeWithText("WELCOME, PHOTOGRAPHER").assertExists()
  compose.onNodeWithTag("welcome").assertDoesNotExist()
 }
 @Test fun profileCanChangeRoleAndDeleteLocalData() {
  onboard("model")
  compose.onNodeWithText("Profile",useUnmergedTree=true).performClick()
  compose.onNodeWithText("I'm a Model").performClick()
  compose.onNodeWithText("I'm a Photographer").performClick()
  compose.onNodeWithText("Home",useUnmergedTree=true).performClick()
  compose.onNodeWithText("WELCOME, PHOTOGRAPHER").assertExists()
  compose.onNodeWithText("Profile",useUnmergedTree=true).performClick()
  compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Delete all local data"))
  compose.onNodeWithText("Delete all local data").performClick()
  compose.onNodeWithText("Delete data").performClick()
  compose.waitUntil(10000){compose.onAllNodesWithTag("welcome").fetchSemanticsNodes().isNotEmpty()}
  compose.onNodeWithText("Welcome to Hello Photography").assertExists()
 }
 @Test fun amharicWelcomeSupportsRoleSelection() {
  compose.onNodeWithText("አማርኛ").performClick()
  compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("role_everyone"))
  compose.onNodeWithTag("role_everyone").performClick()
  compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("welcome_continue"))
  compose.onNodeWithTag("welcome_continue").performClick()
  compose.onNodeWithText("መነሻ",useUnmergedTree=true).assertExists()
  compose.onNodeWithText("መገለጫ",useUnmergedTree=true).assertExists()
 }
}
