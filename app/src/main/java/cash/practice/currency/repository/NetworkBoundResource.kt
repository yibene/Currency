/*
 * Copyright (C) 2017 The Android Open Source Project
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

package cash.practice.currency.repository

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cash.practice.currency.data.remote.ApiResponse
import cash.practice.currency.data.remote.Resource
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.NullPointerException
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.coroutineContext

/**
 * A generic class that can provide a resource backed by both the SQLite database and the network.
 *
 *
 * You can read more about it in the [Architecture Guide](https://developer.android.com/arch).
 * @param [ResultType]
 * @param [RequestType]
 *
 */
abstract class NetworkBoundResource<ResultType, RequestType>
@MainThread constructor(private val accessKey: String) {
    private val result = MediatorLiveData<Resource<ResultType>>()
    private val supervisorJob = SupervisorJob()

    suspend fun build(): NetworkBoundResource<ResultType, RequestType> {
        withContext(Dispatchers.Main) {
            result.value = Resource.loading(null)
        }
        CoroutineScope(coroutineContext).launch(supervisorJob) {
            val localSource = loadFromLocal()
            if (shouldFetch(localSource)) {
                try {
                    fetchFromNetwork(localSource)
                } catch (e: Exception) {
                    val errorCode = when (e) {
                        is IllegalStateException -> HttpsURLConnection.HTTP_BAD_REQUEST
                        is NullPointerException -> HttpsURLConnection.HTTP_FORBIDDEN
                        else -> HttpsURLConnection.HTTP_BAD_GATEWAY
                    }
                    setValue(Resource.error(e.message ?: "", null, errorCode))
                }
            } else {
                setValue(Resource.success(localSource))
            }
        }
        return this
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.postValue(newValue)
        }
    }

    private suspend fun fetchFromNetwork(localSource: ResultType) {
        setValue(Resource.loading(localSource))
        val apiResponse = createCall(accessKey)
        withContext(Dispatchers.IO) {
            if (apiResponse.isHttpSuccess) {
                apiResponse.body?.let { resultBody ->
                    setValue(processResponse(resultBody))
                } ?: setValue(Resource.error(apiResponse.errorMessage ?: "", loadFromLocal(), apiResponse.code))
            } else {
                setValue(Resource.error(apiResponse.errorMessage ?: "", loadFromLocal(), apiResponse.code))
            }
        }
    }

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected abstract suspend fun processResponse(response: RequestType): Resource<ResultType>

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @WorkerThread
    protected abstract suspend fun loadFromLocal(): ResultType

    @MainThread
    protected abstract suspend fun createCall(accessKey: String): ApiResponse<RequestType>

}
