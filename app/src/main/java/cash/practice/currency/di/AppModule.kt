package cash.practice.currency.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.*
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import cash.practice.currency.data.local.AppDatabase
import cash.practice.currency.data.local.AppPreference
import cash.practice.currency.data.local.AppPreference.Companion.PREFERENCES
import cash.practice.currency.data.local.CurrencyDao
import cash.practice.currency.data.remote.CurrencyServer
import cash.practice.currency.repository.CurrencyRepository
import cash.practice.currency.viewmodel.MainViewModel
import java.util.concurrent.TimeUnit

private const val TAG = "AppModule"
private const val DATABASE_NAME = "currency.db"
const val TIME_OUT_SECONDS = 30
const val WEB_SERVICE_URL = "https://s3-ap-northeast-1.amazonaws.com/m-et/Android/"
const val CURRENCY_URL = "http://api.currencylayer.com/"

val appModule = Kodein.Module(TAG) {

    bind<Gson>() with singleton {
        val builder = GsonBuilder()
        builder.create()
    }

    bind<AppPreference>() with singleton {
        AppPreference(instance<Context>().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE), instance())
    }

    bind<MainViewModel>() with eagerSingleton {
        MainViewModel(instance(), instance())
    }

    bind<Converter.Factory>() with provider {
        GsonConverterFactory.create(instance())
    }

    bind<Retrofit.Builder>() with provider { Retrofit.Builder() }

    bind<OkHttpClient.Builder>() with provider { OkHttpClient.Builder() }

    bind<OkHttpClient>() with singleton {
        instance<OkHttpClient.Builder>()  // to bind<OkHttpClient.Builder>()
            .connectTimeout(
                TIME_OUT_SECONDS.toLong(),
                TimeUnit.SECONDS
            )
            .readTimeout(
                TIME_OUT_SECONDS.toLong(),
                TimeUnit.SECONDS
            )
            .build()
    }

    bind<Retrofit>() with singleton {
        instance<Retrofit.Builder>()
            .baseUrl(WEB_SERVICE_URL)
            .client(instance())
            .addConverterFactory(instance())
            .build()
    }

    bind<CurrencyServer>() with singleton {
        instance<Retrofit.Builder>()
            .baseUrl(CURRENCY_URL)
            .client(instance())
            .addConverterFactory(instance())
            .build()
            .create(CurrencyServer::class.java)
    }

    bind<CurrencyDao>() with singleton {
        Room.databaseBuilder(
            instance(),
            AppDatabase::class.java, DATABASE_NAME
        ).fallbackToDestructiveMigration().build().currencyDao()
    }

    bind<CurrencyRepository>() with singleton {
        CurrencyRepository(instance(), instance(), instance(), instance())
    }

}