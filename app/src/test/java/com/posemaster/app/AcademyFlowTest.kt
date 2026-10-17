package com.posemaster.app

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.posemaster.app.data.AcademyStore
import com.posemaster.app.ui.LearnScreen
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AcademyFlowTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before fun reset() { context.getSharedPreferences("academy", Context.MODE_PRIVATE).edit().clear().commit() }

    private fun scrollToText(text: String): SemanticsNodeInteraction {
        // LazyColumn does not compose distant items until it is scrolled toward them.
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        return compose.onNodeWithText(text).performScrollTo()
    }

    @Test fun wrongAnswerCannotCompleteLessonButCorrectAnswerPersistsAndResumes() {
        compose.setContent { MaterialTheme { LearnScreen() } }
        scrollToText("Smartphone photography").performClick()
        scrollToText("Find light before a pose").performClick()
        scrollToText("The strongest filter").performClick()
        scrollToText("Check answer").performClick()
        scrollToText("Not quite. Try again.").assertIsDisplayed()
        AcademyStore(context).use { assertTrue(it.state.value.completed.isEmpty()) }
        scrollToText("The available light").performClick()
        scrollToText("Check answer").performClick()
        scrollToText("Lesson complete • Progress saved").assertIsDisplayed()
        AcademyStore(context).use { assertEquals(setOf("phone-light"), it.state.value.completed) }
        scrollToText("Next lesson").performClick()
        compose.onNodeWithText("Distance changes a face").assertExists()
        AcademyStore(context).use { assertEquals("phone-distance", it.state.value.lastLesson) }
    }

    @Test fun premiumSyllabusRoutesToPremiumWithoutOpeningLesson() {
        var premiumRequests = 0
        compose.setContent { MaterialTheme { LearnScreen(onPremium = { premiumRequests++ }) } }
        scrollToText("Photography business foundations").performClick()
        compose.onNodeWithText("Premium course preview").assertExists()
        scrollToText("Write a clear scope").performClick()
        assertEquals(1, premiumRequests)
        AcademyStore(context).use { assertNull(it.state.value.lastLesson); assertTrue(it.state.value.completed.isEmpty()) }
    }
}
