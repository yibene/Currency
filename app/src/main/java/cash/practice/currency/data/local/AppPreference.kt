package cash.practice.currency.data.local

import android.content.SharedPreferences
import cash.practice.currency.model.Rate
import com.google.gson.Gson

class AppPreference(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "AppPreference"
        const val PREFERENCES = "ApplicationPreference"
        private const val CURRENCY_CACHE_TIME = "appSettings_currencyCacheTime"
        private const val LAST_UPDATE_TIME = "appSettings_lastUpdateTime"
        private const val FAVORITE_CURRENCY = "appSettings_favoriteCurrency"
    }

    var currencyCacheTime: Long
        get() = sharedPreferences.getLong(CURRENCY_CACHE_TIME, 0L)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putLong(CURRENCY_CACHE_TIME, value)
            editor.apply()
        }

    var lastUpdateTime: Long
        get() = sharedPreferences.getLong(LAST_UPDATE_TIME, 0L)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putLong(LAST_UPDATE_TIME, value)
            editor.apply()
        }

    var favoriteCurrency: List<Rate>
        get() {
            val listString = sharedPreferences.getString(FAVORITE_CURRENCY, null)
            return listString?.let { gson.fromJson(it, Array<Rate>::class.java).toList() } ?: listOf()
        }
        set(list) {
            val listString = gson.toJson(list)
            val editor = sharedPreferences.edit()
            editor.putString(FAVORITE_CURRENCY, listString)
            editor.apply()
        }
}