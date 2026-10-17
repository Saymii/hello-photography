package com.posemaster.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posemaster.app.domain.Pose

/** Original vector artwork, drawn locally. It does not use or imitate uploaded photographs. */
@Composable
fun OriginalPoseArt(pose: Pose, modifier: Modifier = Modifier, showLabel: Boolean = true) {
    val palette = artPalettes[Math.floorMod(pose.id, artPalettes.size)]
    Box(modifier.semantics(mergeDescendants = true) { contentDescription = poseArtDescription(pose) }) {
        Canvas(Modifier.matchParentSize()) {
            withTransform({ scale(size.width / 300f, size.height / 360f, pivot = Offset.Zero) }) {
                drawRect(Brush.verticalGradient(listOf(palette.paper, palette.paper.copy(red = (palette.paper.red * .91f).coerceIn(0f, 1f))), startY = 0f, endY = 360f), size = Size(300f, 360f))
                // A photographic set: window, arch, outdoor horizon or urban geometry.
                drawSet(pose, palette)
                val number = when (pose.subject) { "Couple" -> 2; "Family" -> 3; "Group" -> 4; else -> 1 }
                if (pose.position == "Close-up") {
                    drawPortrait(pose, palette)
                } else if (number == 1) {
                    drawPerson(pose, palette, 150f, 308f, 1f, pose.id % 4)
                } else {
                    val scale = if (number == 2) .83f else .65f
                    repeat(number) { index ->
                        val x = 150f + (index - (number - 1) / 2f) * if (number == 2) 92f else 63f
                        val child = pose.subject == "Family" && index == 1
                        drawPerson(pose, if (index % 2 == 0) palette else palette.copy(cloth = palette.accent, accent = palette.cloth), x, 308f - if (index % 2 == 0) 0f else 5f, if (child) .47f else scale, index + pose.id)
                    }
                    if (number == 2) {
                        drawLine(palette.skin, Offset(136f, 213f), Offset(164f, 213f), 10f, StrokeCap.Round)
                    }
                }
                drawPoseProps(pose, palette)
                // Corner marks suggest a composition guide without claiming this is a camera preview.
                val ink = palette.ink.copy(alpha = .22f)
                drawLine(ink, Offset(18f, 34f), Offset(18f, 18f), 1f)
                drawLine(ink, Offset(18f, 18f), Offset(34f, 18f), 1f)
                drawLine(ink, Offset(282f, 34f), Offset(282f, 18f), 1f)
                drawLine(ink, Offset(282f, 18f), Offset(266f, 18f), 1f)
            }
        }
        if (showLabel) Text(
            "Original illustration",
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                .background(Color(0xE6222928), RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

fun poseArtDescription(pose: Pose): String {
    val subjects = when (pose.subject) { "Couple" -> "two people"; "Family" -> "a family of three"; "Group" -> "a group of four"; else -> "one person" }
    val posture = when {
        pose.category.contains("Accessible") -> "seated with mobility support"
        pose.position == "Close-up" -> "in a close-up portrait"
        pose.position == "Sitting" -> "sitting with a relaxed posture"
        pose.position == "Walking" -> "taking a natural step"
        else -> "standing in a relaxed pose"
    }
    return "Original illustration for ${pose.name}: $subjects $posture. Reference the written instructions for the complete pose."
}

private data class ArtPalette(val paper: Color, val accent: Color, val cloth: Color, val skin: Color, val ink: Color = Color(0xFF30352F))
private val artPalettes = listOf(
    ArtPalette(Color(0xFFE8DFD0), Color(0xFFB58160), Color(0xFF4C5C50), Color(0xFFBB8362)),
    ArtPalette(Color(0xFFE9E6DC), Color(0xFF9FA58E), Color(0xFF6F4D43), Color(0xFF845640)),
    ArtPalette(Color(0xFFEBDDD4), Color(0xFFCF9C81), Color(0xFF3A4C50), Color(0xFFCA9772)),
    ArtPalette(Color(0xFFDFE3DD), Color(0xFFADB9A7), Color(0xFF8E6553), Color(0xFF9A664C)),
    ArtPalette(Color(0xFFEFE4CD), Color(0xFFD0B47A), Color(0xFF57576C), Color(0xFFD6A47E)),
    ArtPalette(Color(0xFFE5DFD9), Color(0xFFBA9B85), Color(0xFF3D5B60), Color(0xFF704936))
)

private fun DrawScope.drawSet(pose: Pose, p: ArtPalette) {
    val outside = pose.location in setOf("Outdoor", "Park", "Street")
    if (outside) {
        drawCircle(p.accent.copy(alpha = .40f), 62f, Offset(225f, 79f))
        if (pose.location == "Street") {
            drawRect(p.ink.copy(alpha = .06f), Offset(18f, 70f), Size(64f, 205f))
            drawRect(p.accent.copy(alpha = .28f), Offset(225f, 108f), Size(62f, 170f))
            repeat(3) { drawRect(p.paper, Offset(33f, 90f + it * 53f), Size(26f, 33f)) }
            drawPath(Path().apply { moveTo(0f, 320f); lineTo(300f, 276f); lineTo(300f, 360f); lineTo(0f, 360f); close() }, p.ink.copy(alpha = .06f))
        } else {
            drawPath(Path().apply { moveTo(0f, 250f); cubicTo(90f, 200f, 150f, 276f, 300f, 224f); lineTo(300f, 360f); lineTo(0f, 360f); close() }, p.accent.copy(alpha = .23f))
            drawPath(Path().apply { moveTo(95f, 360f); cubicTo(115f, 308f, 145f, 294f, 190f, 259f); lineTo(213f, 259f); cubicTo(176f, 305f, 169f, 332f, 166f, 360f); close() }, p.paper.copy(alpha = .62f))
            drawLine(p.ink.copy(alpha = .15f), Offset(40f, 255f), Offset(40f, 139f), 3f, StrokeCap.Round)
            drawOval(p.accent.copy(alpha = .33f), Offset(13f, 112f), Size(53f, 101f))
        }
    } else {
        drawRoundRect(p.accent.copy(alpha = .22f), Offset(58f, 24f), Size(184f, 274f), CornerRadius(92f, 92f))
        drawRect(p.paper.copy(alpha = .33f), Offset(58f, 190f), Size(184f, 108f))
        if (pose.location == "Home") {
            drawRoundRect(p.paper.copy(alpha = .6f), Offset(216f, 65f), Size(56f, 146f), CornerRadius(2f))
            drawLine(p.accent.copy(alpha = .7f), Offset(244f, 65f), Offset(244f, 211f), 2f)
            drawLine(p.accent.copy(alpha = .7f), Offset(216f, 132f), Offset(272f, 132f), 2f)
        }
        drawRect(p.ink.copy(alpha = .045f), Offset(0f, 300f), Size(300f, 60f))
        drawLine(p.ink.copy(alpha = .13f), Offset(0f, 300f), Offset(300f, 300f), 1f)
    }
}

private fun DrawScope.drawPerson(pose: Pose, p: ArtPalette, x: Float, floor: Float, personScale: Float, variant: Int) {
    withTransform({ translate(x, floor); scale(personScale, personScale, pivot = Offset.Zero) }) {
        drawOval(p.ink.copy(alpha = .12f), Offset(-54f, -5f), Size(108f, 13f))
        val sitting = pose.position == "Sitting"
        val walking = pose.position == "Walking"
        val mobility = pose.category.contains("Accessible")
        val leaning = pose.id == 5 || pose.id == 7 || pose.id == 3
        val torsoX = if (leaning) -8f else 0f
        if (sitting) {
            drawRoundRect(p.ink.copy(alpha = .22f), Offset(-48f, -87f), Size(91f, 12f), CornerRadius(5f))
            drawLine(p.ink.copy(alpha = .35f), Offset(-36f, -76f), Offset(-40f, -1f), 6f, StrokeCap.Round)
            drawLine(p.ink.copy(alpha = .35f), Offset(32f, -76f), Offset(39f, -1f), 6f, StrokeCap.Round)
            if (mobility) {
                drawCircle(p.ink.copy(alpha = .65f), 40f, Offset(-20f, -40f), style = Stroke(5f))
                drawCircle(p.accent, 4f, Offset(-20f, -40f))
                drawLine(p.ink, Offset(-20f, -40f), Offset(36f, -26f), 4f, StrokeCap.Round)
            }
        }
        val hipY = if (sitting) -93f else -112f
        val shoulderY = if (sitting) -169f else -192f
        val headY = shoulderY - 36f
        // Wide tapered limbs, layered clothing and a curved torso create an editorial silhouette.
        if (sitting) {
            limb(p.cloth.copy(red = (p.cloth.red * .78f).coerceIn(0f, 1f)), Offset(-15f, hipY + 2), Offset(43f, hipY + 14), Offset(46f, -12f), 23f)
            limb(p.cloth, Offset(7f, hipY + 7), Offset(-3f, hipY + 32), Offset(10f, -10f), 25f)
            drawRoundRect(p.ink, Offset(33f, -12f), Size(33f, 12f), CornerRadius(5f))
            drawRoundRect(p.ink, Offset(2f, -11f), Size(32f, 12f), CornerRadius(5f))
        } else if (walking) {
            limb(p.cloth.copy(alpha = .83f), Offset(-13f, hipY), Offset(-29f, -61f), Offset(-58f, -7f), 23f)
            limb(p.cloth, Offset(13f, hipY), Offset(31f, -54f), Offset(41f, -3f), 25f)
            drawLine(p.ink, Offset(-62f, -3f), Offset(-40f, -1f), 12f, StrokeCap.Round)
            drawLine(p.ink, Offset(37f, -2f), Offset(60f, -2f), 12f, StrokeCap.Round)
        } else {
            limb(p.cloth.copy(alpha = .88f), Offset(-15f, hipY), Offset(-17f, -58f), Offset(-23f, -9f), 24f)
            limb(p.cloth, Offset(12f, hipY), Offset(if (variant % 2 == 0) 25f else 6f, -60f), Offset(if (variant % 2 == 0) 38f else 15f, -8f), 24f)
            drawLine(p.ink, Offset(-27f, -3f), Offset(-8f, -3f), 12f, StrokeCap.Round)
            val foot = if (variant % 2 == 0) 38f else 15f
            drawLine(p.ink, Offset(foot - 1f, -3f), Offset(foot + 21f, -3f), 12f, StrokeCap.Round)
        }
        val torso = Path().apply {
            moveTo(torsoX - 26f, shoulderY + 4f)
            quadraticTo(torsoX, shoulderY - 7f, torsoX + 27f, shoulderY + 4f)
            lineTo(24f, hipY + 10f)
            quadraticTo(0f, hipY + 20f, -27f, hipY + 8f)
            close()
        }
        drawPath(torso, p.cloth)
        drawLine(p.paper.copy(alpha = .30f), Offset(torsoX - 1f, shoulderY + 7f), Offset(1f, hipY + 5f), 1.4f)
        drawRoundRect(p.skin, Offset(torsoX - 8f, shoulderY - 22f), Size(16f, 28f), CornerRadius(5f))
        val headX = torsoX + if (variant % 3 == 0) -4f else 2f
        drawOval(p.skin, Offset(headX - 19f, headY - 24f), Size(37f, 48f))
        drawPath(Path().apply { moveTo(headX - 19f, headY); cubicTo(headX - 25f, headY - 38f, headX + 22f, headY - 34f, headX + 18f, headY - 6f); quadraticTo(headX + 8f, headY - 16f, headX - 12f, headY - 12f); close() }, p.ink)
        drawLine(p.ink.copy(alpha = .6f), Offset(headX + 4f, headY + 2f), Offset(headX + 9f, headY + 2f), 1.5f, StrokeCap.Round)
        when {
            sitting -> {
                limb(p.skin, Offset(torsoX - 27f, shoulderY + 12f), Offset(-39f, -116f), Offset(-9f, -88f), 11f)
                limb(p.skin, Offset(torsoX + 28f, shoulderY + 12f), Offset(39f, -117f), Offset(25f, -88f), 11f)
            }
            walking -> {
                limb(p.skin, Offset(torsoX - 27f, shoulderY + 12f), Offset(-47f, -151f), Offset(-63f, -127f), 12f)
                limb(p.skin, Offset(torsoX + 28f, shoulderY + 12f), Offset(40f, -144f), Offset(54f, -118f), 12f)
            }
            pose.id == 4 || variant % 4 == 0 -> {
                limb(p.skin, Offset(torsoX - 27f, shoulderY + 12f), Offset(-44f, -144f), Offset(-29f, -120f), 12f)
                limb(p.skin, Offset(torsoX + 28f, shoulderY + 12f), Offset(41f, -158f), Offset(6f, -181f), 12f)
            }
            variant % 4 == 1 -> {
                limb(p.skin, Offset(torsoX - 27f, shoulderY + 12f), Offset(-47f, -143f), Offset(-17f, -122f), 12f)
                limb(p.skin, Offset(torsoX + 28f, shoulderY + 12f), Offset(36f, -140f), Offset(31f, -112f), 12f)
            }
            variant % 4 == 2 -> {
                limb(p.skin, Offset(torsoX - 27f, shoulderY + 12f), Offset(-43f, -153f), Offset(-6f, -170f), 12f)
                limb(p.skin, Offset(torsoX + 28f, shoulderY + 12f), Offset(38f, -146f), Offset(17f, -164f), 12f)
            }
            else -> {
                limb(p.skin, Offset(torsoX - 27f, shoulderY + 12f), Offset(-35f, -142f), Offset(-32f, -112f), 12f)
                limb(p.skin, Offset(torsoX + 28f, shoulderY + 12f), Offset(47f, -151f), Offset(22f, -128f), 12f)
            }
        }
        // Sleeves cover the upper arm; visible hands remain a different warm material.
        drawLine(p.cloth, Offset(torsoX - 26f, shoulderY + 8f), Offset(torsoX - 30f, shoulderY + 28f), 20f, StrokeCap.Round)
        drawLine(p.cloth, Offset(torsoX + 26f, shoulderY + 8f), Offset(torsoX + 31f, shoulderY + 27f), 20f, StrokeCap.Round)
    }
}

private fun DrawScope.limb(color: Color, from: Offset, bend: Offset, to: Offset, width: Float) {
    drawLine(color, from, bend, width, StrokeCap.Round)
    drawLine(color, bend, to, width * .82f, StrokeCap.Round)
}

private fun DrawScope.drawPortrait(pose: Pose, p: ArtPalette) {
    val profile = pose.id % 2 == 0
    drawOval(p.ink.copy(alpha = .06f), Offset(51f, 294f), Size(198f, 25f))
    drawPath(Path().apply { moveTo(43f, 330f); cubicTo(42f, 215f, 257f, 216f, 258f, 330f); close() }, p.cloth)
    drawRoundRect(p.skin, Offset(132f, 187f), Size(41f, 67f), CornerRadius(17f))
    drawOval(p.skin, Offset(102f, 92f), Size(101f, 127f))
    drawPath(Path().apply { moveTo(103f, 171f); cubicTo(74f, 61f, 219f, 57f, 203f, 142f); quadraticTo(180f, 102f, 123f, 122f); lineTo(116f, 166f); close() }, p.ink)
    if (profile) {
        drawPath(Path().apply { moveTo(194f, 136f); lineTo(211f, 160f); lineTo(194f, 166f); close() }, p.skin)
        drawLine(p.ink.copy(alpha = .75f), Offset(176f, 148f), Offset(187f, 148f), 2f, StrokeCap.Round)
        drawLine(p.ink.copy(alpha = .45f), Offset(186f, 181f), Offset(194f, 181f), 1.5f, StrokeCap.Round)
    } else {
        drawLine(p.ink.copy(alpha = .75f), Offset(125f, 149f), Offset(136f, 150f), 2f, StrokeCap.Round)
        drawLine(p.ink.copy(alpha = .75f), Offset(164f, 150f), Offset(175f, 149f), 2f, StrokeCap.Round)
        drawPath(Path().apply { moveTo(139f, 182f); quadraticTo(150f, 190f, 165f, 181f) }, p.ink.copy(alpha = .50f), style = Stroke(1.8f, cap = StrokeCap.Round))
    }
    if (pose.category == "Selfie poses") {
        drawRoundRect(p.ink, Offset(62f, 156f), Size(40f, 70f), CornerRadius(6f))
        drawRoundRect(p.accent, Offset(66f, 161f), Size(32f, 58f), CornerRadius(3f))
        drawCircle(p.ink, 3f, Offset(73f, 169f))
        limb(p.skin, Offset(94f, 291f), Offset(81f, 250f), Offset(86f, 214f), 18f)
    }
}

private fun DrawScope.drawPoseProps(pose: Pose, p: ArtPalette) {
    when {
        pose.category.contains("Graduation") -> {
            drawPath(Path().apply { moveTo(120f, 76f); lineTo(151f, 63f); lineTo(183f, 76f); lineTo(151f, 88f); close() }, p.ink)
            drawLine(p.accent, Offset(179f, 77f), Offset(179f, 108f), 2f)
            drawLine(p.paper, Offset(144f, 166f), Offset(171f, 166f), 9f, StrokeCap.Round)
        }
        pose.category.contains("Birthday") -> {
            repeat(12) { n ->
                val x = 32f + (n * 31 % 238)
                val y = 49f + (n * 41 % 140)
                drawLine(if (n % 2 == 0) p.accent else p.cloth.copy(alpha = .4f), Offset(x, y), Offset(x + 3f, y + 6f), 3f, StrokeCap.Round)
            }
        }
        pose.category.contains("Wedding") -> {
            drawOval(p.paper.copy(alpha = .80f), Offset(69f, 110f), Size(40f, 125f))
            repeat(5) { n -> drawCircle(p.accent, 6f, Offset(137f + n % 3 * 8f, 206f + n / 3 * 9f)) }
        }
        pose.category.contains("Maternity") -> {
            drawOval(p.cloth, Offset(134f, 157f), Size(45f, 49f))
            drawArc(p.skin, startAngle = 28f, sweepAngle = 119f, useCenter = false, topLeft = Offset(130f, 164f), size = Size(46f, 35f), style = Stroke(8f, cap = StrokeCap.Round))
        }
        pose.category.contains("Fashion") -> {
            drawPath(Path().apply { moveTo(160f, 133f); cubicTo(210f, 143f, 200f, 205f, 252f, 196f); cubicTo(223f, 237f, 183f, 223f, 158f, 187f); close() }, p.accent.copy(alpha = .75f))
        }
    }
}
