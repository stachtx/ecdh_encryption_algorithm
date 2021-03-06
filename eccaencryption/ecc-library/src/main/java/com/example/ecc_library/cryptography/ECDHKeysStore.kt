package com.example.ecc_library.cryptography

import android.annotation.SuppressLint
import android.util.Base64
import com.example.ecc_library.Aliases
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


object ECDHKeysStore {

    private const val ALGORITHM = "ECDH"
    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"

    private val ecdhKeysStore: MutableMap<String, ByteArray> = mutableMapOf()
    private val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)

    private var ivBytes: ByteArray? = null;

    private fun createIV() {
        val r = SecureRandom()
        val tmp = ByteArray(16)
        r.nextBytes(tmp)
        ivBytes = tmp
    }

    fun createECDHKey(alias: String) {
        val keyPair = generateECKeys()
        val encryptedKeyPair = encryptECDHKey(keyPair)
        ecdhKeysStore[alias] = encryptedKeyPair
    }

    fun getECDHKey(alias: String): KeyPair? {
        val storedKey = ecdhKeysStore.get(alias)
        return decryptECDHKey(storedKey)
    }

    private fun generateECKeys(): KeyPair? {
        return try {
            val kpg = KeyPairGenerator.getInstance(ALGORITHM)
            kpg.initialize(256)
            return kpg.generateKeyPair()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun encryptECDHKey(keyPair: KeyPair?): ByteArray {
        val b = ByteArrayOutputStream()
        val o = ObjectOutputStream(b)
        o.writeObject(keyPair)
        return cipher(Base64.encode(b.toByteArray(), Base64.DEFAULT), Cipher.ENCRYPT_MODE)
    }

    private fun decryptECDHKey(data: ByteArray?): KeyPair? {
        val bi =
            ByteArrayInputStream(Base64.decode(cipher(data, Cipher.DECRYPT_MODE), Base64.DEFAULT))
        val oi = ObjectInputStream(bi)
        return oi.readObject() as KeyPair
    }

    @SuppressLint("GetInstance")
    private fun cipher(data: ByteArray?, mode: Int): ByteArray {
        if (ivBytes == null) {
            createIV()
        }
        val secretKey = getKey()
        cipher.init(mode, secretKey, IvParameterSpec(ivBytes))
        return cipher.doFinal(data)
    }

    private fun getKey(): SecretKey {
        val keystore = KeyStore.getInstance(Aliases.androidKeyStoreName)
        keystore.load(null)
        val secretKeyEntry =
            keystore.getEntry(Aliases.androidKeyStoreAliasForECDH, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey;
    }
}