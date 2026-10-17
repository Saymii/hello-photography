package com.posemaster.app.domain

import java.text.Normalizer
import java.util.Locale

/** A local, deterministic suggestion. No model, network call, or photo analysis is involved. */
data class PoseSuggestion(val pose: Pose, val explanation: String, val score: Int)

/**
 * Transparent offline recommendations from the bundled, structured catalog.
 * Explicit subject, position, location, occasion and experience requests are constraints:
 * an empty result is more useful than recommending a standing pose to a seated user.
 * Clothing and gender/style preferences only use catalog labels and written instructions.
 */
object PoseAssistant {
    fun recommend(poses: List<Pose>, request: String, limit: Int = 6): List<PoseSuggestion> {
        if (limit <= 0) return emptyList()
        val query = normalize(request)
        val subjects = terms(query, linkedMapOf(
            "Individual" to listOf("individual", "solo", "single person", "one person", "just me"),
            "Couple" to listOf("couple", "couples", "partner", "partners", "two people"),
            "Family" to listOf("family", "families", "parents", "children", "kids"),
            "Group" to listOf("group", "groups", "friends", "friendship", "team")
        ))
        val positions = terms(query, linkedMapOf(
            "Standing" to listOf("standing", "stand", "upright"),
            "Sitting" to listOf("sitting", "sit", "seated", "seat", "chair"),
            "Walking" to listOf("walking", "walk", "strolling", "stroll", "in motion"),
            "Close-up" to listOf("close up", "closeup", "face portrait")
        ))
        val locations = terms(query, linkedMapOf(
            "Studio" to listOf("studio"),
            "Home" to listOf("home", "bedroom", "living room", "house"),
            "Park" to listOf("park", "garden"),
            "Street" to listOf("street", "city", "urban", "crosswalk"),
            "Cafe" to listOf("cafe", "coffee shop")
        ))
        val styles = terms(query, linkedMapOf(
            "Casual" to listOf("casual", "relaxed", "everyday"),
            "Professional" to listOf("professional", "business", "corporate", "formal"),
            "Romantic" to listOf("romantic", "romance", "intimate"),
            "Fashion" to listOf("fashion", "editorial", "runway"),
            "Creative" to listOf("creative", "artistic", "experimental")
        ))
        val categories = terms(query, linkedMapOf(
            "Wedding photography" to listOf("wedding", "bride", "groom", "bridal"),
            "Graduation photography" to listOf("graduation", "graduate", "diploma"),
            "Birthday photography" to listOf("birthday", "candle", "cake"),
            "Maternity photography" to listOf("maternity", "pregnant", "pregnancy"),
            "Selfie poses" to listOf("selfie", "selfies"),
            "Professional business portraits" to listOf("headshot", "headshots", "linkedin"),
            "Plus-size and body-inclusive posing" to listOf("plus size", "body inclusive"),
            "Accessible seated and mobility-friendly poses" to listOf("wheelchair", "accessible", "mobility friendly")
        ))
        val experience = terms(query, linkedMapOf(
            "Beginner" to listOf("beginner", "beginners", "easy", "first time", "starting out"),
            "Intermediate" to listOf("intermediate"),
            "Professional" to listOf("advanced", "expert", "experienced")
        ))
        val environments = terms(query, linkedMapOf(
            "Indoor" to listOf("indoor", "indoors", "inside"),
            "Outdoor" to listOf("outdoor", "outdoors", "outside")
        ))
        val genders = terms(query, linkedMapOf(
            "Female poses" to listOf("female", "woman", "women", "girl", "feminine"),
            "Male poses" to listOf("male", "man", "men", "boy", "masculine")
        ))
        val plans = terms(query, linkedMapOf("Free" to listOf("free"), "Premium" to listOf("premium")))
        val blank = query.isBlank()
        val significantWords = query.split(' ').filter { it.length > 3 && it !in stopWords }.toSet()
        val clothing = listOf("jacket", "lapel", "dress", "fabric", "suit", "coat", "pocket", "veil", "hat").filter { has(query, it) && !isNegated(query, it) }

        return poses.asSequence().filter { pose ->
            subjects.accepts(pose.subject) && positions.accepts(pose.position) &&
                locations.accepts(normalizeLocation(pose.location)) && styles.accepts(pose.style) &&
                categories.accepts(pose.category) && experience.accepts(pose.difficulty) &&
                environments.accepts(if (normalizeLocation(pose.location) in setOf("Studio", "Home", "Cafe")) "Indoor" else "Outdoor") &&
                genders.excluded.none { it == pose.category } &&
                (genders.included.isEmpty() || pose.category !in genderCategories || pose.category in genders.included) &&
                plans.accepts(if (pose.premium) "Premium" else "Free") &&
                (!blank || (!pose.premium && pose.difficulty == "Beginner"))
        }.map { pose ->
            val signals = mutableListOf<String>()
            var score = 0
            fun matched(filter: Constraint, value: String, label: String, weight: Int) {
                if (value in filter.included) { score += weight; signals += label }
            }
            matched(subjects, pose.subject, "${pose.subject.lowercase(Locale.ROOT)} subject", 12)
            matched(positions, pose.position, "${pose.position.lowercase(Locale.ROOT)} position", 12)
            matched(locations, normalizeLocation(pose.location), "${pose.location.lowercase(Locale.ROOT)} location", 10)
            matched(environments, if (normalizeLocation(pose.location) in setOf("Studio", "Home", "Cafe")) "Indoor" else "Outdoor", "${if (normalizeLocation(pose.location) in setOf("Studio", "Home", "Cafe")) "indoor" else "outdoor"} setting", 5)
            matched(categories, pose.category, pose.category.lowercase(Locale.ROOT), 14)
            matched(styles, pose.style, "${pose.style.lowercase(Locale.ROOT)} style", 8)
            matched(experience, pose.difficulty, "${pose.difficulty.lowercase(Locale.ROOT)} level", 6)
            matched(genders, pose.category, "your preferred catalog collection", 6)
            val content = normalize("${pose.name} ${pose.category} ${pose.steps} ${pose.hands} ${pose.feet}")
            val clothingMatches = clothing.filter { has(content, it) }
            if (clothingMatches.isNotEmpty()) { score += clothingMatches.size * 5; signals += "${clothingMatches.joinToString()} in the guide" }
            score += significantWords.count { has(content, it) }
            if (!pose.premium) score += 2
            if (pose.difficulty == "Beginner") score += 2
            val explanation = if (signals.isEmpty()) {
                if (!pose.premium && pose.difficulty == "Beginner") "Free beginner-friendly starting point from the offline catalog."
                else "A ${pose.difficulty.lowercase(Locale.ROOT)} ${pose.position.lowercase(Locale.ROOT)} guide from the offline catalog."
            } else "Offline rules matched: ${signals.distinct().joinToString(", ")}."
            PoseSuggestion(pose, "$explanation ${pose.steps}", score)
        }.sortedWith(compareByDescending<PoseSuggestion> { it.score }.thenBy { it.pose.id })
            .take(limit).toList()
    }

