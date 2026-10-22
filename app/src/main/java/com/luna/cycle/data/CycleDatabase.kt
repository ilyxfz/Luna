package com.luna.cycle.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Database(entities = [CycleLog::class], version = 2, exportSchema = false)
abstract class CycleDatabase : RoomDatabase() {
    abstract fun cycleLogDao(): CycleLogDao

    companion object {
        @Volatile private var instance: CycleDatabase? = null

        fun getInstance(context: Context): CycleDatabase = instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }

        @Synchronized
        fun rotateEncryptionKey(context: Context): CycleDatabase {
            val database = getInstance(context)
            val oldPassphrase = DatabaseKey.getOrCreate(context)
            val newPassphrase = DatabaseKey.newPassphrase()
            database.openHelper.writableDatabase.execSQL("PRAGMA rekey = '$newPassphrase'")
            try {
                DatabaseKey.store(context, newPassphrase)
            } catch (error: Exception) {
                database.openHelper.writableDatabase.execSQL("PRAGMA rekey = '$oldPassphrase'")
                throw error
            }
            database.close()
            instance = null
            return build(context.applicationContext).also { instance = it }
        }

        private fun build(context: Context): CycleDatabase {
            val passphrase = DatabaseKey.getOrCreate(context)
            val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
            return Room.databaseBuilder(context, CycleDatabase::class.java, "luna_private.db")
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val columns = database.query("PRAGMA table_info(`cycle_logs`)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    buildSet {
                        while (cursor.moveToNext()) add(cursor.getString(nameIndex))
                    }
                }
                if ("activity" !in columns) {
                    database.execSQL("ALTER TABLE cycle_logs ADD COLUMN activity TEXT NOT NULL DEFAULT ''")
                }
                if ("sexualActivity" !in columns) {
                    database.execSQL("ALTER TABLE cycle_logs ADD COLUMN sexualActivity TEXT NOT NULL DEFAULT ''")
                }
                if ("protection" !in columns) {
                    database.execSQL("ALTER TABLE cycle_logs ADD COLUMN protection TEXT NOT NULL DEFAULT ''")
                }
                if ("cervicalMucus" !in columns) {
                    database.execSQL("ALTER TABLE cycle_logs ADD COLUMN cervicalMucus TEXT NOT NULL DEFAULT ''")
                }
            }
        }
    }
}

private object DatabaseKey {
    private const val PREFS = "luna_secure_storage"
    private const val ENTRY = "database_key"
    private const val KEY_ALIAS = "luna_database_wrapping_key"

    @Synchronized
    fun getOrCreate(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = preferences.getString(ENTRY, null)
        if (stored != null) return decrypt(stored)

        (keyStore().getKey(KEY_ALIAS, null) as? SecretKey)
            ?: createWrappingKey()
        val passphrase = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        val encodedPassphrase = Base64.encodeToString(passphrase, Base64.NO_WRAP)
        store(context, encodedPassphrase)
        return encodedPassphrase
    }

    fun newPassphrase(): String = Base64.encodeToString(
        ByteArray(32).also { java.security.SecureRandom().nextBytes(it) },
        Base64.NO_WRAP
    )

    fun store(context: Context, passphrase: String) {
        val passphraseBytes = Base64.decode(passphrase, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyStore().getKey(KEY_ALIAS, null) as SecretKey)
        val encrypted = cipher.doFinal(passphraseBytes)
        val value = "${cipher.iv.toBase64()}:${encrypted.toBase64()}"
        check(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ENTRY, value).commit()) {
            "Unable to persist the encrypted database key"
        }
    }

    private fun decrypt(value: String): String {
        val parts = value.split(":", limit = 2)
        require(parts.size == 2) { "Stored database key is invalid" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            keyStore().getKey(KEY_ALIAS, null) as SecretKey,
            GCMParameterSpec(128, parts[0].fromBase64())
        )
        return Base64.encodeToString(cipher.doFinal(parts[1].fromBase64()), Base64.NO_WRAP)
    }

    private fun createWrappingKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private fun ByteArray.toBase64() = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.fromBase64() = Base64.decode(this, Base64.NO_WRAP)
}
