package com.posemaster.app

import android.os.Looper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.posemaster.app.data.PoseCatalog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Uses the real activity, bundled images and Coil to exercise the complete inspection flow. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PhotoViewerFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun onboard() {
        compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("role_everyone"))
        compose.onNodeWithTag("role_everyone").performClick()
        compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("welcome_continue"))
        compose.onNodeWithTag("welcome_continue").performClick()
    }

    private fun waitForViewerImage(altText: String) {
        val viewerImage = hasContentDescription(altText) and hasAnyAncestor(hasTestTag("photo_viewer"))
        compose.waitUntil(timeoutMillis = 10_000) {
            shadowOf(Looper.getMainLooper()).idle()
            compose.onAllNodes(viewerImage, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(viewerImage, useUnmergedTree = true).assertExists()
    }

    @Test fun guestCanOpenZoomBrowseAndCloseAPhotoWithoutLosingTheSelectedGuide() {
        val photos = PoseCatalog.load(compose.activity.assets).filter { it.photo != null }
        val first = photos[0]
        val second = photos[1]
        onboard()
        compose.onNodeWithText("Explore Poses", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("pose_search").performTextInput(first.name)
        val resultCard = hasText(first.name) and !hasSetTextAction()
        compose.onNodeWithTag("explore_list").performScrollToNode(resultCard)
        compose.onNode(resultCard).performClick()
        compose.onNodeWithTag("pose_detail").performScrollToNode(hasTestTag("open_photo_viewer"))
        compose.onNodeWithTag("open_photo_viewer").performClick()

        compose.onNodeWithTag("photo_viewer").assertIsDisplayed()
        compose.onNodeWithTag("viewer_position").assertTextEquals("1 / ${photos.size}")
        waitForViewerImage(first.photo!!.altText)
        compose.onNodeWithTag("zoom_level").assertTextEquals("100%")
        compose.onNodeWithTag("zoom_out").assertIsNotEnabled()
        compose.onNodeWithTag("viewer_previous").assertIsNotEnabled()
        compose.onNodeWithTag("zoom_in").performClick()
        compose.onNodeWithTag("zoom_level").assertTextEquals("150%")
        compose.onNodeWithTag("zoom_out").assertIsEnabled()
        compose.onNodeWithTag("zoom_reset").performClick()
        compose.onNodeWithTag("zoom_level").assertTextEquals("100%")

        compose.onNodeWithTag("viewer_next").performClick()
        compose.onNodeWithTag("viewer_position").assertTextEquals("2 / ${photos.size}")
        waitForViewerImage(second.photo!!.altText)
        compose.onNodeWithTag("viewer_previous").assertIsEnabled()
        compose.onNodeWithTag("viewer_close").performClick()

        compose.onNodeWithTag("photo_viewer").assertDoesNotExist()
        compose.onNodeWithTag("pose_detail").assertExists()
        compose.onNodeWithTag("pose_detail").performScrollToNode(hasText(first.name))
        compose.onNodeWithText(first.name).assertExists()
        compose.onNodeWithTag("pose_detail").performScrollToNode(hasTestTag("open_photo_viewer"))
        compose.onNodeWithTag("open_photo_viewer").performClick()
        compose.onNodeWithTag("viewer_position").assertTextEquals("1 / ${photos.size}")
        compose.onNodeWithTag("viewer_close").performClick()
    }
}
