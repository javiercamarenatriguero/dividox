package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.model.AuthProvider
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

/**
 * Desktop-only encrypted session storage using AES-256/GCM.
 * Stores [JvmSessionData] (including tokens) to ~/.dividox/session.enc.
 * Random IV per write prevents ciphertext analysis (OWASP MASVS-STORAGE-1).
 */
internal class JvmSessionStorage(
    storageDir: File = File(System.getProperty("user.home"), ".dividox"),
) {
    private val sessionFile = File(storageDir, "session.enc")
    private val keyFile = File(storageDir, "session.key")
    private val key: SecretKey by lazy { loadOrGenerateKey() }

    init {
        if (!storageDir.exists()) storageDir.mkdirs()
    }

    fun save(session: JvmSessionData) {
        val json = toJson(session)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
        sessionFile.writeText(Base64.getEncoder().encodeToString(iv + encrypted))
    }

    fun load(): JvmSessionData? = runCatching {
        val payload = sessionFile.readText()
        val ivAndEncrypted = Base64.getDecoder().decode(payload)
        val iv = ivAndEncrypted.sliceArray(0 until 12)
        val encrypted = ivAndEncrypted.sliceArray(12 until ivAndEncrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        fromJson(String(cipher.doFinal(encrypted), Charsets.UTF_8))
    }.getOrNull()

    fun clear() {
        sessionFile.delete()
    }

    private fun loadOrGenerateKey(): SecretKey {
        if (keyFile.exists()) {
            val encoded = Base64.getDecoder().decode(keyFile.readText())
            return SecretKeySpec(encoded, "AES")
        }
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        keyFile.writeText(Base64.getEncoder().encodeToString(key.encoded))
        return key
    }

    private fun toJson(s: JvmSessionData) =
        """{"uid":"${s.uid}","email":"${s.email}","displayName":"${s.displayName}","provider":"${s.provider.name}","idToken":"${s.idToken}","refreshToken":"${s.refreshToken}"}"""

    private fun fromJson(json: String): JvmSessionData {
        fun extract(key: String) = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"").find(json)?.groupValues?.get(1) ?: ""
        return JvmSessionData(
            uid = extract("uid"),
            email = extract("email").takeIf { it != "null" && it.isNotEmpty() },
            displayName = extract("displayName").takeIf { it != "null" && it.isNotEmpty() },
            provider = AuthProvider.valueOf(extract("provider")),
            idToken = extract("idToken"),
            refreshToken = extract("refreshToken"),
        )
    }
}
