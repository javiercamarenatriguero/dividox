package com.akole.dividox.common.auth.data

import com.akole.dividox.common.auth.domain.model.AuthProvider
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JvmSessionStorageTest {

    private val tempDir = File.createTempFile("session_test", "").apply { delete(); mkdirs() }
    private val storage = JvmSessionStorage(storageDir = tempDir)

    private fun makeSession(
        uid: String = "uid-123",
        email: String? = "user@example.com",
        displayName: String? = "Test User",
        provider: AuthProvider = AuthProvider.EMAIL,
        idToken: String = "id_token",
        refreshToken: String = "refresh_token",
    ) = JvmSessionData(
        uid = uid,
        email = email,
        displayName = displayName,
        provider = provider,
        idToken = idToken,
        refreshToken = refreshToken,
    )

    // region save / load

    @Test
    fun `save and load round trip preserves all fields`() {
        // GIVEN
        val session = makeSession()

        // WHEN
        storage.save(session)
        val loaded = storage.load()

        // THEN
        assertEquals(session, loaded)
    }

    @Test
    fun `load returns null when no session has been saved`() {
        // GIVEN — fresh storage directory with no session file

        // WHEN
        val loaded = storage.load()

        // THEN
        assertNull(loaded)
    }

    @Test
    fun `save overwrites the previous session`() {
        // GIVEN
        storage.save(makeSession(uid = "old-uid"))
        val newSession = makeSession(uid = "new-uid")

        // WHEN
        storage.save(newSession)
        val loaded = storage.load()

        // THEN
        assertEquals("new-uid", loaded?.uid)
    }

    @Test
    fun `save and load preserves null email`() {
        // GIVEN
        val session = makeSession(email = null)

        // WHEN
        storage.save(session)
        val loaded = storage.load()

        // THEN
        assertNull(loaded?.email)
        assertEquals(session.uid, loaded?.uid)
    }

    @Test
    fun `save and load preserves null display name`() {
        // GIVEN
        val session = makeSession(displayName = null)

        // WHEN
        storage.save(session)
        val loaded = storage.load()

        // THEN
        assertNull(loaded?.displayName)
        assertEquals(session.uid, loaded?.uid)
    }

    @Test
    fun `save and load preserves Google provider`() {
        // GIVEN
        val session = makeSession(provider = AuthProvider.GOOGLE)

        // WHEN
        storage.save(session)
        val loaded = storage.load()

        // THEN
        assertEquals(AuthProvider.GOOGLE, loaded?.provider)
    }

    // endregion

    // region clear

    @Test
    fun `clear removes the persisted session`() {
        // GIVEN
        storage.save(makeSession())

        // WHEN
        storage.clear()
        val loaded = storage.load()

        // THEN
        assertNull(loaded)
    }

    @Test
    fun `clear on empty storage does not throw`() {
        // GIVEN — nothing saved

        // WHEN / THEN — no exception thrown
        storage.clear()
    }

    // endregion

    // region encryption

    @Test
    fun `each write produces unique ciphertext due to random IV`() {
        // GIVEN
        val session = makeSession()
        storage.save(session)
        val ciphertext1 = tempDir.resolve("session.enc").readText()

        // WHEN
        storage.save(session)
        val ciphertext2 = tempDir.resolve("session.enc").readText()

        // THEN — random IV per write → different ciphertext even for identical plaintext
        assert(ciphertext1 != ciphertext2) { "Random IV must produce unique ciphertext per write" }
        assertEquals(session, storage.load())
    }

    @Test
    fun `load returns null when session file is corrupted`() {
        // GIVEN
        tempDir.resolve("session.enc").writeText("not-valid-base64-encrypted-data!!")

        // WHEN
        val loaded = storage.load()

        // THEN — corrupt file handled gracefully, no crash
        assertNull(loaded)
    }

    // endregion
}
