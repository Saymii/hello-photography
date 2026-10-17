package com.posemaster.app.data

import android.content.Context
import androidx.room.*
import androidx.room.withTransaction
import com.posemaster.app.domain.Pose
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

@Entity(tableName = "saved_poses")
data class SavedPose(@PrimaryKey val poseId: Int, val collection: String = "My favorites", val downloaded: Boolean = false)
@Dao interface SavedDao {
 @Query("SELECT * FROM saved_poses ORDER BY poseId") fun observe(): Flow<List<SavedPose>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(pose: SavedPose)
 @Query("SELECT COUNT(*) FROM saved_poses") suspend fun count(): Int
 @Query("SELECT COUNT(*) FROM saved_poses WHERE poseId = :id") suspend fun contains(id:Int): Int
 @Query("DELETE FROM saved_poses") suspend fun clear()
 @Query("DELETE FROM saved_poses WHERE poseId = :id") suspend fun remove(id: Int)
}
@Database(entities = [SavedPose::class], version = 1, exportSchema = false)
abstract class PoseDatabase : RoomDatabase() { abstract fun saved(): SavedDao }
class PoseRepository(context: Context) {
 private val db = Room.databaseBuilder(context.applicationContext, PoseDatabase::class.java, "posemaster.db").build()
 val saved = db.saved().observe()
 val poses: List<Pose> = JSONArray(context.assets.open("poses.json").bufferedReader().use { it.readText() }).let { a ->
  (0 until a.length()).map { index -> a.getJSONObject(index).let {
   Pose(it.getInt("id"), it.getString("name"), it.getString("category"), it.getString("subject"), it.getString("style"), it.getString("location"), it.getString("position"), it.getString("difficulty"), it.getBoolean("premium"), it.getString("steps"), it.getString("hands"), it.getString("feet"), it.getString("face"))
  } }
 }
 suspend fun save(pose: SavedPose):Boolean = db.withTransaction {
  if(db.saved().contains(pose.poseId)>0 || db.saved().count()<FREE_FAVORITE_LIMIT) { db.saved().save(pose); true } else false
 }
 suspend fun clear() = db.saved().clear()
 companion object { const val FREE_FAVORITE_LIMIT=20 }
 suspend fun remove(id: Int) = db.saved().remove(id)
}
