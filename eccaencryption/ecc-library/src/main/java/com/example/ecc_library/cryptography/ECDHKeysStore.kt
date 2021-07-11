package com.example.ecc_library.cryptography

import com.example.ecc_library.Aliases
import java.security.*
import javax.crypto.Cipher
import javax.crypto.SecretKey

class ECDHKeysStore {

    private var ecdhKeysStore: Map<String, String> = emptyMap()
    private val algorithm = "EC"
    private val transformation = "AES/GCM/NoPadding"

    fun createECDHKey(alias: String) {
        val keyPair = generateECKeys()
        val encryptedKeyPair = encryptECDHKey(keyPair)
        ecdhKeysStore.plus(Pair(alias,encryptedKeyPair))
    }

    fun getECDHKey(alias: String): KeyPair?{
        val storedKey = ecdhKeysStore.get(alias)
        return decryptECDHKey(storedKey)
    }

    private fun generateECKeys(): KeyPair? {
        return try {
            val kpg = KeyPairGenerator.getInstance(algorithm)
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

    private fun encryptECDHKey(key: KeyPair?): String {
        val cipher = Cipher.getInstance(transformation)
        return ""
    }


    private fun decryptECDHKey(key: String?): KeyPair? {

    }


    fun getKey(): SecretKey {
        val keystore = KeyStore.getInstance(Aliases.androidKeyStoreName)
        keystore.load(null)
        val secretKeyEntry = keystore.getEntry(Aliases.androidKeyStoreAlias, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey;
    }
}