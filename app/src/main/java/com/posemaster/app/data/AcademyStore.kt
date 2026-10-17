package com.posemaster.app.data

import android.content.Context
import android.content.SharedPreferences
import com.posemaster.app.domain.AcademyCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AcademyProgress(val completed: Set<String> = emptySet(), val lastLesson: String? = null)

/** Only academy progress is stored here; clearing it does not remove favorites or profile data. */
class AcademyStore(context: Context) : AutoCloseable {
    private val preferences = context.applicationContext.getSharedPreferences("academy", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(read())
    val state: StateFlow<AcademyProgress> = mutableState.asStateFlow()
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> mutableState.value = read() }

    init { preferences.registerOnSharedPreferenceChangeListener(listener) }

    fun complete(lessonId: String) {
        require(AcademyCatalog.lesson(lessonId) != null) { "Unknown academy lesson" }
        val completed = read().completed + lessonId
        preferences.edit().putStringSet("completed", completed).putString("lastLesson", lessonId).apply()
        mutableState.value = read()
    }

    fun visit(lessonId: String) {
        require(AcademyCatalog.lesson(lessonId) != null) { "Unknown academy lesson" }
        preferences.edit().putString("lastLesson", lessonId).apply()
        mutableState.value = read()
    }

    fun clear() {
        preferences.edit().clear().apply()
        mutableState.value = AcademyProgress()
    }

    private fun read() = AcademyProgress(
        preferences.getStringSet("completed", emptySet())?.toSet().orEmpty(),
        preferences.getString("lastLesson", null),
    )

    override fun close() { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
}
