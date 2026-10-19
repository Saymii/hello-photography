package com.posemaster.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.posemaster.app.domain.Pose
import kotlin.math.max
import kotlin.math.min

/** Offline photo inspection; its own window keeps navigation away from the image. */
@Composable
fun PosePhotoViewer(poses: List<Pose>, initialPoseId: Int, language: String = "en", onDismiss: () -> Unit) {
    val photos = remember(poses) { poses.filter { it.photo != null } }
    if (photos.isEmpty()) return
    var displayedId by rememberSaveable(initialPoseId) { mutableIntStateOf(initialPoseId) }
    val index = photos.indexOfFirst { it.id == displayedId }.coerceAtLeast(0)
    val pose = photos[index]
    val photo = requireNotNull(pose.photo)
    var zoom by rememberSaveable(pose.id) { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable(pose.id) { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable(pose.id) { mutableFloatStateOf(0f) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember(pose.id) { mutableStateOf(IntSize.Zero) }

    fun clampOffset(value: Offset, scale: Float): Offset {
        if (imageSize.width == 0 || imageSize.height == 0 || viewport.width == 0 || viewport.height == 0) return Offset.Zero
        val fit = min(viewport.width.toFloat() / imageSize.width, viewport.height.toFloat() / imageSize.height)
        val limitX = max(0f, (imageSize.width * fit * scale - viewport.width) / 2f)
        val limitY = max(0f, (imageSize.height * fit * scale - viewport.height) / 2f)
        return Offset(value.x.coerceIn(-limitX, limitX), value.y.coerceIn(-limitY, limitY))
    }
    fun updateZoom(value: Float, focus: Offset = Offset.Zero) {
        val next = value.coerceIn(1f, 4f)
        val offset = clampOffset((Offset(offsetX, offsetY) - focus) * (next / zoom) + focus, next)
        zoom = next
        offsetX = offset.x
        offsetY = offset.y
    }
    LaunchedEffect(viewport, imageSize) {
        val offset = clampOffset(Offset(offsetX, offsetY), zoom)
        offsetX = offset.x
        offsetY = offset.y
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        MaterialTheme(colorScheme = darkColorScheme(primary = HelloGold, background = Color(0xFF090C0B), surface = Color(0xFF090C0B))) {
            Surface(color = Color(0xFF090C0B), modifier = Modifier.fillMaxSize().testTag("photo_viewer")) {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(pose.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(if (photo.source.contains("AI-generated", true)) tr(language, "AI-generated pose reference", "በAI የተፈጠረ የአቋም ማጣቀሻ") else tr(language, "Pose reference photo", "የአቋም ማጣቀሻ ፎቶ"),
                                color = HelloGold, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("viewer_close")) {
                            Icon(Icons.Outlined.Close, tr(language, "Close full-screen photo", "ሙሉ ማያ ፎቶን ዝጋ"))
                        }
                    }
                    Box(Modifier.fillMaxWidth().weight(1f).clipToBounds().background(Color(0xFF090C0B))
                        .onSizeChanged { viewport = it }
                        .pointerInput(pose.id, viewport, imageSize) {
                            detectTransformGestures { centroid, pan, scale, _ ->
                                val next = (zoom * scale).coerceIn(1f, 4f)
                                val focus = centroid - Offset(viewport.width / 2f, viewport.height / 2f)
                                val offset = clampOffset((Offset(offsetX, offsetY) - focus) * (next / zoom) + focus + pan, next)
                                zoom = next
                                offsetX = offset.x
                                offsetY = offset.y
                            }
                        }
                        .pointerInput(pose.id, viewport, imageSize) {
                            detectTapGestures(onDoubleTap = { tap ->
                                updateZoom(if (zoom > 1f) 1f else 2.5f, tap - Offset(viewport.width / 2f, viewport.height / 2f))
                            })
                        }, contentAlignment = Alignment.Center) {
                        PoseReferenceImage(pose, Modifier.fillMaxSize().graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = offsetX
                            translationY = offsetY
                        }.testTag("photo_viewer_image"), showLabel = false, language = language, onImageSize = { imageSize = it })
                    }
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(photo.credit, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { updateZoom(zoom - .5f) }, enabled = zoom > 1f, modifier = Modifier.testTag("zoom_out")) {
                                Icon(Icons.Outlined.ZoomOut, tr(language, "Zoom out", "አሳንስ"))
                            }
                            Text("${(zoom * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("zoom_level"))
                            IconButton(onClick = { updateZoom(zoom + .5f) }, enabled = zoom < 4f, modifier = Modifier.testTag("zoom_in")) {
                                Icon(Icons.Outlined.ZoomIn, tr(language, "Zoom in", "አጉላ"))
                            }
                            IconButton(onClick = { updateZoom(1f) }, modifier = Modifier.testTag("zoom_reset")) {
                                Icon(Icons.Outlined.RestartAlt, tr(language, "Reset photo zoom", "የፎቶ ማጉላትን መልስ"))
                            }
                        }
                        Text(tr(language, "Pinch to zoom · drag to move · double-tap to zoom/reset", "በሁለት ጣቶች አጉላ፣ ጎትት፣ ለማጉላት ወይም ለመመለስ ሁለት ጊዜ ንካ"), style = MaterialTheme.typography.labelSmall)
                        if (photos.size > 1) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { displayedId = photos[index - 1].id }, enabled = index > 0, modifier = Modifier.testTag("viewer_previous")) {
                                Text(tr(language, "← Previous", "← ቀዳሚ"))
                            }
                            Text("${index + 1} / ${photos.size}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.testTag("viewer_position"))
                            TextButton(onClick = { displayedId = photos[index + 1].id }, enabled = index < photos.lastIndex, modifier = Modifier.testTag("viewer_next")) {
                                Text(tr(language, "Next →", "ቀጣይ →"))
                            }
                        }
                    }
                }
            }
        }
    }
}
