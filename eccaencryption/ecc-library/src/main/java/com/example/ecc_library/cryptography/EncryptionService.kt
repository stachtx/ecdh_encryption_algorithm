package com.example.ecc_library.cryptography

import at.favre.lib.crypto.HKDF
import com.example.ecc_library.Aliases
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
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

        val sharedKey = getKeyAgreementWithDatabase()
        return useHKDF(sharedKey)
    }

    private fun useHKDF(sharedKey:ByteArray?) :ByteArray?{
        val salt32Byte = getNextSalt()
        val hkdf = HKDF.fromHmacSha256()
        val pseudoRandomKey = hkdf.extract(salt32Byte, sharedKey)
        return hkdf.expand(pseudoRandomKey, "aes-key".toByteArray(), 32)
    }

    private fun getNextSalt(): ByteArray? {
        val random: Random = SecureRandom()
        val salt = ByteArray(32)
        random.nextBytes(salt)
        return salt
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