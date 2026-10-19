package com.posemaster.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.imageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.posemaster.app.domain.Pose

/** Bundled reference photos need no network, account, or photo-library permission. */
@Composable
fun PoseReferenceImage(pose: Pose, modifier: Modifier = Modifier, showLabel: Boolean = true, language: String = "en", imageLoader: ImageLoader? = null, onImageSize: ((IntSize) -> Unit)? = null) {
    val photo = pose.photo
    if (photo == null) {
        OriginalPoseArt(pose, modifier, showLabel)
        return
    }
    SubcomposeAsyncImage(
        model = "file:///android_asset/${photo.assetPath}",
        imageLoader = imageLoader ?: LocalContext.current.imageLoader,
        contentDescription = null,
        // Preserve the complete reference: feet, hands and props matter for a posing guide.
        contentScale = ContentScale.Fit,
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
        },
        error = {
            Box(Modifier.fillMaxSize()) {
                OriginalPoseArt(pose, Modifier.fillMaxSize(), showLabel = false)
                ReferenceBadge(tr(language,
                    "Photo unavailable · illustration shown",
                    "ፎቶው አልተገኘም፤ ምሳሌ ይታያል"
                ), Modifier.align(Alignment.BottomStart))
            }
        },
        success = {
            val imageScope = this
            val size = painter.intrinsicSize
            if (size.width.isFinite() && size.height.isFinite() && size.width > 0 && size.height > 0) {
                SideEffect { onImageSize?.invoke(IntSize(size.width.toInt(), size.height.toInt())) }
            }
            Box(Modifier.fillMaxSize()) {
                imageScope.SubcomposeAsyncImageContent(Modifier.fillMaxSize(), contentDescription = photo.altText, contentScale = ContentScale.Fit)
                if (showLabel) ReferenceBadge(if (photo.source.contains("AI-generated", ignoreCase = true))
                    tr(language, "AI-generated reference", "በAI የተፈጠረ ማጣቀሻ")
                    else tr(language, "Reference photo", "ማጣቀሻ ፎቶ"), Modifier.align(Alignment.BottomStart))
            }
        }
    )
}

@Composable
private fun ReferenceBadge(text: String, modifier: Modifier) {
    Text(text, modifier.padding(10.dp).background(Color(0xE6222928), RoundedCornerShape(20.dp))
        .padding(horizontal = 9.dp, vertical = 4.dp), color = Color.White,
        style = MaterialTheme.typography.labelSmall, maxLines = 2)
}
