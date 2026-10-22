package com.luna.cycle.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class CycleBackup(val logs: List<CycleLog>, val lastPeriod: String?)

object EncryptedBackup {
    private val magic = "LUNABAK1".toByteArray(Charsets.US_ASCII)
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val KEY_ITERATIONS = 210_000

    fun write(context: Context, uri: Uri, logs: List<CycleLog>, lastPeriod: String, passphrase: String) {
        require(passphrase.length >= 8) { "Use a passphrase with at least 8 characters." }
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_BYTES).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(serialize(logs, lastPeriod).toByteArray(Charsets.UTF_8))
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("Unable to open the selected backup destination.")
        stream.use {
            it.write(magic)
            it.write(salt)
            it.write(iv)
            it.write(encrypted)
        }
    }

    fun read(context: Context, uri: Uri, passphrase: String): CycleBackup {
        require(passphrase.length >= 8) { "Use the passphrase created for this backup." }
        val stream = context.contentResolver.openInputStream(uri)
            ?: error("Unable to open the selected backup file.")
        val bytes = stream.use { it.readBytes() }
        val headerSize = magic.size + SALT_BYTES + IV_BYTES
        require(bytes.size > headerSize + 16) { "The selected file is not a valid Luna backup." }
        require(bytes.copyOfRange(0, magic.size).contentEquals(magic)) {
            "The selected file is not a valid Luna backup."
        }

        val saltStart = magic.size
        val ivStart = saltStart + SALT_BYTES
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            deriveKey(passphrase, bytes.copyOfRange(saltStart, ivStart)),
            GCMParameterSpec(128, bytes.copyOfRange(ivStart, headerSize))
        )
        val json = cipher.doFinal(bytes.copyOfRange(headerSize, bytes.size)).toString(Charsets.UTF_8)
        return deserialize(json)
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, KEY_ITERATIONS, 256)
        return try {
            SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    private fun serialize(logs: List<CycleLog>, lastPeriod: String): String {
        val entries = JSONArray()
        logs.forEach { log ->
            entries.put(
                JSONObject()
                    .put("date", log.date)
                    .put("flow", log.flow)
                    .put("symptoms", log.symptoms)
                    .put("mood", log.mood)
                    .put("note", log.note)
                    .put("waterGlasses", log.waterGlasses)
                    .put("sleepQuality", log.sleepQuality)
                    .put("activity", log.activity)
                    .put("sexualActivity", log.sexualActivity)
                    .put("protection", log.protection)
                    .put("cervicalMucus", log.cervicalMucus)
                    .put("updatedAt", log.updatedAt)
            )
        }
        return JSONObject().put("version", 1).put("lastPeriod", lastPeriod).put("logs", entries).toString()
    }

    private fun deserialize(json: String): CycleBackup {
        val root = JSONObject(json)
        require(root.getInt("version") == 1) { "This Luna backup version is not supported." }
        val entries = root.getJSONArray("logs")
        val logs = List(entries.length()) { index ->
            val entry = entries.getJSONObject(index)
            CycleLog(
                date = entry.getString("date"),
                flow = entry.optString("flow"),
                symptoms = entry.optString("symptoms"),
                mood = entry.optString("mood"),
                note = entry.optString("note"),
                waterGlasses = entry.optInt("waterGlasses"),
                sleepQuality = entry.optInt("sleepQuality"),
                activity = entry.optString("activity"),
                sexualActivity = entry.optString("sexualActivity"),
                protection = entry.optString("protection"),
                cervicalMucus = entry.optString("cervicalMucus"),
                updatedAt = entry.optLong("updatedAt")
            )
        }
        return CycleBackup(logs, root.optString("lastPeriod").takeIf(String::isNotBlank))
    }
}
