package com.example.ecc_library.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.ecc_library.Aliases
import javax.crypto.KeyGenerator

object AndroidKeyAESGenerator {

    @Throws(Exception::class)
    fun generateKey(alias: String) {

        val keyGenAndroid: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Aliases.androidKeyStoreName)
        keyGenAndroid.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setKeySize(128).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build()
        )
        keyGenAndroid.generateKey()
    }
}