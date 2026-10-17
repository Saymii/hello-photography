package com.posemaster.app.domain

data class Pose(val id: Int, val name: String, val category: String, val subject: String, val style: String, val location: String, val position: String, val difficulty: String, val premium: Boolean, val steps: String, val hands: String, val feet: String, val face: String) {
 val angle get() = if(position == "Close-up") "Eye level; keep the lens parallel to the face." else "Start at chest height, then adjust without tilting the horizon."
 val lighting get() = "Use soft window light or open shade, with the face angled toward the light. Avoid staring into direct sun."
 val mistakes get() = "Avoid locked joints, forced expressions, cropped fingers, and a busy background. Adapt every movement for comfort."
 val photographerTip get() = "Demonstrate the action, ask permission before adjustments, and take a short sequence between movements."
 val modelTip get() = "Breathe out, relax your shoulders, and choose the range of motion that feels comfortable."
}
data class Finder(val subject: String = "Any", val style: String = "Any", val location: String = "Any", val position: String = "Any", val experience: String = "Any")
object PoseFilter {
 fun apply(poses: List<Pose>, query: String, category: String, f: Finder): List<Pose> = poses.filter {
  (query.isBlank() || "${it.name} ${it.category} ${it.steps}".contains(query, true)) &&
  (category == "All" || it.category == category) &&
  (f.subject == "Any" || f.subject == it.subject) && (f.style == "Any" || f.style == it.style) &&
  (f.location == "Any" || f.location == it.location) && (f.position == "Any" || f.position == it.position) &&
  (f.experience == "Any" || f.experience == it.difficulty)
 }
}
interface AiRecommendationService { suspend fun recommend(request: Finder): Result<List<Int>> }
class UnconfiguredAi : AiRecommendationService { override suspend fun recommend(request: Finder) = Result.failure<List<Int>>(IllegalStateException("AI requires a configured model and backend")) }