    private data class Constraint(val included: Set<String>, val excluded: Set<String>) {
        fun accepts(value: String) = value !in excluded && (included.isEmpty() || value in included)
    }

    private fun terms(query: String, dictionary: Map<String, List<String>>): Constraint {
        val included = mutableSetOf<String>()
        val excluded = mutableSetOf<String>()
        dictionary.forEach { (label, aliases) ->
            aliases.forEach { alias ->
                if (has(query, alias)) {
                    if (isNegated(query, alias)) excluded += label else included += label
                }
            }
        }
        return Constraint(included, excluded)
    }

    private fun has(value: String, phrase: String): Boolean = Regex("(?:^| )${Regex.escape(phrase)}(?: |$)").containsMatchIn(value)

    private fun isNegated(query: String, phrase: String): Boolean {
        val matches = Regex("(?:^| )${Regex.escape(phrase)}(?: |$)").findAll(query)
        return matches.all { match ->
            val preceding = query.substring(0, match.range.first).trim()
            Regex("(?:^| )(?:not|no|avoid|without|exclude)(?: (?:any|a|an|the))?$").containsMatchIn(preceding)
        }
    }

    private fun normalize(value: String): String {
        var text = value.lowercase(Locale.ROOT)
        // These are explicit written requests; no gender, location, or pose is inferred from photos.
        amharicAliases.forEach { (alias, canonical) -> text = text.replace(alias, " $canonical ") }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "").replace(Regex("[^a-z0-9]+"), " ").trim().replace(Regex("\\s+"), " ")
    }

    private fun normalizeLocation(value: String) = if (normalize(value) == "cafe") "Cafe" else value
    private val genderCategories = setOf("Female poses", "Male poses")
    private val amharicAliases = linkedMapOf(
        "ጥንዶች" to "couple", "ጥንድ" to "couple", "ሰርግ" to "wedding",
        "ተቀምጦ" to "sitting", "ተቀምጣ" to "sitting", "ቆሞ" to "standing", "ቆማ" to "standing",
        "እየተራመደ" to "walking", "ቤተሰብ" to "family", "ጓደኞች" to "friends",
        "ሴት" to "woman", "ወንድ" to "man", "ልደት" to "birthday", "ምረቃ" to "graduation",
        "ጀማሪ" to "beginner", "ቤት" to "home", "ውጭ" to "outdoor", "ስቱዲዮ" to "studio"
    )
    private val stopWords = setOf("want", "would", "like", "please", "photo", "photos", "pose", "poses", "with", "that", "this", "some", "looking", "ideas", "show", "need", "give")
}
