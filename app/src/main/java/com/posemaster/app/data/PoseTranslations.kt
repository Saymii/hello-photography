package com.posemaster.app.data

import android.content.Context
import com.posemaster.app.domain.Pose
import org.json.JSONArray

/** Optional offline translations. IDs and filter values always remain language-independent. */
class PoseTranslations(context: Context) {
    private val assets = context.applicationContext.assets

    private data class Text(
        val name: String,
        val steps: String,
        val hands: String,
        val feet: String,
        val face: String,
    )

    // Read once, only if Amharic is selected. English remains usable if this optional asset fails.
    private val amharic: Map<Int, Text> by lazy {
        runCatching {
            val json = JSONArray(assets.open("poses-am.json").bufferedReader().use { it.readText() })
            buildMap {
                for (index in 0 until json.length()) {
                    val entry = json.optJSONObject(index) ?: continue
                    val id = entry.optInt("id", -1)
                    if (id < 0) continue
                    put(id, Text(
                        entry.optString("name"), entry.optString("steps"),
                        entry.optString("hands"), entry.optString("feet"), entry.optString("face"),
                    ))
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun localize(pose: Pose, language: String): Pose {
        if (!language.isAmharic()) return pose
        val text = amharic[pose.id] ?: return pose
        return pose.copy(
            name = text.name.ifBlank { pose.name },
            steps = text.steps.ifBlank { pose.steps },
            hands = text.hands.ifBlank { pose.hands },
            feet = text.feet.ifBlank { pose.feet },
            face = text.face.ifBlank { pose.face },
        )
    }
}

/** Display-only category labels; never store translated category names as filter or saved-data keys. */
fun categoryLabel(category: String, language: String): String =
    if (language.isAmharic()) amharicCategories[category] ?: category else category

private fun String.isAmharic(): Boolean = substringBefore('-').substringBefore('_').equals("am", true)

private val amharicCategories = mapOf(
    "Male poses" to "የወንዶች ፖዞች",
    "Female poses" to "የሴቶች ፖዞች",
    "Unisex poses" to "ለሁሉም የሚሆኑ ፖዞች",
    "Standing poses" to "የቆሙ ፖዞች",
    "Sitting poses" to "የተቀመጡ ፖዞች",
    "Walking poses" to "የእርምጃ ፖዞች",
    "Street photography" to "የጎዳና ፎቶግራፍ",
    "Fashion photography" to "የፋሽን ፎቶግራፍ",
    "Portrait photography" to "የግለሰብ ፎቶግራፍ",
    "Wedding photography" to "የሰርግ ፎቶግራፍ",
    "Couple photography" to "የጥንዶች ፎቶግራፍ",
    "Family photography" to "የቤተሰብ ፎቶግራፍ",
    "Graduation photography" to "የምረቃ ፎቶግራፍ",
    "Birthday photography" to "የልደት በዓል ፎቶግራፍ",
    "Maternity photography" to "የእርግዝና ፎቶግራፍ",
    "Professional business portraits" to "የሙያ እና የሥራ ፎቶዎች",
    "Selfie poses" to "የራስ ፎቶ ፖዞች",
    "Instagram and TikTok poses" to "የኢንስታግራም እና የቲክቶክ ፖዞች",
    "Group photography" to "የቡድን ፎቶግራፍ",
    "Indoor photography" to "የቤት ውስጥ ፎቶግራፍ",
    "Outdoor photography" to "የውጭ ፎቶግራፍ",
    "Studio photography" to "የስቱዲዮ ፎቶግራፍ",
    "Plus-size and body-inclusive posing" to "ለሁሉም የሰውነት ቅርጾች የሚሆኑ ፖዞች",
    "Accessible seated and mobility-friendly poses" to "ተደራሽ እና ለእንቅስቃሴ ፍላጎቶች የተስማሙ ፖዞች",
)
