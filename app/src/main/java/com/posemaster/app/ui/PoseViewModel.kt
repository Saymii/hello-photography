package com.posemaster.app.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.posemaster.app.data.*
import com.posemaster.app.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class PoseViewModel(app: Application) : AndroidViewModel(app) {
 private val repository = PoseRepository(app)
 val errors = MutableSharedFlow<String>()
 val loading = MutableStateFlow(true)
 val poses = repository.poses
 val saved = repository.saved.onEach { loading.value=false }.catch { loading.value=false; errors.emit("Saved guides could not be loaded. Restart the app and check device storage."); emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 fun save(id: Int, collection: String, download: Boolean) { viewModelScope.launch { runCatching { repository.save(SavedPose(id, collection.ifBlank { "My favorites" }, download)) }.onFailure { errors.emit("Could not save this guide. Check device storage and try again.") } } }
 fun remove(id: Int) { viewModelScope.launch { runCatching { repository.remove(id) }.onFailure { errors.emit("Could not remove this guide. Please try again.") } } }
}
