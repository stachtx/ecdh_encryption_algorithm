package com.example.ecca_encryption.database

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import com.example.ecc_library.sqllite.SecuredDatabase
import com.example.ecca_encryption.entities.User
import com.example.ecca_encryption.entities.UserDao

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : SecuredDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        fun getInstance(
            context: Context,
            secure: Boolean = false,
            memorySecure: Boolean = false
        ): UserDatabase {
            return SecuredDatabase.getInstance(context, secure, memorySecure) as UserDatabase
        }
    }
}

