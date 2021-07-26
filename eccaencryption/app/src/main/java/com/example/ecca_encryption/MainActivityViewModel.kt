/*
 * Copyright (C) 2020 Sonique Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ecca_encryption

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecca_encryption.database.SecuredDatabase
import com.example.ecca_encryption.entities.User
import kotlinx.coroutines.*
import java.util.*
import kotlin.experimental.and

class MainActivityViewModel(private val context: Context) : ViewModel() {

    companion object {
        const val DEFAULT_LENGTH = 1000
        const val RETURN_INTERRUPTED = -1L
    }

    private var ioScope = CoroutineScope(Dispatchers.IO)

    internal var encrypted = false
    internal var encryptedWithMemorySecurity = false
    internal var algorithm: Algorithm = Algorithm.NONE

    internal var runAll = false

    private var length = DEFAULT_LENGTH

    private var stringsFirstname = mutableListOf<String>()
    private var stringsCV = mutableListOf<String>()

    private val db: SecuredDatabase
        get() {
            return SecuredDatabase.getInstance(
                context,
                algorithm,
                encrypted,
                encryptedWithMemorySecurity
            )
        }


    private var _querySize = MutableLiveData<Int>().apply { postValue(DEFAULT_LENGTH) }
    private var _results = MutableLiveData<String>()
    private var _enableUI = MutableLiveData<Boolean>()

    val querySize: LiveData<Int> = _querySize
    val results: LiveData<String> = _results
    val enableUI: LiveData<Boolean> = _enableUI

    private fun setUIEnabled(enabled: Boolean) {
        _enableUI.postValue(enabled)
    }

    internal fun updateQuerySize(querySize: Int) {
        length = querySize
        _querySize.postValue(length)
    }

    private fun appendResults(string: String) {
        _results.postValue(_results.value + string)
    }

    private fun clearResults() {
        // force the update immediately, so if append is called too fast
        // the content is gone anyway
        _results.value = ""
        _results.postValue("")
    }

    private fun runInsertsTests() {
        ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {
            if (runAll) {

                algorithm = Algorithm.NONE
                encrypted = false
                encryptedWithMemorySecurity = false
                val noEncryption = runInsertRounds()
                if (noEncryption == RETURN_INTERRUPTED) {
                    return@launch
                }

                algorithm = Algorithm.ECDH
                encrypted = true
                val encryptedEcdh = runInsertRounds()
                if (encryptedEcdh == RETURN_INTERRUPTED) {
                    return@launch
                }


                algorithm = Algorithm.AES
                encrypted = true
                val encryptedAes = runInsertRounds()
                if (encryptedAes == RETURN_INTERRUPTED) {
                    return@launch
                }

                algorithm = Algorithm.ECDH
                encryptedWithMemorySecurity = true
                val encryptedWithMemorySecurityECDH = runInsertRounds()
                if (encryptedWithMemorySecurityECDH == RETURN_INTERRUPTED) {
                    return@launch
                }

                algorithm = Algorithm.AES
                encryptedWithMemorySecurity = true
                val encryptedWithMemorySecurityAES = runInsertRounds()
                if (encryptedWithMemorySecurityAES == RETURN_INTERRUPTED) {
                    return@launch
                }

                val encDiffECDH =
                    percentageDifferenceWithBase(noEncryption.toDouble(), encryptedEcdh.toDouble())
                val encDiffAES =
                    percentageDifferenceWithBase(noEncryption.toDouble(), encryptedAes.toDouble())
                val encWithMemDiffECDH = percentageDifferenceWithBase(
                    noEncryption.toDouble(),
                    encryptedWithMemorySecurityECDH.toDouble()
                )

                val encWithMemDiffAES = percentageDifferenceWithBase(
                    noEncryption.toDouble(),
                    encryptedWithMemorySecurityAES.toDouble()
                )

                appendResults(
                    "\n\n" +
                            "Inserts\n" +
                            "No Encryption (base):      ${noEncryption}ms \n" +
                            "Encrypted ECDH:            ${encryptedEcdh}ms ${encDiffECDH}%\n" +
                            "Encrypted AES:             ${encryptedAes}ms ${encDiffAES}%\n" +
                            "Encrypted ECDH + Memory Security: ${encryptedWithMemorySecurityECDH}ms ${encWithMemDiffECDH}%\n"+
                            "Encrypted AES + Memory Security: ${encryptedWithMemorySecurityAES}ms ${encWithMemDiffAES}%\n"
                )
            } else {
                runInsertRounds()
            }


            setUIEnabled(true)

        }
    }

    private fun percentageDifferenceWithBase(base: Double, actual: Double): String {
        // calculate the difference between base number (no-encryption)
        // with actual (encryption)
        return String.format("%.2f", actual / base * 100.0 - 100.0)
    }

    private fun runSelectsIndexedTests() {
        ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {

            if (runAll) {
                encrypted = false
                encryptedWithMemorySecurity = false
                cleanStartForSelect()
                val noEncryption = runSelectIndexed()
                if (noEncryption == RETURN_INTERRUPTED) {
                    return@launch
                }

                encrypted = true
                cleanStartForSelect()
                val encrypted = runSelectIndexed()
                if (encrypted == RETURN_INTERRUPTED) {
                    return@launch
                }

                encryptedWithMemorySecurity = true
                cleanStartForSelect()
                val encryptedWithMemorySecurity = runSelectIndexed()
                if (encryptedWithMemorySecurity == RETURN_INTERRUPTED) {
                    return@launch
                }

                val encDiff =
                    percentageDifferenceWithBase(noEncryption.toDouble(), encrypted.toDouble())
                val encWithMemDiff = percentageDifferenceWithBase(
                    noEncryption.toDouble(),
                    encryptedWithMemorySecurity.toDouble()
                )

                appendResults(
                    "\n\n" +
                            "Selects indexed \n" +
                            "No Encryption (base):      ${noEncryption}ms \n" +
                            "Encrypted:                 ${encrypted}ms ${encDiff}%\n" +
                            "Encrypted+Memory Security: ${encryptedWithMemorySecurity}ms ${encWithMemDiff}%\n"
                )

            } else {
                cleanStartForSelect()
                runSelectIndexed()
            }

            setUIEnabled(true)
        }
    }

    private fun runSelectsNotIndexedTests() {
        ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {

            if (runAll) {
                encrypted = false
                encryptedWithMemorySecurity = false
                cleanStartForSelect()
                val noEncryption = runSelectNotIndexed()
                if (noEncryption == RETURN_INTERRUPTED) {
                    return@launch
                }

                encrypted = true
                cleanStartForSelect()
                val encrypted = runSelectNotIndexed()
                if (encrypted == RETURN_INTERRUPTED) {
                    return@launch
                }

                encryptedWithMemorySecurity = true
                cleanStartForSelect()
                val encryptedWithMemorySecurity = runSelectNotIndexed()
                if (encryptedWithMemorySecurity == RETURN_INTERRUPTED) {
                    return@launch
                }

                val encDiff =
                    percentageDifferenceWithBase(noEncryption.toDouble(), encrypted.toDouble())
                val encWithMemDiff = percentageDifferenceWithBase(
                    noEncryption.toDouble(),
                    encryptedWithMemorySecurity.toDouble()
                )

                appendResults(
                    "\n\n" +
                            "Selects NOT indexed \n" +
                            "No Encryption (base):      ${noEncryption}ms \n" +
                            "Encrypted:                 ${encrypted}ms ${encDiff}%\n" +
                            "Encrypted+Memory Security: ${encryptedWithMemorySecurity}ms ${encWithMemDiff}%\n"
                )

            } else {
                cleanStartForSelect()
                runSelectNotIndexed()
            }

            setUIEnabled(true)
        }
    }

    internal fun onCancelClicked() {
        ioScope.cancel()
        appendResults(
            "\n\n" +
                    "User canceled the action\n" +
                    "STOPPING...\n\n"
        )
        setUIEnabled(true)
    }

    internal fun onInsertsClicked() {
        setUIEnabled(false)
        clearResults()
        runInsertsTests()
    }

    internal fun onSelectIndexedClicked() {
        setUIEnabled(false)
        clearResults()
        runSelectsIndexedTests()
    }

    internal fun onSelectNoIndexClicked() {
        setUIEnabled(false)
        clearResults()
        runSelectsNotIndexedTests()
    }


    private fun cleanStartForSelect() {
        deleteAll()
        insertData(0, true)
    }

    private fun runInsertRounds(): Long {
        deleteAll()

        val rounds = 10
        var totalDuration = 0L
        for (i in 1..rounds) {
            if (ioScope.isActive) {
                totalDuration += insertData(i)
            } else {
                setUIEnabled(true)
                return RETURN_INTERRUPTED
            }
        }

        appendResults("Average: ${(totalDuration / rounds)}ms\n\n")

        return (totalDuration / rounds)
    }

    private fun runSelectIndexed(): Long {

        // Because this one can be very long, in order to preserve accuracy of the results
        // and allow cancellation, we will check if we need to cancel only every 500 selects
        // so we gonna run x batch of 500 selects

        val rounds = (length / 500) - 1

        val ids = mutableListOf<Long>()

        // first generate all ids randomly, so it is not counted into the time of querying
        for (i in 0..length) {
            ids.add(Random().nextInt(length).toLong())
        }

        val start = SystemClock.elapsedRealtime()

        for (i in 0..rounds) {
            if (ioScope.isActive) {
                for (j in 0..499) {
                    db.userDao().getById(ids[((i * 500) + j)])
                }
            } else {
                setUIEnabled(true)
                return RETURN_INTERRUPTED
            }
        }

        val stop = SystemClock.elapsedRealtime()

        val time = (stop - start)

        appendResults("Select $length time indexed: ${(time)}ms\n\n")

        return time
    }

    private fun runSelectNotIndexed(): Long {

        // Because this one can be very long, in order to preserve accuracy of the results
        // and allow cancellation, we will check if we need to cancel only every 500 selects
        // so we gonna run x batch of 500 selects

        val rounds = (length / 500) - 1

        stringsFirstname.shuffle()

        val start = SystemClock.elapsedRealtime()

        for (i in 0..rounds) {
            if (ioScope.isActive) {
                for (j in 0..499) {
                    db.userDao().findByFirstName(stringsFirstname[((i * 500) + j)])
                }
            } else {
                setUIEnabled(true)
                return RETURN_INTERRUPTED
            }
        }

        val stop = SystemClock.elapsedRealtime()

        val time = (stop - start)

        appendResults(
            "Select $length time NOT indexed: ${(time)}ms\n\n"
        )

        return time
    }

    private fun deleteAll() {
        if (ioScope.isActive) {
            db.userDao().deleteAll()

            appendResults(
                "Starting (algorithm: ${algorithm.name}, encrypted: $encrypted, with memory encrypt: $encryptedWithMemorySecurity)...\n"
            )
        } else {
            setUIEnabled(true)
        }
    }

    private fun insertData(round: Int, buildSelectDS: Boolean = false): Long {

        // Build the Data Set first, so it is not counted into the SQL time

        val users = mutableListOf<User>()

        if (buildSelectDS) {
            stringsFirstname.clear()
            stringsCV.clear()
        }
        for (i in 0..length) {
            // Generate random values
            val firstName = randomString(8 + (i % 5))
            val cv = randomString(280 + (i % 100))
            if (buildSelectDS) {
                // if in select mode,
                // will use some of the values for selects
                stringsFirstname.add(firstName)
                stringsCV.add(cv.substring(8 + (i % 30), 40 + (i % 100)))
            }

            users.add(
                User(
                    id = i.toLong(),
                    firstName = firstName,
                    lastName = randomString(8 + (i % 5)),
                    height = random.nextDouble(),
                    weight = random.nextDouble(),
                    cvInfo = cv
                )
            )
        }

        val start = SystemClock.elapsedRealtime()

        db.userDao().insertUserList(users)

        val stop = SystemClock.elapsedRealtime()

        val time = (stop - start)

        appendResults("Insert $length, round: $round : ${time}ms\n")


        return time
    }

    // Random String Generator
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val random = Random()
    private fun randomString(length: Int): String {
        val bytes = ByteArray(length)
        random.nextBytes(bytes)

        return bytes.indices
            .map { i ->
                charPool[(bytes[i] and 0xFF.toByte() and (charPool.size - 1).toByte()).toInt()]
            }.joinToString("")
    }

    override fun onCleared() {
        ioScope.cancel()
        super.onCleared()
    }
}