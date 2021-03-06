package cash.practice.currency.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cash.practice.currency.model.CurrencyList
import cash.practice.currency.model.LiveRateList
import cash.practice.currency.model.Rate
import cash.practice.currency.data.local.*
import cash.practice.currency.data.remote.ApiResponse
import cash.practice.currency.data.remote.CurrencyServer
import cash.practice.currency.data.remote.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrencyRepository(
    private val context: Context,
    private val currencyServer: CurrencyServer,
    private val currencyDao: CurrencyDao,
    private val preference: AppPreference
) : DataRepository {

    companion object {
        const val TAG = "CurrencyRepository"
    }

    override suspend fun getCurrencyList(accessKey: String): LiveData<Resource<List<Rate>>> {
        return object : NetworkBoundResource<List<Rate>, CurrencyList>(accessKey) {

            override suspend fun processResponse(response: CurrencyList): Resource<List<Rate>> {
                return if (response.success) {
                    response.currencies.forEach { (name, currencyName) ->
                        currencyDao.insertRate(Rate(name, currencyName))
                    }
                    Resource.success(loadFromLocal())
                } else {
                    Resource.error(response.error.info, null, response.error.code)
                }
            }

            override suspend fun loadFromLocal() = currencyDao.getAll()

            /**
             * should fetch after 30 min from last fetch
             */
            override fun shouldFetch(data: List<Rate>?) = data.isNullOrEmpty()

            override suspend fun createCall(accessKey: String): ApiResponse<CurrencyList> {
                Log.w(TAG, "getCurrencyList called")
                return ApiResponse(currencyServer.getCurrencyList(accessKey))
            }

        }.build().asLiveData()
    }

    override suspend fun getLiveCurrencyRate(accessKey: String): LiveData<Resource<List<Rate>>> {
        return object : NetworkBoundResource<List<Rate>, LiveRateList>(accessKey) {

            override suspend fun processResponse(response: LiveRateList): Resource<List<Rate>> {
                if (response.success) {
                    preference.lastUpdateTime = response.timestamp
                    preference.currencyCacheTime = System.currentTimeMillis() / 1000
                    val map = response.quotes
                    if (map.isNullOrEmpty()) {
                        return Resource.error("Empty currency list", null, -1)
                    }
                    val list = preference.favoriteCurrency
                    map.forEach { (name, rate) ->
                        currencyDao.updateRateByCurrency(name.substring(3, name.length), rate)
                        list.find { it.currency == name }?.let {
                            it.rate = rate
                        }
                    }
                    preference.favoriteCurrency = list
                    return Resource.success(loadFromLocal())
                } else {
                    return Resource.error(response.error.info, null, response.error.code)
                }
            }

            override suspend fun loadFromLocal(): List<Rate> = currencyDao.getAll()

            /**
             * currency layer has 60 min cache at server side (for free and basic plan)
             * so we should fetch after 30 min from last fetch
             */
            override fun shouldFetch(data: List<Rate>?): Boolean {
                val timestamp = preference.currencyCacheTime
                val isExpired = System.currentTimeMillis()/1000 - timestamp > 30 * 60
                Log.d(TAG, "getLiveRateList shouldFetch: $isExpired, distance from last update: ${System.currentTimeMillis()/1000 - timestamp}sec")
                return isInternetAvailable() && isExpired
            }

            override suspend fun createCall(accessKey: String): ApiResponse<LiveRateList> {
                Log.w(TAG, "getLiveRateList called")
                return ApiResponse(currencyServer.getLiveRateList(accessKey))
            }

        }.build().asLiveData()
    }

    fun isInternetAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val networkCapabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return with(networkCapabilities) {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
    }

    fun printFavorite() {
        Log.i(TAG, "print favorite")
        GlobalScope.launch {
            currencyDao.getFavoriteList().forEach {
                Log.i(TAG, "$it")
            }
        }
    }

    suspend fun getFavoriteRateList(): LiveData<List<Rate>> {
        return MediatorLiveData<List<Rate>>().apply {
            postValue(currencyDao.getFavoriteList())
        }
    }

    suspend fun setFavoriteRateList(favoriteList: List<Rate>?) {
        withContext(Dispatchers.IO) {
            Log.i(TAG, "set favorite list size = ${favoriteList?.size}")
            favoriteList?.forEach {
                currencyDao.updateFavoriteByCurrency(it.currency, it.isFavorite)
            }
            Log.i(TAG, "set favorite list finish")
        }
    }

    suspend fun setFavoriteRate(rate: Rate?) {
        withContext(Dispatchers.IO) {
            rate?.let {
                currencyDao.updateFavoriteByCurrency(it.currency, it.isFavorite)
            }
        }
    }

}