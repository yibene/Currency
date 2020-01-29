package cash.practice.currency.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableDouble
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import cash.practice.currency.model.Rate
import cash.practice.currency.data.remote.Resource
import cash.practice.currency.repository.DataRepository

class MainViewModel(private val currencyRepository: DataRepository) : ObservableViewModel() {

    companion object {
        val DEFAULT_CURRENCY = Rate("USD", "United States Dollar", 1.0)
    }

    private val selectedBaseCurrency = ObservableField<Rate>(DEFAULT_CURRENCY)
    val currencyListCache = ObservableField<List<Rate>>()
    val currentBaseValue = ObservableDouble(1.0)
    val isLoading = ObservableBoolean(false)
    val lastUpdateTime = ObservableField<String>("")

    fun getCurrencyList(accessKey: String): LiveData<Resource<List<Rate>>> {
        return liveData {
            emitSource(currencyRepository.getCurrencyList(accessKey))
        }
    }

    fun getRateList(accessKey: String): LiveData<Resource<List<Rate>>> {
        return liveData {
            emitSource(currencyRepository.getLiveCurrencyRate(accessKey))
        }
    }

    fun selectBaseCurrency(currency: Rate?) {
        selectedBaseCurrency.set(currency)
    }

}
