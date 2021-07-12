package com.example.ecc_library

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Base64
import com.example.ecc_library.cryptography.AndroidKeyGenerator
import com.example.ecc_library.cryptography.CryptographicService
import com.example.ecc_library.cryptography.ECDHKeysStore
import com.example.ecc_library.sqllite.SqlLiteDbHelper
import java.lang.Exception
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class EncryptedSqlLiteImpl(
    context: Context,
    private val cryptoService: CryptographicService,
    private val sqlLiteDbHelper: SqlLiteDbHelper
) : EncryptedSqlLite {

    override fun executeSQl(sql:String):Boolean {
        var result = false
        try{

            sqlLiteDbHelper.writableDatabase.execSQL(sql)
            result = true
        } catch (e:Exception){
            println(e.stackTrace)
        }
        return result
    }

    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun armorEncrypt(data: ByteArray?): String? {
        return Base64.encodeToString(cryptoService.encrypt(data), Base64.DEFAULT)
    }

    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun armorDecrypt(data: String?): String? {
        return String(cryptoService.decrypt(Base64.decode(data, Base64.DEFAULT)))
    }

}