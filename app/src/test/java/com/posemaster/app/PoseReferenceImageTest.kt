package com.posemaster.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import coil.EventListener
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import com.posemaster.app.domain.Pose
import com.posemaster.app.domain.PosePhoto
import com.posemaster.app.ui.PoseReferenceImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import okhttp3.Call
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Exercises Coil's real asset fetcher and bitmap decoder, rather than faking image success. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PoseReferenceImageTest {
    @get:Rule val compose = createComposeRule()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val decodeGate = CompletableDeferred<Unit>()
    private val enteredLoader = AtomicBoolean(false)
    private val networkCalls = AtomicInteger(0)
    private val decoded = AtomicReference<SuccessResult?>(null)
    private val failed = AtomicReference<ErrorResult?>(null)
    private lateinit var imageLoader: ImageLoader

    @Before fun configureIsolatedRealLoader() {
        imageLoader = ImageLoader.Builder(context)
            .allowHardware(false)
            .networkObserverEnabled(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .networkCachePolicy(CachePolicy.DISABLED)
            .callFactory(Call.Factory {
                networkCalls.incrementAndGet()
                error("Bundled image tests must never make a network request")
            })
            .components {
                add(object : Interceptor {
                    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                        enteredLoader.set(true)
                        decodeGate.await()
                        return chain.proceed(chain.request)
                    }
                })
            }
            .eventListener(object : EventListener {
                override fun onSuccess(request: ImageRequest, result: SuccessResult) { decoded.set(result) }
                override fun onError(request: ImageRequest, result: ErrorResult) { failed.set(result) }
            })
            .build()
    }

    @After fun shutDownLoader() {
        decodeGate.complete(Unit)
        if (::imageLoader.isInitialized) imageLoader.shutdown()
    }

    private fun show(assetPath: String) {
        val pose = Pose(
            id = 1048, name = "Reference renderer test", category = "Standing poses",
            subject = "Individual", style = "Casual", location = "Studio", position = "Standing",
            difficulty = "Beginner", premium = false, steps = "Relax comfortably.",
            hands = "Raise one hand.", feet = "Keep both feet supported.", face = "Look toward the camera.",
            photo = PosePhoto(
                assetPath = assetPath, altText = ALT_TEXT, source = "Original generated test fixture",
                credit = "Hello Photography test suite", license = "Original test-only plain-color asset",
                collectionId = "renderer-tests",
            ),
        )
        compose.setContent {
            MaterialTheme {
                PoseReferenceImage(
                    pose, Modifier.size(width = 240.dp, height = 320.dp), imageLoader = imageLoader,
                )
            }
        }
    }

    private fun waitForLabel(text: String) {
        waitForLoader {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForLoader(condition: () -> Boolean) {
        compose.waitUntil(timeoutMillis = 10_000) {
            // Coil resumes its Main dispatcher after background asset fetching/decoding.
            // Advancing Compose's clock alone does not drain Robolectric's paused Looper.
            shadowOf(Looper.getMainLooper()).idle()
            condition()
        }
    }

    @Test fun bundledAssetIsActuallyDecodedBeforeTheReferenceLabelAppears() {
        show("photos/test-reference.png")
        waitForLoader { enteredLoader.get() }
        compose.onNodeWithText("Reference photo").assertDoesNotExist()
        compose.onNodeWithText(FALLBACK).assertDoesNotExist()
        assertNull(decoded.get())

        decodeGate.complete(Unit)
        waitForLoader { decoded.get() != null || failed.get() != null }
        assertNull("The bundled PNG must decode successfully", failed.get())
        val result = decoded.get()!!
        assertEquals(DataSource.DISK, result.dataSource)
        assertTrue(result.drawable is BitmapDrawable)
        val bitmap = (result.drawable as BitmapDrawable).bitmap
        assertEquals(32, bitmap.width)
        assertEquals(48, bitmap.height)
        // A real decoded pixel from our original PNG, rather than a simulated drawable.
        assertEquals(Color.rgb(240, 180, 60), bitmap.getPixel(2, 2))
        waitForLabel("Reference photo")
        compose.onNodeWithText("Reference photo").assertIsDisplayed()
        compose.onNodeWithContentDescription(ALT_TEXT).assertExists()
        compose.onNodeWithText(FALLBACK).assertDoesNotExist()
        assertEquals(0, networkCalls.get())
    }

    @Test fun missingBundledAssetShowsAnIllustrationNoticeWithoutAReferenceLabel() {
        show("photos/does-not-exist.png")
        waitForLoader { enteredLoader.get() }
        compose.onNodeWithText("Reference photo").assertDoesNotExist()
        decodeGate.complete(Unit)
        waitForLoader { failed.get() != null }
        assertNotNull(failed.get()!!.throwable)
        assertNull(decoded.get())
        waitForLabel(FALLBACK)
        compose.onNodeWithText(FALLBACK).assertIsDisplayed()
        compose.onNodeWithText("Reference photo").assertDoesNotExist()
        assertEquals(0, networkCalls.get())
    }

    private companion object {
        const val ALT_TEXT = "Original plain-color fixture for testing bundled image decoding."
        const val FALLBACK = "Photo unavailable · illustration shown"
    }
}
