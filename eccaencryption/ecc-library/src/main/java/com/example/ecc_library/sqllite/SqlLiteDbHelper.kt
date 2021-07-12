package com.example.ecc_library.sqllite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.provider.BaseColumns
import android.support.annotation.RequiresApi
import com.example.ecc_library.Aliases
import com.example.ecc_library.cryptography.AndroidKeyGenerator
import com.example.ecc_library.cryptography.ECDHKeysStore

class SqlLiteDbHelper(
    context: Context,
    private val androidKeyGenerator: AndroidKeyGenerator,
    private val ecdhKeysStore: ECDHKeysStore
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(db: SQLiteDatabase) {
        androidKeyGenerator.generateKey()
        ecdhKeysStore.createECDHKey(Aliases.ecdhKeysStoreAlias)
        db.execSQL(SQL_CREATE_ENTRIES)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SqlLiteDbHelper.FeedReaderContract.FeedEntry.TABLE_NAME}"

        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
                    "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE} TEXT)"
    }

    object FeedReaderContract {
        object FeedEntry : BaseColumns {
            const val TABLE_NAME = "entry"
            const val COLUMN_NAME_TITLE = "title"
            const val COLUMN_NAME_SUBTITLE = "subtitle"
        }
    }
}