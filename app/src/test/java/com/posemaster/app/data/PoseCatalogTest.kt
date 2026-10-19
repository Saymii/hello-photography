package com.posemaster.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.FileNotFoundException
import java.io.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PoseCatalogTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val original by lazy { context.assets.open("poses.json").bufferedReader().use { it.readText() } }

    private fun photoMetadata(): JSONObject = JSONObject()
        .put("assetPath", "photos/owner-photo-set-2026-10/1048.webp")
        .put("altText", "Standing subject with one relaxed hand raised in a wave.")
        .put("source", "Test-only owner-supplied file 48")
        .put("credit", "Test photographer")
        .put("license", "Test-only redistribution permission")
        .put("collectionId", "owner-photo-set-2026-10")

    private fun entry(id: Int = 1048, photo: JSONObject? = photoMetadata()): JSONObject =
        JSONObject(JSONArray(original).getJSONObject(0).toString()).put("id", id).apply {
            if (photo != null) put("photo", photo)
        }

    private fun collection(vararg entries: JSONObject): String = JSONArray().apply {
        entries.forEach { put(it) }
    }.toString()

    @Test fun bundledLoaderRetainsTheOriginalGuidesInTheirOriginalOrder() {
        val loaded = PoseCatalog.load(context.assets)
        val source = JSONArray(original)
        assertEquals((1..48).toList(), loaded.take(48).map { it.id })
        for (index in 0 until source.length()) {
            assertEquals(source.getJSONObject(index).getString("name"), loaded[index].name)
            assertEquals(source.getJSONObject(index).getString("steps"), loaded[index].steps)
        }
    }

    @Test fun absentOptionalCollectionPreservesAllOriginalGuidesAndIds() {
        val poses = PoseCatalog.load(
            readAsset = { path -> if (path == "poses.json") original else throw FileNotFoundException(path) },
            photoExists = { error("Original guides do not require photo files") },
        )
        assertEquals((1..48).toList(), poses.map { it.id })
        val source = JSONArray(original)
        for ((index, pose) in poses.withIndex()) {
            assertEquals(source.getJSONObject(index).getString("name"), pose.name)
            assertEquals(source.getJSONObject(index).getString("steps"), pose.steps)
            assertEquals(null, pose.photo)
        }
    }

    @Test fun optionalCollectionAppendsMetadataAndChecksTheBundledAsset() {
        val checkedPaths = mutableListOf<String>()
        val poses = PoseCatalog.parse(original, collection(entry())) { checkedPaths.add(it); true }
        assertEquals((1..48).toList() + 1048, poses.map { it.id })
        val reference = poses.last().photo!!
        assertEquals(listOf("photos/owner-photo-set-2026-10/1048.webp"), checkedPaths)
        assertEquals(checkedPaths.single(), reference.assetPath)
        assertEquals(photoMetadata().getString("altText"), reference.altText)
        assertEquals("Test photographer", reference.credit)
        assertEquals("Test-only redistribution permission", reference.license)
        assertEquals("Test-only owner-supplied file 48", reference.source)
        assertEquals("owner-photo-set-2026-10", reference.collectionId)
    }

    @Test fun localizationPreservesPhotoMetadataAlongsideTheOriginalStableId() {
        val withPhoto = PoseCatalog.parse(collection(entry(1)), null) { true }.single()
        val translated = PoseTranslations(context).localize(withPhoto, "am")
        assertEquals(1, translated.id)
        assertFalse(withPhoto.name == translated.name)
        assertSame(withPhoto.photo, translated.photo)
    }

    @Test fun unsafePathsNeverReachTheAssetLoader() {
        val invalidPaths = listOf(
            "https://example.com/photo.jpg", "file:///sdcard/photo.jpg", "/photos/1048.webp",
            "photos/../1048.webp", "photos/set/../../1048.webp", "photos/./1048.webp",
            "photos//1048.webp", "photos\\1048.webp", "photos/set/1048.webp?token=1",
            "photos/%2e%2e/1048.webp", "photos/set/1048.svg", "other/1048.webp",
        )
        for (path in invalidPaths) {
            assertThrows("Unsafe path: $path", IllegalArgumentException::class.java) {
                PoseCatalog.parse(original, collection(entry(photo = photoMetadata().put("assetPath", path)))) {
                    error("Unsafe path reached the asset loader: $it")
                }
            }
        }
    }

    @Test fun referencesNeedAllMetadataAndAnExistingBundledFile() {
        for (field in listOf("assetPath", "altText", "source", "credit", "license", "collectionId")) {
            for (invalid in listOf<Any>("  ", 123, JSONObject.NULL)) {
                assertThrows("Missing or invalid $field", IllegalArgumentException::class.java) {
                    PoseCatalog.parse(original, collection(entry(photo = photoMetadata().put(field, invalid)))) { true }
                }
            }
            assertThrows("Missing $field", IllegalArgumentException::class.java) {
                val missing = photoMetadata().apply { remove(field) }
                PoseCatalog.parse(original, collection(entry(photo = missing))) { true }
            }
        }
        val missingFile = assertThrows(IllegalArgumentException::class.java) {
            PoseCatalog.parse(original, collection(entry())) { false }
        }
        assertTrue(missingFile.message!!.contains("asset is missing"))
    }

    @Test fun collidingOrRepeatedIdsAreRejectedInsteadOfReplacingSavedGuides() {
        val collision = assertThrows(IllegalArgumentException::class.java) {
            PoseCatalog.parse(original, collection(entry(48))) { true }
        }
        assertEquals("Duplicate pose ID 48", collision.message)
        assertThrows(IllegalArgumentException::class.java) {
            PoseCatalog.parse(original, collection(entry(), entry())) { true }
        }
    }

    @Test fun publishedPhotoEntriesCannotOmitThePhotoReference() {
        assertThrows(IllegalArgumentException::class.java) {
            PoseCatalog.parse(original, collection(entry(photo = null))) { true }
        }
    }

    @Test fun malformedCatalogsAndOtherReadFailuresAreNotSilentlyIgnored() {
        assertThrows(JSONException::class.java) {
            PoseCatalog.load(
                readAsset = { path -> if (path == "poses.json") original else "not JSON" },
                photoExists = { true },
            )
        }
        assertThrows(IOException::class.java) {
            PoseCatalog.load(
                readAsset = { path -> if (path == "poses.json") original else throw IOException("Read failed") },
                photoExists = { true },
            )
        }
        assertThrows(FileNotFoundException::class.java) {
            PoseCatalog.load(readAsset = { throw FileNotFoundException(it) }, photoExists = { true })
        }
    }
}
