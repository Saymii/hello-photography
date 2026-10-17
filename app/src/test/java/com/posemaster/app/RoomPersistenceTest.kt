package com.posemaster.app
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.posemaster.app.data.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class RoomPersistenceTest {
 @Test fun collectionAndOfflineBookmarkSurviveReopeningDatabase() = runBlocking {
  val context=ApplicationProvider.getApplicationContext<android.content.Context>()
  context.deleteDatabase("persistence-test.db")
  var db=Room.databaseBuilder(context,PoseDatabase::class.java,"persistence-test.db").build()
  val expected=SavedPose(21,"Weekend shoot",true)
  db.saved().save(expected)
  db.close()
  db=Room.databaseBuilder(context,PoseDatabase::class.java,"persistence-test.db").build()
  assertEquals(listOf(expected),db.saved().observe().first())
  db.saved().remove(21)
  assertTrue(db.saved().observe().first().isEmpty())
  db.close()
 }
}
