package com.posemaster.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Captures actual Compose UI, without frames, captions, or invented screenshots.
 * Native Robolectric graphics makes this reproducible on a cloud machine without KVM.
 * Run explicitly: ./gradlew :app:testDebugUnitTest --tests '*StoreScreenshotTest'
 * This is rendering evidence; it does not replace physical-device release testing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StoreScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        val output = File("build/outputs/store-screenshots").apply { mkdirs() }
        // Compose captureToImage uses a hardware frame/PixelCopy callback that a
        // headless Robolectric window may never deliver. Draw the same Activity's
        // actual decor view with native Canvas instead; do not construct fake UI.
        val screenshot = compose.runOnUiThread {
            val decor = compose.activity.window.decorView
            check(decor.width > 0 && decor.height > 0) { "Activity must be laid out before capture" }
            Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888).also {
                it.setHasAlpha(false)
                decor.draw(Canvas(it))
            }
        }
        File(output, "$name.png").outputStream().use {
            check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        screenshot.recycle()
    }

    @Test fun captureActualEnglishPhoneScreens() {
        // Prepare this app's real local preferences; never seed a premium entitlement.
        compose.runOnUiThread {
            compose.activity.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                .edit().clear().putString("theme", "Light").putString("language", "en").commit()
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("welcome").assertExists()
        capture("01-welcome")

        compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("role_photographer"))
        compose.onNodeWithTag("role_photographer").performClick()
        compose.onNodeWithTag("welcome").performScrollToNode(hasTestTag("welcome_continue"))
        compose.onNodeWithTag("welcome_continue").performClick()
        compose.onNodeWithText("WELCOME, PHOTOGRAPHER").assertExists()
        capture("02-personalized-home")

        compose.onNodeWithText("Explore Poses", useUnmergedTree = true).performClick()
        capture("03-explore-poses")

        compose.onNodeWithTag("pose_search").performTextInput("Shared horizon")
        val guide = hasText("Shared horizon") and !hasSetTextAction()
        compose.onNodeWithTag("explore_list").performScrollToNode(guide)
        compose.onNode(guide).performClick()
        capture("04-original-pose-guide")

        compose.onNodeWithText("Learn", useUnmergedTree = true).performClick()
        capture("05-learning-academy")

        compose.onNodeWithText("Profile", useUnmergedTree = true).performClick()
        capture("06-profile-settings")
    }
}
