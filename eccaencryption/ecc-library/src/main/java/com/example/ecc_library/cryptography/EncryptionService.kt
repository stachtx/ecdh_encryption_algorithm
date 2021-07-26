package com.example.ecc_library.cryptography

import at.favre.lib.crypto.HKDF
import com.example.ecc_library.Aliases
import java.security.KeyStore
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey

class EncryptionService {

    init {
        AndroidKeyAESGenerator.generateKey(Aliases.androidKeyStoreAliasForECDH)
        AndroidKeyAESGenerator.generateKey(Aliases.androidKeyStoreAlias)
        ECDHKeysStore.createECDHKey(Aliases.ecdhKeysStoreAlias)
        ECDHKeysStore.createECDHKey(Aliases.ecdhKeysStoreDatabaseAlias)
    }

    fun prepareKeyECDH(): ByteArray? {
        val staticSalt32Byte = byteArrayOf(
            0xDA.toByte(),
            0xAC.toByte(), 0x3E, 0x10, 0x55,
            0xB5.toByte(),
            0xF1.toByte(), 0x3E, 0x53,
            0xE4.toByte(), 0x70,
            0xA8.toByte(), 0x77, 0x79,
            0x8E.toByte(), 0x0A,
            0x89.toByte(),
            0xAE.toByte(),
            0x96.toByte(), 0x5F, 0x19, 0x5D, 0x53, 0x62, 0x58,
            0x84.toByte(), 0x2C, 0x09, 0xAD.toByte(), 0x6E, 0x20, 0xD4.toByte()
        )
        val sharedKey = getKeyAgreementWithDatabase()
        val hkdf = HKDF.fromHmacSha256()
        val pseudoRandomKey = hkdf.extract(staticSalt32Byte, sharedKey)
        return hkdf.expand(pseudoRandomKey, "aes-key".toByteArray(), 32)
    }

    private fun getKeyAgreementWithDatabase(): ByteArray? {
        val userKeys = ECDHKeysStore.getECDHKey(Aliases.ecdhKeysStoreAlias)
        val databaseKeys = ECDHKeysStore.getECDHKey(Aliases.ecdhKeysStoreDatabaseAlias)
        val keyAgreement: KeyAgreement = KeyAgreement.getInstance("ECDH", "SC")
        keyAgreement.init(userKeys?.private)
        keyAgreement.doPhase(databaseKeys?.public, true)
        return keyAgreement.generateSecret()
    }


    fun prepareKeyAES(): ByteArray? {
        val aesKey = getAESKey()
        return aesKey.encoded

    }

    private fun getAESKey(): SecretKey {
        val keystore = KeyStore.getInstance(Aliases.androidKeyStoreName)
        keystore.load(null)
        val secretKeyEntry =
            keystore.getEntry(Aliases.androidKeyStoreAlias, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey;
    }


}