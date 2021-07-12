package com.example.ecc_library.cryptography

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.example.ecc_library.Aliases
import com.yourapp.android.crypto.KeyManager
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class ECDHService(private val ctx: Context, private val ecdhKeysStore: ECDHKeysStore) : CryptographicService {

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    override fun encrypt(data: ByteArray?): ByteArray {
        val keyPair = ecdhKeysStore.getECDHKey(Aliases.ecdhKeysStoreAlias)


        return cipher(data, Cipher.ENCRYPT_MODE)
    }

    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    override fun decrypt(data: ByteArray?): ByteArray {
        return cipher(data, Cipher.DECRYPT_MODE)
    }


    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun cipher(data: ByteArray?, mode: Int): ByteArray {
        val km = KeyManager(ctx)
        val sks = SecretKeySpec(km.id, engine)
        val iv = IvParameterSpec(km.iv)
        val c = Cipher.getInstance(crypto)
        c.init(mode, sks, iv)
        return c.doFinal(data)
    }




    companion object {
        private const val engine = "AES"
        private const val crypto = "AES/CBC/PKCS5Padding"
    }

}