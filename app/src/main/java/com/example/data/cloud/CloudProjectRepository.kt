package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.ProjectEntity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class CloudProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val aspectRatioName: String = "RATIO_9_16",
    val resolutionName: String = "RES_1080P",
    val fpsName: String = "FPS_30",
    val durationMs: Long = 1000L,
    val clipCount: Int = 0,
    val payloadJson: String = "{}",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toProjectEntity(): ProjectEntity {
        val updatedMillis = (updatedAt ?: createdAt)?.toDate()?.time ?: System.currentTimeMillis()
        return ProjectEntity(
            id = id,
            title = title,
            aspectRatioName = aspectRatioName,
            resolutionName = resolutionName,
            fpsName = fpsName,
            durationMs = durationMs,
            clipCount = clipCount,
            payloadJson = payloadJson,
            lastExportedUri = null,
            updatedAt = updatedMillis
        )
    }
}

class CloudProjectRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeCloudProjects(): Flow<List<CloudProject>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/projects"
        emitAll(
            db.collection("users").document(uid).collection("projects")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudProject::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getCloudProjects(): Result<List<CloudProject>> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid/projects"
        try {
            val snapshot = db.collection("users").document(uid).collection("projects").get().await()
            snapshot.toObjects(
                CloudProject::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun getCloudProjectById(ownerUid: String, projectId: String): Result<CloudProject?> = runCatching {
        val path = "users/$ownerUid/projects/$projectId"
        try {
            val doc = db.collection("users").document(ownerUid)
                .collection("projects").document(projectId)
                .get()
                .await()
            doc.toObject(CloudProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun createCloudProject(
        projectId: String,
        title: String,
        aspectRatioName: String = "RATIO_9_16",
        resolutionName: String = "RES_1080P",
        fpsName: String = "FPS_30",
        durationMs: Long = 4000L,
        clipCount: Int = 1,
        payloadJson: String = "{}"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanId = projectId.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "proj_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("projects").document(cleanId)
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().ifBlank { "Untitled Cut" }.take(120),
            "aspectRatioName" to aspectRatioName.take(40),
            "resolutionName" to resolutionName.take(40),
            "fpsName" to fpsName.take(40),
            "durationMs" to durationMs.coerceIn(0L, 86_400_000L),
            "clipCount" to clipCount.coerceIn(0, 500),
            "payloadJson" to payloadJson.ifBlank { "{}" }.take(250_000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            cleanId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun upsertCloudProject(entity: ProjectEntity): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanId = entity.id.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "proj_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("projects").document(cleanId)
        val existingDoc = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        if (existingDoc != null && existingDoc.exists()) {
            val updatePayload = mapOf(
                "title" to entity.title.trim().ifBlank { "Untitled Cut" }.take(120),
                "aspectRatioName" to entity.aspectRatioName.take(40),
                "resolutionName" to entity.resolutionName.take(40),
                "fpsName" to entity.fpsName.take(40),
                "durationMs" to entity.durationMs.coerceIn(0L, 86_400_000L),
                "clipCount" to entity.clipCount.coerceIn(0, 500),
                "payloadJson" to entity.payloadJson.ifBlank { "{}" }.take(250_000),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updatePayload).await()
                cleanId
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            createCloudProject(
                projectId = cleanId,
                title = entity.title,
                aspectRatioName = entity.aspectRatioName,
                resolutionName = entity.resolutionName,
                fpsName = entity.fpsName,
                durationMs = entity.durationMs,
                clipCount = entity.clipCount,
                payloadJson = entity.payloadJson
            ).getOrThrow()
        }
    }

    suspend fun deleteCloudProject(projectId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val cleanId = projectId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val docRef = db.collection("users").document(uid).collection("projects").document(cleanId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
