package cash.practice.currency.data.local

import android.content.SharedPreferences

class AppPreference(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val TAG = "AppPreference"
        const val PREFERENCES = "ApplicationPreference"
        private const val CURRENCY_CACHE_TIME = "appSettings_currencyCacheTime"
        private const val LAST_UPDATE_TIME = "appSettings_lastUpdateTime"
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
}