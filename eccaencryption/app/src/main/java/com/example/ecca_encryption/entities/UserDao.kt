/*
 * Copyright (C) 2020 Sonique Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ecca_encryption.entities

import androidx.room.*

@Dao
interface UserDao{
    @Query("SELECT * FROM user")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM user WHERE id = :id")
    fun getById(id: Long): User

    @Query("SELECT * FROM user WHERE first_name LIKE :find")
    fun findByFirstName(find: String): List<User>

    @Query("SELECT * FROM user WHERE cv_info LIKE :find")
    fun findByCV(find: String): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserList(users: List<User>)

    @Update
    fun updateUsers(vararg users: User)

    @Delete
    fun deleteUsers(vararg users: User)

    @Query("DELETE FROM user")
    fun deleteAll()
}