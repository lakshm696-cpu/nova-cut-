package com.example.data.cloud

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CloudProjectRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveCloudProject_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = CloudProjectRepository(firestore, auth)
        val docId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCloudProject(
                projectId = docId,
                title = "Cinema Master Cut",
                aspectRatioName = "RATIO_16_9",
                resolutionName = "RES_1080P",
                fpsName = "FPS_60",
                durationMs = 8500L,
                clipCount = 3,
                payloadJson = """{"videoClips":[]}"""
            )
        }
        assertTrue(createResult.isSuccess)
        assertEquals(docId, createResult.getOrThrow())

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCloudProjects().first { list -> list.any { it.id == docId } }
        }
        assertTrue(observed.any { it.id == docId && it.title == "Cinema Master Cut" })
    }

    @Test
    fun getCloudProjectById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CloudProjectRepository(firestore, auth)
        val docId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCloudProject(
                projectId = docId,
                title = "Alice Private Cut"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = CloudProjectRepository(firestore, auth)
        val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getCloudProjectById(aliceUid, docId)
        }
        assertTrue(crossResult.isFailure)
        val ex = crossResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun getCloudProjectById_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CloudProjectRepository(firestore, auth)
        val docId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCloudProject(
                projectId = docId,
                title = "Protected Cut"
            ).getOrThrow()
        }

        auth.signOut()
        val unauthRepo = CloudProjectRepository(firestore, auth)
        val unauthResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            unauthRepo.getCloudProjectById(aliceUid, docId)
        }
        assertTrue(unauthResult.isFailure)
        val ex = unauthResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                unauthRepo.observeCloudProjects().first()
            }
            fail("Expected IllegalStateException when observing while signed out")
        } catch (expected: Exception) {
            assertTrue(expected is IllegalStateException || expected.cause is IllegalStateException)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@novacut.test"
        const val BOB_EMAIL = "bob@novacut.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
