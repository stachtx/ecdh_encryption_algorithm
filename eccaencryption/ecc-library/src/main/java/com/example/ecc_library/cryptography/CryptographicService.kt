package com.example.ecc_library.cryptography

interface CryptographicService {

    fun encrypt(data: ByteArray?): ByteArray
    fun decrypt(data: ByteArray?): ByteArray
}