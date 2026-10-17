package com.posemaster.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseAssistantTest {
    private val standing = Pose(1, "Easy stance", "Unisex poses", "Individual", "Casual", "Studio", "Standing", "Beginner", false, "Relax your shoulders.", "Rest one hand.", "Keep both feet supported.", "Look at the light.")
    private val sitting = standing.copy(id = 2, name = "Chair moment", category = "Sitting poses", location = "Home", position = "Sitting", steps = "Sit at a stable chair and breathe out.")
    private val walking = standing.copy(id = 3, name = "Park walk", category = "Walking poses", location = "Park", position = "Walking", steps = "Take a slow step.")
    private val wedding = standing.copy(id = 4, name = "Veil moment", category = "Wedding photography", subject = "Couple", location = "Outdoor", style = "Romantic", hands = "Rest your hands near the veil.")
    private val male = standing.copy(id = 5, name = "Jacket adjustment", category = "Male poses", hands = "Touch your jacket lapel.")
    private val female = standing.copy(id = 6, category = "Female poses")
    private val premium = standing.copy(id = 7, name = "Editorial study", category = "Fashion photography", style = "Fashion", difficulty = "Professional", premium = true)
    private val birthday = standing.copy(id = 8, category = "Birthday photography", location = "Home", style = "Creative")
    private val accessible = sitting.copy(id = 9, category = "Accessible seated and mobility-friendly poses", location = "Studio")
    private val poses = listOf(standing, sitting, walking, wedding, male, female, premium, birthday, accessible)

    @Test fun noInputReturnsOnlyFreeBeginnerGuidesAndHonestExplanation() {
        val result = PoseAssistant.recommend(poses, "")
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { !it.pose.premium && it.pose.difficulty == "Beginner" })
        assertTrue(result.all { "offline" in it.explanation })
    }

    @Test fun explicitPositionAndLocationAreNeverSilentlyRelaxed() {
        val result = PoseAssistant.recommend(poses, "sitting at home for a beginner")
        assertEquals(listOf(sitting), result.map { it.pose })
        assertTrue(result.single().explanation.contains("sitting position"))
        assertTrue(result.single().explanation.contains("home location"))
        assertTrue(PoseAssistant.recommend(poses, "couple sitting in a cafe").isEmpty())
    }

    @Test fun synonymsAndPunctuationAreNormalized() {
        assertEquals(listOf(walking), PoseAssistant.recommend(poses, "SOLO, strolling in the garden!").map { it.pose })
        assertTrue(PoseAssistant.recommend(poses, "sitting in a café").isEmpty())
    }

    @Test fun weddingsRequireAnAppropriateOccasionAndSubject() {
        val result = PoseAssistant.recommend(poses, "romantic wedding outdoors")
        assertEquals(listOf(wedding), result.map { it.pose })
        assertTrue(result.single().explanation.contains("wedding photography"))
        assertTrue(PoseAssistant.recommend(poses, "solo wedding").isEmpty())
    }

    @Test fun indoorAndSpecificLocationAreBothRespected() {
        assertTrue(PoseAssistant.recommend(poses, "indoor park").isEmpty())
        assertTrue(PoseAssistant.recommend(poses, "outside at home").isEmpty())
        assertEquals(listOf(walking), PoseAssistant.recommend(poses, "walking outside").map { it.pose })
    }

    @Test fun genderMatchingUsesWholeWordsAndAllowsNeutralGuides() {
        val result = PoseAssistant.recommend(poses, "women standing", limit = 20)
        assertTrue(result.any { it.pose == female })
        assertTrue(result.any { it.pose == standing })
        assertFalse(result.any { it.pose.category == "Male poses" })
        assertEquals(female, result.first().pose)
    }

    @Test fun clothingPreferenceRanksWrittenInstructionsAndIsExplained() {
        val result = PoseAssistant.recommend(poses, "a jacket", limit = 20)
        assertEquals(male, result.first().pose)
        assertTrue(result.first().explanation.contains("jacket in the guide"))
    }

    @Test fun negationExcludesOnlyTheNamedConstraint() {
        val result = PoseAssistant.recommend(poses, "not standing but sitting", limit = 20)
        assertEquals(setOf(sitting, accessible), result.map { it.pose }.toSet())
        assertFalse(PoseAssistant.recommend(poses, "no male standing", limit = 20).any { it.pose == male })
        assertFalse(PoseAssistant.recommend(poses, "not premium", limit = 20).any { it.pose.premium })
    }

    @Test fun wheelchairRequestKeepsBothAccessibilityAndSeatedRequirements() {
        assertEquals(listOf(accessible), PoseAssistant.recommend(poses, "wheelchair indoor").map { it.pose })
        assertTrue(PoseAssistant.recommend(poses, "wheelchair walking").isEmpty())
    }

    @Test fun amharicWrittenInputHasExplicitLocalSynonyms() {
        assertEquals(listOf(sitting), PoseAssistant.recommend(poses, "ተቀምጦ ቤት ጀማሪ").map { it.pose })
        assertEquals(listOf(wedding), PoseAssistant.recommend(poses, "ጥንዶች ሰርግ ውጭ").map { it.pose })
    }

    @Test fun premiumRequestsDoNotInventOrUnlockEntitlements() {
        val result = PoseAssistant.recommend(poses, "premium fashion")
        assertEquals(listOf(premium), result.map { it.pose })
        assertTrue(result.single().pose.premium)
        assertTrue(PoseAssistant.recommend(poses, "free fashion").isEmpty())
    }

    @Test fun limitAndStableOrderingAreRespected() {
        assertTrue(PoseAssistant.recommend(poses, "", limit = 0).isEmpty())
        assertEquals(2, PoseAssistant.recommend(poses, "standing", limit = 2).size)
        assertEquals(PoseAssistant.recommend(poses, "standing"), PoseAssistant.recommend(poses.reversed(), "standing"))
    }
}
