package com.example.ecca_encryption.database

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import com.example.ecc_library.Aliases
import com.example.ecc_library.cryptography.ECDHKeysStore
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
            secure: Boolean = false,
            memorySecure: Boolean = false
        ): SecuredDatabase {
            return if (secure) {
                if (!memorySecure) {
                    securedDBSecure ?: synchronized(this) {
                        securedDBSecure ?: buildDatabase(
                            context,
                            secure,
                            memorySecure
                        ).also { securedDBSecure = it }
                    }
                } else {
                    securedDBSecureWithMemorySecurity ?: synchronized(this) {
                        securedDBSecureWithMemorySecurity ?: buildDatabase(
                            context,
                            secure,
                            memorySecure
                        ).also { securedDBSecureWithMemorySecurity = it }
                    }
                }
            } else {
                securedDB ?: synchronized(this) {
                    securedDB ?: buildDatabase(context, secure).also {
                        securedDB = it
                    }
                }
            }
        }

        private fun buildDatabase(
            context: Context,
            secure: Boolean,
            memorySecure: Boolean = false
        ): SecuredDatabase {
            val dbname = if (secure && memorySecure) {
                "encrypted-with-mem"
            } else if (secure && !memorySecure) {
                "encrypted"
            } else {
                "not-encrypted"
            }
            val builder = Room.databaseBuilder(
                context.applicationContext,
                SecuredDatabase::class.java, "${dbname}.db"
            )
            if (secure) {
                val passphrase: ByteArray? = ECDHKeysStore.getECDHKey(Aliases.ecdhKeysStoreAlias)?.public?.encoded
                val factory = SupportFactory(passphrase, object : SQLiteDatabaseHook {
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
                builder.openHelperFactory(factory)
            }
            return builder.build()
        }
    }
}