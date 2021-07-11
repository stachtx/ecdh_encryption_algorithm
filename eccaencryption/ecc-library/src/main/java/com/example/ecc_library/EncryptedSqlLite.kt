package com.yourapp.android.crypto

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Base64
import com.example.ecc_library.Aliases
import com.example.ecc_library.cryptography.AndroidKeyGenerator
import com.example.ecc_library.cryptography.CryptographicService
import com.example.ecc_library.cryptography.ECDHKeysStore
import com.example.ecc_library.sqllite.FeedReaderDbHelper
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class EncryptedSqlLite(
    context: Context,
    private val cryptoService: CryptographicService,
    private val androidKeyGenerator: AndroidKeyGenerator,
    private val ecdhKeysStore: ECDHKeysStore
) :
    SQLiteOpenHelper(
        context,
        FeedReaderDbHelper.DATABASE_NAME, null,
        FeedReaderDbHelper.DATABASE_VERSION
    ) {


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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(p0: SQLiteDatabase?) {
        androidKeyGenerator.generateKey()
        ecdhKeysStore.createECDHKey(Aliases.ecdhKeysStoreAlias)

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }


}