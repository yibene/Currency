package cash.practice.currency.data.remote

import cash.practice.currency.model.CurrencyList
import cash.practice.currency.model.LiveRateList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyServer {
    @GET("list")
    suspend fun getCurrencyList(
        @Query("access_key") accessKey: String
    ): Response<CurrencyList>

    @GET("live")
    suspend fun getLiveRateList(
        @Query("access_key") accessKey: String
    ): Response<LiveRateList>
}