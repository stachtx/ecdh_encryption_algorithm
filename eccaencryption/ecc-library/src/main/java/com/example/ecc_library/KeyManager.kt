package com.yourapp.android.crypto

import android.content.Context
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

class KeyManager(private val ctx: Context) {

    var id: ByteArray?
        get() = reader(file1)
        set(data) {
            writer(data, file1)
        }

    var iv: ByteArray?
        get() = reader(file2)
        set(data) {
            writer(data, file2)
        }

    fun reader(file: String?): ByteArray? {
        var data: ByteArray? = null
        try {
            var bytesRead = 0
            val fis =
                ctx.openFileInput(file)
            val bos = ByteArrayOutputStream()
            val b = ByteArray(1024)
            while (fis.read(b).also { bytesRead = it } != -1) {
                bos.write(b, 0, bytesRead)
            }
            data = bos.toByteArray()
        } catch (e: FileNotFoundException) {
            Log.e(
                TAG,
                "File not found in getId()"
            )
        } catch (e: IOException) {
            Log.e(
                TAG,
                "IOException in setId(): " + e.message
            )
        }
        return data
    }

    fun writer(data: ByteArray?, file: String?) {
        try {
            val fos =
                ctx.openFileOutput(
                    file,
                    Context.MODE_PRIVATE
                )
            fos.write(data)
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.e(
                TAG,
                "File not found in setId()"
            )
        } catch (e: IOException) {
            Log.e(
                TAG,
                "IOException in setId(): " + e.message
            )
        }
    }

    companion object {
        private const val TAG = "KeyManager"
        private const val file1 = "id_value"
        private const val file2 = "iv_value"
    }

}