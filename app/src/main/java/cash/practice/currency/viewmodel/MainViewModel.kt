package cash.practice.currency.viewmodel

import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import cash.practice.currency.model.Rate
import cash.practice.currency.data.remote.Resource
import cash.practice.currency.repository.DataRepository
import cash.practice.currency.ui.ConvertRateAdapter

class MainViewModel(private val currencyRepository: DataRepository) : ObservableViewModel() {

    companion object {
        val DEFAULT_CURRENCY = Rate("USD", "United States Dollar", 1.0)
    }

    private var adapter: ConvertRateAdapter? = null

    val _currencyListCache = ObservableField<List<Rate>>()
//    val currencyListCache: LiveData<List<Rate>>
//        get() = _currencyListCache

    val selectedBaseCurrency = ObservableField<Rate>(DEFAULT_CURRENCY)
    val baseValue = ObservableField<String>("1")
    val isLoading = ObservableBoolean(false)
    val lastUpdateTime = ObservableField<String>("")

    fun getAdapter(): ConvertRateAdapter {
        return adapter ?: ConvertRateAdapter(this).also { adapter = it }
    }

    @Bindable
    fun getOnKeyListener() = View.OnKeyListener { view, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            view?.clearFocus()
            true
        } else {
            false
        }
    }

    @Bindable
    fun getOnItemClickListener() = object: ConvertRateAdapter.ItemClickListener {
        override fun onItemClick(view: View, position: Int) {
            selectBaseCurrency(position)
        }

        override fun onItemLongClick(view: View, position: Int) {
            val favorite = _currencyListCache.get()?.get(position)?.isFavorite ?: false
            _currencyListCache.get()?.get(position)?.isFavorite = !favorite
            sortCurrencyListCache()
            Log.w("Cash", "notifyItemChanged $position")
//            _currencyListCache.get()?.forEach {
//                Log.w("Cash", "$it")
//            }
            adapter?.notifyItemChanged(position)
        }
    }

    fun setCurrencyListCache(list: List<Rate>?) {
        _currencyListCache.set(list)
    }

//    fun getCurrencyListCache() = liveData {
//        emitSource(_currencyListCache)
//    }.value

    fun sortCurrencyListCache() {
        val list = _currencyListCache.get()
        list?.sortedWith(Comparator { rate1, rate2 ->
            if (rate1.isFavorite && !rate2.isFavorite) {
                Log.i("Cash", "rate1 is favorite!")
                return@Comparator 1
            }
            else if (!rate1.isFavorite && rate2.isFavorite) return@Comparator -1
            else {
                return@Comparator rate1.currency.compareTo(rate2.currency)
            }
        })
        list?.forEach {
            Log.w("Cash", "$it")
        }
        _currencyListCache.set(list)
    }

    fun selectBaseCurrency(position: Int) {
        selectedBaseCurrency.set(_currencyListCache.get()?.get(position))
    }

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

}
