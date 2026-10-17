package com.posemaster.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.posemaster.app.data.AcademyStore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AcademyPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before fun reset() { context.getSharedPreferences("academy", Context.MODE_PRIVATE).edit().clear().commit() }

    @Test fun visitDoesNotCompleteAndCompletedLessonsSurviveRecreation() {
        AcademyStore(context).use { store ->
            store.visit("phone-light")
            assertEquals("phone-light", store.state.value.lastLesson)
            assertTrue(store.state.value.completed.isEmpty())
            store.complete("phone-light")
            store.complete("phone-light")
        }
        AcademyStore(context).use { restored ->
            assertEquals(setOf("phone-light"), restored.state.value.completed)
            assertEquals("phone-light", restored.state.value.lastLesson)
            restored.visit("phone-distance")
            assertEquals(setOf("phone-light"), restored.state.value.completed)
            assertEquals("phone-distance", restored.state.value.lastLesson)
        }
    }

    @Test fun clearingAcademyLeavesOtherUserPreferencesAlone() {
        val other = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
        other.edit().putString("role", "model").commit()
        AcademyStore(context).use { store ->
            store.complete("phone-light")
            store.clear()
            assertTrue(store.state.value.completed.isEmpty())
            assertNull(store.state.value.lastLesson)
        }
        assertEquals("model", other.getString("role", null))
    }

    @Test fun unknownLessonsCannotAccidentallyEnterProgress() {
        AcademyStore(context).use { store ->
            assertTrue(runCatching { store.complete("missing") }.isFailure)
            assertTrue(runCatching { store.visit("missing") }.isFailure)
            assertTrue(store.state.value.completed.isEmpty())
        }
    }
}
