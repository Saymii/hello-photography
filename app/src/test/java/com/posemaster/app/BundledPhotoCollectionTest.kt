package com.posemaster.app

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.size.Size
import com.posemaster.app.data.PoseCatalog
import com.posemaster.app.domain.Finder
import com.posemaster.app.domain.PoseFilter
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import okhttp3.Call
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Checks the supplied collection itself, including real offline fetching and bitmap decoding. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BundledPhotoCollectionTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun everySuppliedPhotoHasItsOwnGuideAndIsReachableThroughSearchAndCategory() {
        val catalog = PoseCatalog.load(context.assets)
        val photos = catalog.filter { it.photo != null }

        // Nine photos have not been supplied. Their absent files must not become fake references.
        assertEquals(EXPECTED_PHOTO_IDS, photos.map { it.id }.toSet())
        assertEquals(39, photos.size)
        assertEquals(
            mapOf(
                "Male poses" to 12,
                "Female poses" to 10,
                "Couple photography" to 8,
                "Group photography" to 1,
                "Casual & selfie poses" to 8,
            ),
            photos.groupingBy { it.category }.eachCount(),
        )
        assertEquals(photos.size, photos.map { it.photo!!.assetPath }.distinct().size)
        assertEquals(photos.size, photos.map { it.name }.distinct().size)

        for (pose in photos) {
            val reference = pose.photo!!
            assertTrue("Pose ${pose.id} needs a descriptive title", pose.name.trim().length > 4)
            assertTrue("Pose ${pose.id} needs descriptive accessible text", reference.altText.trim().length > 20)
            assertFalse(reference.altText.contains("placeholder", ignoreCase = true))
            for ((field, value) in listOf(
                "steps" to pose.steps, "hands" to pose.hands, "feet" to pose.feet,
                "face" to pose.face, "camera angle" to pose.angle, "lighting" to pose.lighting,
                "source" to reference.source, "credit" to reference.credit, "license" to reference.license,
            )) {
                assertTrue("Pose ${pose.id} needs $field", value.isNotBlank())
            }
            assertTrue(
                "Pose ${pose.id} must be available in ${pose.category}",
                PoseFilter.apply(catalog, "", pose.category, Finder()).any { it.id == pose.id },
            )
            assertTrue(
                "Pose ${pose.id} must be searchable by its title",
                PoseFilter.apply(catalog, pose.name, "All", Finder()).any { it.id == pose.id },
            )
        }
    }

    @Test fun originalSavedGuideIdsAndContentSurviveAddingThePhotoCollection() {
        val originalJson = context.assets.open("poses.json").bufferedReader().use { it.readText() }
        val originalGuides = PoseCatalog.parse(originalJson, null) { error("Original guides need no photo") }
        val catalog = PoseCatalog.load(context.assets)

        assertEquals((1..48).toList(), originalGuides.map { it.id })
        assertEquals(originalGuides, catalog.take(48))
        assertEquals(87, catalog.size)
    }

    @Test fun all39BundledPhotoFilesDecodeAsRealPhotographsWithoutNetworkAccess() {
        val networkCalls = AtomicInteger(0)
        val loader = ImageLoader.Builder(context)
            .allowHardware(false)
            .networkObserverEnabled(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .networkCachePolicy(CachePolicy.DISABLED)
            .callFactory(Call.Factory {
                networkCalls.incrementAndGet()
                error("Bundled photos must load offline")
            })
            .build()
        try {
            val photos = PoseCatalog.load(context.assets).filter { it.photo != null }
            assertEquals(39, photos.size)
            for (pose in photos) {
                val reference = pose.photo!!
                val completed = AtomicReference<ImageResult?>(null)
                val request = ImageRequest.Builder(context)
                    .data("file:///android_asset/${reference.assetPath}")
                    .size(Size.ORIGINAL)
                    .listener(
                        onSuccess = { _, result -> completed.set(result) },
                        onError = { _, result -> completed.set(result) },
                    )
                    .build()
                val disposable = loader.enqueue(request)
                try {
                    val deadline = System.nanoTime() + 10_000_000_000L
                    while (completed.get() == null && System.nanoTime() < deadline) {
                        // Coil posts request completion to Main after background asset decoding.
                        shadowOf(Looper.getMainLooper()).idle()
                        Thread.sleep(5)
                    }
                    val result = completed.get()
                    assertNotNull("Photo ${pose.id} did not finish loading: ${reference.assetPath}", result)
                    if (result is ErrorResult) {
                        throw AssertionError("Photo ${pose.id} failed to decode: ${reference.assetPath}", result.throwable)
                    }
                    assertTrue("Photo ${pose.id} must decode successfully", result is SuccessResult)
                    result as SuccessResult
                    assertEquals("Photo ${pose.id} must come from bundled assets", DataSource.DISK, result.dataSource)
                    assertTrue("Photo ${pose.id} must contain decoded pixels", result.drawable is BitmapDrawable)
                    val bitmap = (result.drawable as BitmapDrawable).bitmap
                    try {
                        assertTrue("Photo ${pose.id} is too small", bitmap.width >= 640 && bitmap.height >= 640)
                        assertTrue("Photo ${pose.id} needs a mobile-sized copy", maxOf(bitmap.width, bitmap.height) <= 1280)
                        val sampledColors = mutableSetOf<Int>()
                        for (row in 0 until 12) for (column in 0 until 12) {
                            sampledColors.add(bitmap.getPixel(
                                column * (bitmap.width - 1) / 11,
                                row * (bitmap.height - 1) / 11,
                            ))
                        }
                        assertTrue("Photo ${pose.id} is unexpectedly blank", sampledColors.size > 20)
                    } finally {
                        bitmap.recycle()
                    }
                } finally {
                    disposable.dispose()
                }
            }
            assertEquals(0, networkCalls.get())
        } finally {
            loader.shutdown()
        }
    }

    private companion object {
        val EXPECTED_PHOTO_IDS = ((1001..1012) + (2001..2010) + (3001..3008) + (1040..1048)).toSet()
    }
}
