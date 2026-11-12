package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.AspectRatioPreset
import com.example.model.AudioTrackItem
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.model.StickerOverlayItem
import com.example.model.TextOverlayItem
import com.example.model.VideoClip
import com.example.model.VideoEffectItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "novacut_projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val aspectRatioName: String,
    val resolutionName: String,
    val fpsName: String,
    val durationMs: Long,
    val clipCount: Int,
    val payloadJson: String,
    val lastExportedUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM novacut_projects ORDER BY updatedAt DESC")
    fun observeAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM novacut_projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: ProjectEntity)

    @Query("DELETE FROM novacut_projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Query("SELECT COUNT(*) FROM novacut_projects")
    suspend fun getProjectCount(): Int
}

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class NovaCutDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: NovaCutDatabase? = null

        fun getInstance(context: Context): NovaCutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaCutDatabase::class.java,
                    "novacut_studio.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

data class DecodedProjectPayload(
    val videoClips: List<VideoClip>,
    val videoEffects: List<VideoEffectItem>,
    val audioTracks: List<AudioTrackItem>,
    val textOverlays: List<TextOverlayItem>,
    val stickerOverlays: List<StickerOverlayItem>,
    val aspectRatio: AspectRatioPreset,
    val exportResolution: ExportResolution,
    val exportFps: ExportFps
)

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.observeAllProjects()

    suspend fun getProjectById(id: String): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun getProjectCount(): Int = projectDao.getProjectCount()

    suspend fun saveProjectState(
        id: String,
        title: String,
        aspectRatio: AspectRatioPreset,
        exportResolution: ExportResolution,
        exportFps: ExportFps,
        videoClips: List<VideoClip>,
        videoEffects: List<VideoEffectItem>,
        audioTracks: List<AudioTrackItem>,
        textOverlays: List<TextOverlayItem>,
        stickerOverlays: List<StickerOverlayItem>,
        lastExportedUri: String? = null
    ) {
        val root = JSONObject().apply {
            val clipsArr = JSONArray()
            videoClips.forEach { clipsArr.put(it.toJson()) }
            put("videoClips", clipsArr)

            val fxArr = JSONArray()
            videoEffects.forEach { fxArr.put(it.toJson()) }
            put("videoEffects", fxArr)

            val audioArr = JSONArray()
            audioTracks.forEach { audioArr.put(it.toJson()) }
            put("audioTracks", audioArr)

            val textArr = JSONArray()
            textOverlays.forEach { textArr.put(it.toJson()) }
            put("textOverlays", textArr)

            val stickerArr = JSONArray()
            stickerOverlays.forEach { stickerArr.put(it.toJson()) }
            put("stickerOverlays", stickerArr)
        }

        val totalDuration = videoClips
            .filter { it.trackIndex == 0 }
            .sumOf { it.effectiveDurationMs }
            .coerceAtLeast(1000L)

        val entity = ProjectEntity(
            id = id,
            title = title,
            aspectRatioName = aspectRatio.name,
            resolutionName = exportResolution.name,
            fpsName = exportFps.name,
            durationMs = totalDuration,
            clipCount = videoClips.size,
            payloadJson = root.toString(),
            lastExportedUri = lastExportedUri,
            updatedAt = System.currentTimeMillis()
        )
        projectDao.upsertProject(entity)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    fun decodePayload(entity: ProjectEntity): DecodedProjectPayload {
        val root = runCatching { JSONObject(entity.payloadJson) }.getOrDefault(JSONObject())
        val clipsArr = root.optJSONArray("videoClips") ?: JSONArray()
        val fxArr = root.optJSONArray("videoEffects") ?: JSONArray()
        val audioArr = root.optJSONArray("audioTracks") ?: JSONArray()
        val textArr = root.optJSONArray("textOverlays") ?: JSONArray()
        val stickerArr = root.optJSONArray("stickerOverlays") ?: JSONArray()

        val clips = (0 until clipsArr.length()).mapNotNull { idx ->
            clipsArr.optJSONObject(idx)?.let { VideoClip.fromJson(it) }
        }
        val effects = (0 until fxArr.length()).mapNotNull { idx ->
            fxArr.optJSONObject(idx)?.let { VideoEffectItem.fromJson(it) }
        }
        val audios = (0 until audioArr.length()).mapNotNull { idx ->
            audioArr.optJSONObject(idx)?.let { AudioTrackItem.fromJson(it) }
        }
        val texts = (0 until textArr.length()).mapNotNull { idx ->
            textArr.optJSONObject(idx)?.let { TextOverlayItem.fromJson(it) }
        }
        val stickers = (0 until stickerArr.length()).mapNotNull { idx ->
            stickerArr.optJSONObject(idx)?.let { StickerOverlayItem.fromJson(it) }
        }

        val aspectRatio = runCatching { AspectRatioPreset.valueOf(entity.aspectRatioName) }
            .getOrDefault(AspectRatioPreset.RATIO_9_16)
        val resolution = runCatching { ExportResolution.valueOf(entity.resolutionName) }
            .getOrDefault(ExportResolution.RES_1080P)
        val fps = runCatching { ExportFps.valueOf(entity.fpsName) }
            .getOrDefault(ExportFps.FPS_30)

        return DecodedProjectPayload(
            videoClips = clips,
            videoEffects = effects,
            audioTracks = audios,
            textOverlays = texts,
            stickerOverlays = stickers,
            aspectRatio = aspectRatio,
            exportResolution = resolution,
            exportFps = fps
        )
    }
}
