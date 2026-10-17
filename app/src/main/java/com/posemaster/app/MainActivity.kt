package com.posemaster.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.posemaster.app.ui.HelloPhotographyApp

class MainActivity : ComponentActivity() {
 private var notificationEvent by mutableIntStateOf(0)
 private var notificationPose by mutableStateOf<Int?>(null)
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  notificationPose=intent.getIntExtra("pose_id",-1).takeIf {it>0}
  notificationEvent++
  setContent { HelloPhotographyApp(notificationPose,notificationEvent) }
 }
 override fun onNewIntent(intent:Intent) {
  super.onNewIntent(intent);setIntent(intent);notificationEvent++
  notificationPose=intent.getIntExtra("pose_id",-1).takeIf {it>0}
 }
}
