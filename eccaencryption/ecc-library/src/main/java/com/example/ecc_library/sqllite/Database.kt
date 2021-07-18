package com.example.ecc_library.sqllite

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.ecc_library.Aliases
import com.example.ecc_library.cryptography.ECDHKeysStore
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

 abstract class SecuredDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var securedDB: SecuredDatabase? = null

        @Volatile
        private var securedDBSecure: SecuredDatabase? = null

        @Volatile
        private var securedDBSecureWithMemorySecurity: SecuredDatabase? = null

        @Volatile
        private var ecdhKeysStore:ECDHKeysStore? = null

        @RequiresApi(Build.VERSION_CODES.O)
        open fun getInstance(
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
                            ecdhKeysStore,
                            memorySecure
                        ).also { securedDBSecure = it }
                    }
                } else {
                    securedDBSecureWithMemorySecurity ?: synchronized(this) {
                        securedDBSecureWithMemorySecurity ?: buildDatabase(
                            context,
                            secure,
                            ecdhKeysStore,
                            memorySecure
                        ).also { securedDBSecureWithMemorySecurity = it }
                    }
                }
            } else {
                securedDB ?: synchronized(this) {
                    securedDB ?: buildDatabase(context, secure, ecdhKeysStore).also { securedDB = it }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun buildDatabase(
            context: Context,
            secure: Boolean,
            ecdhKeysStore: ECDHKeysStore?,
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
                val passphrase: ByteArray? =
                    ecdhKeysStore?.getECDHKey(Aliases.ecdhKeysStoreAlias)?.public?.encoded
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

    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        TODO("Not yet implemented")
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }
}