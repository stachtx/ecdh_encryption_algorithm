package com.example.ecc_library.cryptography

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import com.example.ecc_library.Aliases
import javax.crypto.KeyGenerator


class AndroidKeyGenerator {

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    fun generateKey() {
        val keyGenAndroid: KeyGenerator = KeyGenerator.getInstance("AES", Aliases.androidKeyStoreName)
        keyGenAndroid.init(
            KeyGenParameterSpec.Builder(
                Aliases.androidKeyStoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        keyGenAndroid.generateKey()
    }

}