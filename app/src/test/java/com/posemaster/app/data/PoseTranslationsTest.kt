package com.posemaster.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.posemaster.app.domain.Pose
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PoseTranslationsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val translations = PoseTranslations(context)

    private fun asset(name: String) = JSONArray(
        context.assets.open(name).bufferedReader().use { it.readText() }
    )

    private fun poses(): List<Pose> = asset("poses.json").let { data ->
        (0 until data.length()).map { index ->
            data.getJSONObject(index).let {
                Pose(
                    id = it.getInt("id"), name = it.getString("name"),
                    category = it.getString("category"), subject = it.getString("subject"),
                    style = it.getString("style"), location = it.getString("location"),
                    position = it.getString("position"), difficulty = it.getString("difficulty"),
                    premium = it.getBoolean("premium"), steps = it.getString("steps"),
                    hands = it.getString("hands"), feet = it.getString("feet"), face = it.getString("face"),
                )
            }
        }
    }

    @Test fun amharicCatalogCoversEverySourcePoseExactlyOnce() {
        val source = poses()
        val translated = asset("poses-am.json")
        val translatedIds = (0 until translated.length()).map { index ->
            val entry = translated.getJSONObject(index)
            assertEquals(setOf("id", "name", "steps", "hands", "feet", "face"), entry.keys().asSequence().toSet())
            for (field in listOf("name", "steps", "hands", "feet", "face")) {
                assertTrue("Pose ${entry.getInt("id")} needs Amharic $field", entry.getString(field).any { it in '\u1200'..'\u137F' })
            }
            entry.getInt("id")
        }
        assertEquals(48, source.size)
        assertEquals(source.map { it.id }.toSet(), translatedIds.toSet())
        assertEquals(translatedIds.size, translatedIds.toSet().size)
    }

    @Test fun localizationPreservesSavedIdsAndOfflineFilterCodes() {
        for (pose in poses()) {
            val localized = translations.localize(pose, "am")
            assertNotEquals(pose.name, localized.name)
            assertNotEquals(pose.steps, localized.steps)
            assertEquals(pose, localized.copy(name = pose.name, steps = pose.steps, hands = pose.hands, feet = pose.feet, face = pose.face))
        }
    }

    @Test fun everyCategoryHasAnAmharicDisplayLabelWithoutChangingItsEnglishKey() {
        val categories = poses().map { it.category }.toSet()
        assertEquals(24, categories.size)
        for (category in categories) {
            assertEquals(category, categoryLabel(category, "en"))
            val localized = categoryLabel(category, "am")
            assertNotEquals(category, localized)
            assertTrue(localized.any { it in '\u1200'..'\u137F' })
        }
        assertEquals("Future category", categoryLabel("Future category", "am"))
    }

    @Test fun englishUnknownLanguageAndUnknownPoseFallBackToOriginal() {
        val pose = poses().first()
        assertSame(pose, translations.localize(pose, "en"))
        assertSame(pose, translations.localize(pose, "fr"))
        val futurePose = pose.copy(id = 9999)
        assertSame(futurePose, translations.localize(futurePose, "am"))
        assertEquals(translations.localize(pose, "am"), translations.localize(pose, "am-ET"))
        assertEquals(translations.localize(pose, "am"), translations.localize(pose, "am_ET"))
        assertFalse(translations.localize(pose, "am").name.isBlank())
    }
}
