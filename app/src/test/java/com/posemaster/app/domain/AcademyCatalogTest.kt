package com.posemaster.app.domain

import org.junit.Assert.*
import org.junit.Test

class AcademyCatalogTest {
    @Test fun everyCourseHasAnOriginalTranslatedLearningPathAndValidQuiz() {
        assertEquals(9, AcademyCatalog.courses.size)
        assertEquals(9, AcademyCatalog.courses.map { it.id }.toSet().size)
        assertEquals(27, AcademyCatalog.lessons.map { it.id }.toSet().size)
        AcademyCatalog.courses.forEach { course ->
            assertTrue(course.lessons.size >= 3)
            assertTrue(course.titleAm.isNotBlank() && course.descriptionAm.isNotBlank())
            course.lessons.forEach { lesson ->
                assertTrue("${lesson.id} needs substantive instruction", lesson.body.split(" ").size >= 100)
                assertTrue("${lesson.id} needs translated content", lesson.titleAm.isNotBlank() && lesson.bodyAm.length > 200)
                assertTrue(lesson.body.contains("Practice:"))
                assertEquals(course, AcademyCatalog.courseForLesson(lesson.id))
                assertEquals(lesson, AcademyCatalog.lesson(lesson.id))
                assertEquals(1, lesson.quiz.options.indices.count { lesson.quiz.isCorrect(it) })
                assertFalse(lesson.quiz.isCorrect(-1))
                assertFalse(lesson.quiz.isCorrect(lesson.quiz.options.size))
                assertTrue(lesson.quiz.explanation.isNotBlank() && lesson.quiz.explanationAm.isNotBlank())
            }
        }
    }

    @Test fun allBeginnerCoursesAreFreeAndAdvancedBusinessRemainsLocked() {
        assertTrue(AcademyCatalog.courses.filter { it.level == "Beginner" }.all { !it.premium })
        assertTrue(AcademyCatalog.courses.first { it.id == "business" }.premium)
        assertNull(AcademyCatalog.lesson("missing"))
        assertNull(AcademyCatalog.courseForLesson("missing"))
    }
}
