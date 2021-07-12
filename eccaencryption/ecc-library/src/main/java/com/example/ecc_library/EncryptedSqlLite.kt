package com.example.ecc_library

interface EncryptedSqlLite {

    fun executeSQl(sql:String): Boolean
}