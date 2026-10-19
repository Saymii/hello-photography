package com.posemaster.app.data

import android.content.res.AssetManager
import com.posemaster.app.domain.Pose
import com.posemaster.app.domain.PosePhoto
import java.io.FileNotFoundException
import org.json.JSONArray
import org.json.JSONObject

/** Loads the original guides and an optional, reviewed collection of bundled reference photos. */
object PoseCatalog {
    private const val ORIGINAL_CATALOG = "poses.json"
    private const val PHOTO_CATALOG = "photo-poses.json"
    private val assetSegment = Regex("[A-Za-z0-9_-][A-Za-z0-9_.-]*")
    private val imageExtensions = setOf("webp", "jpg", "jpeg", "png")

    fun load(assets: AssetManager): List<Pose> = load(
        readAsset = { path -> assets.open(path).bufferedReader().use { it.readText() } },
        photoExists = { path ->
            try {
                assets.open(path).use { }
                true
            } catch (_: FileNotFoundException) {
                false
            }
        },
    )

    /** Only an absent optional catalog is ignored. Corrupt shipped data must fail validation. */
    internal fun load(
        readAsset: (String) -> String,
        photoExists: (String) -> Boolean,
    ): List<Pose> {
        val original = readAsset(ORIGINAL_CATALOG)
        val photoCollection = try {
            readAsset(PHOTO_CATALOG)
        } catch (_: FileNotFoundException) {
            null
        }
        return parse(original, photoCollection, photoExists)
    }

    internal fun parse(
        original: String,
        photoCollection: String?,
        photoExists: (String) -> Boolean,
    ): List<Pose> {
        val poses = parseArray(original, requirePhoto = false, photoExists = photoExists) +
            (photoCollection?.let { parseArray(it, requirePhoto = true, photoExists = photoExists) } ?: emptyList())
        val ids = mutableSetOf<Int>()
        for (pose in poses) require(ids.add(pose.id)) { "Duplicate pose ID ${pose.id}" }
        return poses
    }

    private fun parseArray(
        text: String,
        requirePhoto: Boolean,
        photoExists: (String) -> Boolean,
    ): List<Pose> {
        val entries = JSONArray(text)
        return (0 until entries.length()).map { index ->
            val entry = entries.getJSONObject(index)
            val id = entry.getInt("id")
            require(id > 0) { "Pose IDs must be positive" }
            val photo = if (entry.has("photo")) {
                parsePhoto(entry.getJSONObject("photo"), id, photoExists)
            } else {
                require(!requirePhoto) { "Photo collection pose $id needs a photo reference" }
                null
            }
            Pose(
                id = id,
                name = entry.getString("name"),
                category = entry.getString("category"),
                subject = entry.getString("subject"),
                style = entry.getString("style"),
                location = entry.getString("location"),
                position = entry.getString("position"),
                difficulty = entry.getString("difficulty"),
                premium = entry.getBoolean("premium"),
                steps = entry.getString("steps"),
                hands = entry.getString("hands"),
                feet = entry.getString("feet"),
                face = entry.getString("face"),
                photo = photo,
            )
        }
    }

    private fun parsePhoto(entry: JSONObject, poseId: Int, photoExists: (String) -> Boolean): PosePhoto {
        fun text(key: String): String {
            val value = entry.opt(key) as? String
            require(!value.isNullOrBlank()) { "Pose $poseId photo needs nonblank $key" }
            return value
        }
        val assetPath = text("assetPath")
        val segments = assetPath.split('/')
        require(
            segments.size >= 2 && segments.first() == "photos" &&
                segments.all { it.matches(assetSegment) } &&
                segments.last().substringAfterLast('.', "").lowercase() in imageExtensions
        ) { "Pose $poseId photo must be a safe bundled image under photos/" }
        val photo = PosePhoto(
            assetPath = assetPath,
            altText = text("altText"),
            source = text("source"),
            credit = text("credit"),
            license = text("license"),
            collectionId = text("collectionId"),
        )
        require(photoExists(assetPath)) { "Pose $poseId photo asset is missing: $assetPath" }
        return photo
    }
}
