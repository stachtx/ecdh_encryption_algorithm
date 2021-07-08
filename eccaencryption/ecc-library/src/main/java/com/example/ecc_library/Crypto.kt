package com.yourapp.android.crypto

import android.content.Context
import android.util.Base64
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypto(private val ctx: Context) {

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

    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun encrypt(data: ByteArray?): ByteArray {
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
    fun decrypt(data: ByteArray?): ByteArray {
        return cipher(data, Cipher.DECRYPT_MODE)
    }

    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun armorEncrypt(data: ByteArray?): String {
        return Base64.encodeToString(encrypt(data), Base64.DEFAULT)
    }

    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun armorDecrypt(data: String?): String {
        return String(decrypt(Base64.decode(data, Base64.DEFAULT)))
    }

    companion object {
        private const val engine = "AES"
        private const val crypto = "AES/CBC/PKCS5Padding"
    }

}