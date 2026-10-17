package com.posemaster.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Reuses the original preferences file. A new role never resets legacy favorites or theme. */
data class UserSettings(
 val role:String?=null,
 val language:String="en",
 val theme:String="System",
 val notificationsEnabled:Boolean=false,
 val recentIds:List<Int> = emptyList()
)
class AppPreferences(context:Context) {
 private val prefs=context.applicationContext.getSharedPreferences("preferences",Context.MODE_PRIVATE)
 private fun read()=UserSettings(
  prefs.getString("role",null)?.takeIf { it in ROLES },
  prefs.getString("language","en")?.takeIf {it in listOf("en","am")} ?: "en",
  prefs.getString("theme","System") ?: "System",
  prefs.getBoolean("daily_notifications",false),
  prefs.getString("recent_ids","").orEmpty().split(',').mapNotNull {it.toIntOrNull()}.distinct().take(12)
 )
 private val mutable=MutableStateFlow(read())
 val state:StateFlow<UserSettings> = mutable
 private val listener=SharedPreferences.OnSharedPreferenceChangeListener {_,_->mutable.value=read()}
 init {prefs.registerOnSharedPreferenceChangeListener(listener)}
 fun setRole(role:String) {require(role in ROLES);prefs.edit().putString("role",role).apply();mutable.value=read()}
 fun setLanguage(language:String) {require(language in listOf("en","am"));prefs.edit().putString("language",language).apply();mutable.value=read()}
 fun setTheme(theme:String) {require(theme in listOf("System","Light","Dark"));prefs.edit().putString("theme",theme).apply();mutable.value=read()}
 fun setNotifications(enabled:Boolean) {prefs.edit().putBoolean("daily_notifications",enabled).apply();mutable.value=read()}
 fun recordView(id:Int) {val recent=(listOf(id)+read().recentIds).distinct().take(12);prefs.edit().putString("recent_ids",recent.joinToString(",")).apply();mutable.value=read()}
 fun clear() {prefs.edit().clear().apply();mutable.value=read()}
 companion object {val ROLES=listOf("photographer","model","everyone")}
}
