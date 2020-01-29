package cash.practice.currency.repository

import androidx.lifecycle.LiveData
import cash.practice.currency.model.Rate
import cash.practice.currency.data.remote.Resource

interface DataRepository {
    suspend fun getCurrencyList(accessKey: String): LiveData<Resource<List<Rate>>>
    suspend fun getLiveCurrencyRate(accessKey: String): LiveData<Resource<List<Rate>>>
}