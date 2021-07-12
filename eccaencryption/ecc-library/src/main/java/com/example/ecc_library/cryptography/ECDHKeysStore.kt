package com.example.ecc_library.cryptography

import android.os.Build
import android.support.annotation.RequiresApi
import com.example.ecc_library.Aliases
import com.google.gson.Gson
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey

class ECDHKeysStore {

    private val ecdhKeysStore: Map<String, ByteArray> = emptyMap()

    fun createECDHKey(alias: String) {
        val keyPair = generateECKeys()
        val encryptedKeyPair = encryptECDHKey(keyPair)
        ecdhKeysStore.plus(Pair(alias, encryptedKeyPair))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getECDHKey(alias: String): KeyPair? {
        val storedKey = ecdhKeysStore.get(alias)
        return decryptECDHKey(storedKey)
    }

    private fun generateECKeys(): KeyPair? {
        return try {
            val kpg = KeyPairGenerator.getInstance(ALGORITHM)
            kpg.initialize(256)
            return kpg.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
            null
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
            null
        }
    }

    private fun encryptECDHKey(keyPair: KeyPair?): ByteArray {
        val data = Gson().toJson(keyPair)
        return cipher(data.toByteArray(), Cipher.ENCRYPT_MODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decryptECDHKey(data: ByteArray?): KeyPair? {
        val value = Base64.getEncoder().encodeToString(cipher(data, Cipher.DECRYPT_MODE))
        return Gson().fromJson(value,KeyPair::class.java)
    }
   
    private fun cipher(data: ByteArray?, mode: Int): ByteArray {
        val secretKey = getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
    
    private fun getKey(): SecretKey {
        val keystore = KeyStore.getInstance(Aliases.androidKeyStoreName)
        keystore.load(null)
        val secretKeyEntry =
            keystore.getEntry(Aliases.androidKeyStoreAlias, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey;
    }

    companion object  {
        const val ALGORITHM = "EC"
        const val TRANSFORMATION = "AES"
    }
}