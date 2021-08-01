package com.example.ecca_encryption.database

import android.content.Context
import androidx.room.*
import com.example.ecc_library.cryptography.EncryptionService
import com.example.ecca_encryption.Algorithm
import com.example.ecca_encryption.Algorithm.*
import com.example.ecca_encryption.entities.User
import com.example.ecca_encryption.entities.UserDao
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

@Database(entities = [User::class], version = 1)
abstract class SecuredDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var securedDB: SecuredDatabase? = null

        @Volatile
        private var securedDBSecure: SecuredDatabase? = null

        @Volatile
        private var securedDBSecureWithMemorySecurity: SecuredDatabase? = null

        fun getInstance(
            context: Context,
            algorithm: Algorithm,
            secure: Boolean = false,
            memorySecure: Boolean = false
        ): SecuredDatabase {
            return if (secure) {
                if (!memorySecure) {
                    securedDBSecure ?: synchronized(this) {
                        securedDBSecure ?: buildDatabase(
                            context,
                            algorithm,
                            secure,
                            memorySecure
                        ).also { securedDBSecure = it }
                    }
                } else {
                    securedDBSecureWithMemorySecurity ?: synchronized(this) {
                        securedDBSecureWithMemorySecurity ?: buildDatabase(
                            context,
                            algorithm,
                            secure,
                            memorySecure
                        ).also { securedDBSecureWithMemorySecurity = it }
                    }
                }
            } else {
                securedDB ?: synchronized(this) {
                    securedDB ?: buildDatabase(context,algorithm, secure).also {
                        securedDB = it
                    }
                }
            }
        }

        private fun buildDatabase(
            context: Context,
            algorithm: Algorithm,
            secure: Boolean,
            memorySecure: Boolean = false
        ): SecuredDatabase {
            var dbname = "not-encrypted"
            var factory: SupportFactory? = null
            if (secure) {
                var passphrase: ByteArray? = null;
                val encryptionService = EncryptionService();
                if (algorithm == AES) {
                    dbname = "encrypted-aes"
                    passphrase = encryptionService.prepareKeyECDH()
                } else if (algorithm == ECDH) {
                    dbname = "encrypted-ecdh"
                    passphrase = encryptionService.prepareKeyAES()
                }
                if (memorySecure) {
                    dbname = "$dbname-with-mem"
                }
                factory = getSupportFactory(passphrase, memorySecure)
            }
            val builder = Room.databaseBuilder(
                context.applicationContext,
                SecuredDatabase::class.java, "${dbname}.db"
            )
            if(factory == null) {
                builder.openHelperFactory(factory)
            }
            return builder.build()
        }

        private fun getSupportFactory(
            passphrase: ByteArray?,
            memorySecure: Boolean
        ): SupportFactory {
            return SupportFactory(passphrase, object : SQLiteDatabaseHook {
                override fun preKey(database: SQLiteDatabase?) = Unit

                override fun postKey(database: SQLiteDatabase?) {
                    if (memorySecure) {
                        database?.rawExecSQL(
                            "PRAGMA cipher_memory_security = ON"
                        )
                    } else {
                        database?.rawExecSQL("PRAGMA cipher_memory_security = OFF")
                    }
                }
            })
        }
    }
}