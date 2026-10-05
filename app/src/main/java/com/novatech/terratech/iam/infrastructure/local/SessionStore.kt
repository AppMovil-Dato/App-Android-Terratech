package com.novatech.terratech.iam.infrastructure.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.novatech.terratech.iam.domain.entity.Session
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionData by preferencesDataStore("session")

class SessionStore(private val context: Context) {
    private val keyName = "terratech.session.v1"
    private val pref = stringPreferencesKey("encrypted_session")
    private val gson = Gson()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(keyName, null) as? SecretKey)?.let {
            return it
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                            keyName,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                        )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
            }
            .generateKey()
    }

    suspend fun load(): Session? {
        val raw = context.sessionData.data.first()[pref] ?: return null
        return try {
            val parts = raw.split(":")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)),
            )
            val s =
                gson.fromJson(
                    String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8),
                    StoredSession::class.java,
                )
            Session(s.userId, s.email, s.fullName, s.token, Instant.parse(s.expiresAt))
        } catch (e: Exception) {
            clear()
            null
        }
    }

    suspend fun save(s: Session) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val data =
            gson
                .toJson(
                    StoredSession(s.userId, s.email, s.fullName, s.token, s.expiresAt.toString())
                )
                .toByteArray(Charsets.UTF_8)
        val raw =
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP) +
                ":" +
                Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)
        context.sessionData.edit { it[pref] = raw }
    }

    fun selection(user: Int) =
        context.sessionData.data.map { prefs ->
            prefs[intPreferencesKey("field_$user")] to prefs[intPreferencesKey("device_$user")]
        }

    suspend fun select(user: Int, field: Int?, device: Int?) {
        context.sessionData.edit { prefs ->
            val f = intPreferencesKey("field_$user")
            val d = intPreferencesKey("device_$user")
            if (field == null) prefs.remove(f) else prefs[f] = field
            if (device == null) prefs.remove(d) else prefs[d] = device
        }
    }

    suspend fun clear() {
        context.sessionData.edit { it.clear() }
    }
}
