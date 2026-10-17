package com.posemaster.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posemaster.app.data.AcademyProgress
import com.posemaster.app.data.AcademyStore
import com.posemaster.app.domain.AcademyCatalog
import com.posemaster.app.domain.AcademyCourse
import com.posemaster.app.domain.AcademyLesson

@Composable
fun LearnScreen(modifier: Modifier = Modifier, language: String = "en", onPremium: () -> Unit = {}) {
    val context = LocalContext.current
    val store = remember(context.applicationContext) { AcademyStore(context) }
    DisposableEffect(store) { onDispose { store.close() } }
    val progress by store.state.collectAsState()
    val am = language.startsWith("am")
    var courseId by rememberSaveable { mutableStateOf<String?>(null) }
    var lessonId by rememberSaveable { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val course = AcademyCatalog.courses.firstOrNull { it.id == courseId }
    val lesson = AcademyCatalog.lesson(lessonId)
    val listState = rememberLazyListState()
    LaunchedEffect(courseId, lessonId) { listState.scrollToItem(0) }

    fun openLesson(selected: AcademyLesson) {
        val parent = AcademyCatalog.courseForLesson(selected.id) ?: return
        if (parent.premium) { onPremium(); return }
        runCatching { store.visit(selected.id) }.onFailure {
            message = if (am) "ሂደቱን ማስቀመጥ አልተቻለም። የመሣሪያ ማከማቻን ይፈትሹ።" else "Progress could not be saved. Check device storage."
        }
        courseId = parent.id
        lessonId = selected.id
    }

    BackHandler(enabled = courseId != null || lessonId != null) {
        if (lessonId != null) lessonId = null else courseId = null
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (message != null) item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(message.orEmpty(), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                TextButton(onClick = { message = null }) { Text(if (am) "ዝጋ" else "Dismiss") }
            }
        }
        if (lesson != null && course != null && !course.premium) {
            item {
                TextButton(onClick = { lessonId = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (am) course.titleAm else course.title)
                }
            }
            item {
                Text(if (am) "ትምህርት ${course.lessons.indexOf(lesson) + 1} / ${course.lessons.size} • ${lesson.minutes} ደቂቃ" else "LESSON ${course.lessons.indexOf(lesson) + 1} OF ${course.lessons.size} • ${lesson.minutes} MIN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(if (am) lesson.titleAm else lesson.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(10.dp))
                Text(if (am) "ከመስመር ውጭ ይገኛል • የመጀመሪያ ትምህርት" else "Available offline • Original learning material", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (course.id == "composition" || course.id == "light") item { AcademySketch(course.id, am) }
            item {
                Text(if (am) lesson.bodyAm else lesson.body, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 27.sp))
            }
            item {
                LessonQuiz(lesson, am, lesson.id in progress.completed) {
                    runCatching { store.complete(lesson.id) }.onFailure {
                        message = if (am) "ሂደቱን ማስቀመጥ አልተቻለም።" else "Your progress could not be saved. Please try again."
                    }
                }
            }
            if (lesson.id in progress.completed) item {
                val next = course.lessons.getOrNull(course.lessons.indexOf(lesson) + 1)
                Button(onClick = { if (next != null) openLesson(next) else lessonId = null }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text(if (next != null) { if (am) "ቀጣይ ትምህርት" else "Next lesson" } else { if (am) "ወደ ኮርሱ ተመለስ" else "Back to course" })
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        } else if (course != null) {
            item {
                TextButton(onClick = { courseId = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (am) "ሁሉም ኮርሶች" else "All courses")
                }
            }
            item { CourseOverview(course, progress, am) }
            if (course.premium) item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Text(if (am) "የፕሪሚየም ኮርስ ቅድመ እይታ" else "Premium course preview", style = MaterialTheme.typography.titleMedium)
                        Text(if (am) "የትምህርት ዝርዝሩን ከታች ይመልከቱ። መዳረሻ የተረጋገጠ የGoogle Play ግዢ ይፈልጋል።" else "Browse the syllabus below. Access requires a verified Google Play purchase when subscriptions are available.")
                        Button(onClick = onPremium) { Text(if (am) "ፕሪሚየምን ይመልከቱ" else "View premium options") }
                    }
                }
            }
            items(course.lessons, key = { it.id }) { entry ->
                LessonRow(entry, course.lessons.indexOf(entry) + 1, entry.id in progress.completed, course.premium, am) { openLesson(entry) }
            }
        } else {
            item {
                Text(if (am) "የፎቶግራፊ አካዳሚ" else "Photography Academy", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(8.dp))
                Text(if (am) "በየትምህርቱ የተሻለ ፎቶ ያንሱ።" else "A better photograph, one lesson at a time.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(34.dp))
                        Text(if (am) "ትንሽ ይማሩ። ብዙ ይለማመዱ።" else "Learn a little. Create a lot.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (am) "9 ኮርሶች • 27 ትምህርቶች • 8 ነፃ ኮርሶች\nተግባራዊ ልምምድ፣ አጭር ፈተናዎችና የተቀመጠ ሂደት።" else "9 courses • 27 lessons • 8 free courses\nPractical exercises, quick quizzes, and progress saved on this device.")
                        val completed = AcademyCatalog.lessons.count { it.id in progress.completed }
                        LinearProgressIndicator(progress = { completed.toFloat() / AcademyCatalog.lessons.size }, modifier = Modifier.fillMaxWidth())
                        Text(if (am) "$completed / 27 ትምህርቶች ተጠናቀዋል" else "$completed of 27 lessons complete", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            val resume = AcademyCatalog.lesson(progress.lastLesson)
            if (resume != null) item {
                OutlinedCard(onClick = { openLesson(resume) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (am) "የመጨረሻ ትምህርትን ቀጥል" else "Resume last lesson", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(if (am) resume.titleAm else resume.title, style = MaterialTheme.typography.titleMedium)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
            items(AcademyCatalog.courses, key = { it.id }) { entry ->
                OutlinedCard(onClick = { courseId = entry.id }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(academyLevel(entry.level, am), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            if (entry.premium) {
                                Icon(Icons.Default.Lock, contentDescription = null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(if (entry.premium) { if (am) "ፕሪሚየም" else "Premium" } else { if (am) "ነፃ" else "Free" }, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(if (am) entry.titleAm else entry.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(if (am) entry.descriptionAm else entry.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val done = entry.lessons.count { it.id in progress.completed }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (am) "$done / ${entry.lessons.size} ተጠናቀዋል" else "$done / ${entry.lessons.size} complete", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            if (done == entry.lessons.size) Icon(Icons.Default.CheckCircle, contentDescription = if (am) "ኮርሱ ተጠናቋል" else "Course completed", tint = MaterialTheme.colorScheme.primary)
                            else Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = if (am) "ኮርሱን ክፈት" else "Open course")
                        }
                    }
                }
            }
        }
    }
}

private fun academyLevel(level: String, am: Boolean) = if (!am) level else when (level) {
    "Beginner" -> "ጀማሪ"
    "Intermediate" -> "መካከለኛ"
    else -> "ከፍተኛ"
}

@Composable
private fun CourseOverview(course: AcademyCourse, progress: AcademyProgress, am: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(academyLevel(course.level, am), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(if (am) course.titleAm else course.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        Text(if (am) course.descriptionAm else course.description, style = MaterialTheme.typography.bodyLarge)
        val completed = course.lessons.count { it.id in progress.completed }
        LinearProgressIndicator(progress = { completed.toFloat() / course.lessons.size }, modifier = Modifier.fillMaxWidth())
        Text(if (am) "$completed / ${course.lessons.size} ትምህርቶች ተጠናቀዋል" else "$completed of ${course.lessons.size} lessons complete", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LessonRow(lesson: AcademyLesson, number: Int, completed: Boolean, locked: Boolean, am: Boolean, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(number.toString(), Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (am) lesson.titleAm else lesson.title, style = MaterialTheme.typography.titleMedium)
                Text(if (am) "${lesson.minutes} ደቂቃ • አጭር ፈተና" else "${lesson.minutes} min • Quick quiz", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(if (locked) Icons.Default.Lock else if (completed) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                contentDescription = if (locked) { if (am) "ፕሪሚየም ትምህርት" else "Premium lesson" } else if (completed) { if (am) "ተጠናቋል" else "Completed" } else { if (am) "ትምህርትን ክፈት" else "Open lesson" }, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LessonQuiz(lesson: AcademyLesson, am: Boolean, completed: Boolean, onComplete: () -> Unit) {
    var selected by rememberSaveable(lesson.id) { mutableIntStateOf(-1) }
    var submitted by rememberSaveable(lesson.id) { mutableStateOf(false) }
    val quiz = lesson.quiz
    val correct = submitted && quiz.isCorrect(selected)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (am) "እውቀትዎን ይፈትሹ" else "Check your understanding", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Text(if (am) quiz.questionAm else quiz.question, style = MaterialTheme.typography.bodyLarge)
            quiz.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier.fillMaxWidth().selectable(selected = selected == index, role = Role.RadioButton, onClick = { selected = index; submitted = false }).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == index, onClick = null)
                    Spacer(Modifier.width(10.dp))
                    Text(if (am) quiz.optionsAm[index] else option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            Button(onClick = { submitted = true; if (quiz.isCorrect(selected)) onComplete() }, enabled = selected >= 0, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                Text(if (am) "መልስን ፈትሽ" else "Check answer")
            }
            if (submitted) {
                Text(if (correct) { if (am) "ትክክል ነው። በጣም ጥሩ!" else "Correct. Well done!" } else { if (am) "እንደገና ይሞክሩ።" else "Not quite. Try again." }, style = MaterialTheme.typography.titleMedium, color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                // An incorrect attempt gets a useful explanation but never records completion.
                Text(if (am) quiz.explanationAm else quiz.explanation)
            }
            if (completed) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(if (am) "ትምህርቱ ተጠናቋል • ሂደት ተቀምጧል" else "Lesson complete • Progress saved", fontWeight = FontWeight.SemiBold)
                }
            } else Text(if (am) "ትክክለኛ መልስ ከሰጡ በኋላ ትምህርቱ ይጠናቀቃል።" else "Answer correctly to complete this lesson.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AcademySketch(topic: String, am: Boolean) {
    val gold = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.surfaceContainerHigh
    val text = if (topic == "composition") {
        if (am) "የሦስተኛ መስመሮች፦ ፊቱን ከመገናኛው አጠገብ ያስቀምጡ።" else "Thirds sketch: a face near an intersection, with room to look into."
    } else {
        if (am) "ትልቅ የመስኮት ብርሃን፦ ወደ ብርሃኑ በትንሹ ይዙሩ።" else "Window-light sketch: turn gently toward a broad light source."
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.fillMaxWidth().height(170.dp).background(background, RoundedCornerShape(16.dp)).semantics { contentDescription = text }) {
            val face = Offset(size.width * 0.34f, size.height * 0.36f)
            if (topic == "composition") {
                for (i in 1..2) {
                    drawLine(gold.copy(alpha = 0.35f), Offset(size.width * i / 3f, 0f), Offset(size.width * i / 3f, size.height), 2f)
                    drawLine(gold.copy(alpha = 0.35f), Offset(0f, size.height * i / 3f), Offset(size.width, size.height * i / 3f), 2f)
                }
            } else {
                drawRect(gold.copy(alpha = 0.25f), Offset(size.width * 0.75f, size.height * 0.16f), Size(size.width * 0.16f, size.height * 0.65f))
                for (i in 0..2) drawLine(gold.copy(alpha = 0.45f), Offset(size.width * 0.73f, size.height * (0.25f + i * 0.2f)), face, 2f)
            }
            drawCircle(gold, 22.dp.toPx(), face, style = Stroke(3.dp.toPx()))
            drawLine(gold, Offset(face.x, face.y + 23.dp.toPx()), Offset(face.x, size.height * 0.85f), 3.dp.toPx())
            drawLine(gold, Offset(face.x - 22.dp.toPx(), size.height * 0.66f), Offset(face.x + 22.dp.toPx(), size.height * 0.66f), 3.dp.toPx())
            drawCircle(gold, 2.dp.toPx(), Offset(face.x + 8.dp.toPx(), face.y - 3.dp.toPx()))
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
