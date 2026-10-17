package com.posemaster.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.posemaster.app.data.AppPreferences
import com.posemaster.app.data.PoseDatabase
import com.posemaster.app.data.PoseRepository
import com.posemaster.app.data.SavedPose
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Regression coverage for an existing installation upgrading to role-based onboarding. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UpgradeStorageTest {
    private lateinit var context: Context
    private val ownedDatabases = mutableListOf<PoseDatabase>()

    @Before
    fun resetLocalStorage() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("preferences", Context.MODE_PRIVATE).edit().clear().commit()
        context.deleteDatabase("posemaster.db")
    }

    @After
    fun closeDatabases() {
        ownedDatabases.forEach { it.close() }
        context.deleteDatabase("posemaster.db")
    }

    @Test
    fun selectingRoleRetainsLegacyThemeOnboardingAndSavedGuide() = runBlocking {
        val legacyPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        legacyPreferences.edit()
            .putBoolean("onboarded", true)
            .putString("theme", "Dark")
            .putString("interests", "Wedding photography")
            .commit()
        val legacyFavorite = SavedPose(21, "Weekend shoot", downloaded = true)
        seedLegacyFavorites(listOf(legacyFavorite))

        val preferences = AppPreferences(context)
        assertNull("The old onboarding flag must not imply a selected role", preferences.state.value.role)
        assertEquals("Dark", preferences.state.value.theme)
        preferences.setRole("photographer")

        val reloaded = AppPreferences(context).state.value
        assertEquals("photographer", reloaded.role)
        assertEquals("Dark", reloaded.theme)
        assertTrue(legacyPreferences.getBoolean("onboarded", false))
        assertEquals("Wedding photography", legacyPreferences.getString("interests", null))
        assertEquals(listOf(legacyFavorite), newRepository().saved.first())
    }

    @Test
    fun roleLanguageAndRecentOrderSurviveNewPreferenceInstances() {
        val preferences = AppPreferences(context)
        preferences.setRole("model")
        preferences.setLanguage("am")
        preferences.setTheme("Light")
        preferences.setNotifications(true)
        (1..15).forEach(preferences::recordView)
        preferences.recordView(9)

        val reloaded = AppPreferences(context)
        assertEquals("model", reloaded.state.value.role)
        assertEquals("am", reloaded.state.value.language)
        assertEquals("Light", reloaded.state.value.theme)
        assertTrue(reloaded.state.value.notificationsEnabled)
        assertEquals(listOf(9, 15, 14, 13, 12, 11, 10, 8, 7, 6, 5, 4), reloaded.state.value.recentIds)

        reloaded.setRole("everyone")
        reloaded.setLanguage("en")
        reloaded.recordView(15)
        val thirdInstance = AppPreferences(context).state.value
        assertEquals("everyone", thirdInstance.role)
        assertEquals("en", thirdInstance.language)
        assertEquals(listOf(15, 9, 14, 13, 12, 11, 10, 8, 7, 6, 5, 4), thirdInstance.recentIds)
    }

    @Test
    fun twentiethFavoriteIsAllowedAndExistingFavoriteCanBeEditedAtLimit() = runBlocking {
        val repository = newRepository()
        val originalFavorites = (1..20).map { SavedPose(it, "Original collection") }
        originalFavorites.forEach { assertTrue(repository.save(it)) }

        val edited = SavedPose(7, "Client shortlist", downloaded = true)
        assertTrue("Updating a saved pose does not consume another slot", repository.save(edited))
        assertFalse("A new pose exceeds the free limit", repository.save(SavedPose(21)))

        val expected = originalFavorites.map { if (it.poseId == 7) edited else it }
        assertEquals("Rejected saves must not evict or alter existing rows", expected, repository.saved.first())
        assertEquals("The saved rows must also be visible from a reopened repository", expected, newRepository().saved.first())
    }

    @Test
    fun legacyFavoritesAboveNewLimitArePreservedAndRemainEditable() = runBlocking {
        val legacyFavorites = (1..24).map { SavedPose(it, "Before upgrade", downloaded = true) }
        seedLegacyFavorites(legacyFavorites)
        val preferences = AppPreferences(context)
        preferences.setRole("everyone")
        val repository = newRepository()

        assertEquals(legacyFavorites, repository.saved.first())
        val edited = SavedPose(24, "Retained offline guides", downloaded = true)
        assertTrue(repository.save(edited))
        assertFalse(repository.save(SavedPose(25)))
        assertEquals(legacyFavorites.dropLast(1) + edited, repository.saved.first())
    }

    @Test
    fun clearingSavedDataAllowsNewFavoritesAgain() = runBlocking {
        val repository = newRepository()
        (1..20).forEach { assertTrue(repository.save(SavedPose(it))) }
        assertFalse(repository.save(SavedPose(21)))

        repository.clear()

        assertTrue(repository.saved.first().isEmpty())
        val newFavorite = SavedPose(21, "A fresh start", downloaded = true)
        assertTrue(repository.save(newFavorite))
        assertEquals(listOf(newFavorite), repository.saved.first())
    }

    private suspend fun seedLegacyFavorites(favorites: List<SavedPose>) {
        val database = Room.databaseBuilder(context, PoseDatabase::class.java, "posemaster.db").build()
        try {
            favorites.forEach { database.saved().save(it) }
        } finally {
            database.close()
        }
    }

    private fun newRepository(): PoseRepository = PoseRepository(context).also { repository ->
        // The production repository has no close API. Retain its handle only for test cleanup;
        // all behavior and persistence assertions use the public repository API.
        val databaseField = PoseRepository::class.java.getDeclaredField("db")
        databaseField.isAccessible = true
        ownedDatabases += databaseField.get(repository) as PoseDatabase
    }
}
