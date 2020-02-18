package cash.practice.currency.viewmodel

import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import cash.practice.currency.data.local.AppPreference
import cash.practice.currency.model.Rate
import cash.practice.currency.data.remote.Resource
import cash.practice.currency.repository.CurrencyRepository
import cash.practice.currency.repository.DataRepository
import cash.practice.currency.ui.ConvertRateAdapter
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.Comparator

class MainViewModel(
    private val currencyRepository: DataRepository,
    private val preferences: AppPreference
) : ObservableViewModel() {

    companion object {
        val DEFAULT_CURRENCY = Rate("USD", "United States Dollar", 1.0)
    }

    private var adapter: ConvertRateAdapter? = null
    val currencyList = ObservableField<List<Rate>>()
    val favoriteList = ObservableField<List<Rate>>()

    val selectedBaseCurrency = ObservableField<Rate>(DEFAULT_CURRENCY)
    val baseValue = ObservableField<String>("1")
    val isLoading = ObservableBoolean(false)
    val lastUpdateTime = ObservableField<String>("")

    fun getAdapter(): ConvertRateAdapter {
        return adapter ?: ConvertRateAdapter(this).also { adapter = it }
    }

    fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                UP or DOWN or START or END,
                START or END
            ) {

                override fun getDragDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    if (viewHolder !is ConvertRateAdapter.FavoriteRateHolder) return 0
                    return super.getDragDirs(recyclerView, viewHolder)
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    if (viewHolder !is ConvertRateAdapter.FavoriteRateHolder) return 0
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    if (target !is ConvertRateAdapter.FavoriteRateHolder) return false
                    val adapter = recyclerView.adapter as ConvertRateAdapter
                    val from = viewHolder.adapterPosition - 1
                    val to = target.adapterPosition - 1
                    moveFavorite(from, to)
                    adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0f
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition - 1
                    val list = favoriteList.get()?.toMutableList()
                    val item = list?.get(position)
                    item?.let {
                        it.isFavorite = 0
                        GlobalScope.launch {
                            (currencyRepository as CurrencyRepository).setFavoriteRate(it)
                        }
                    }
                    list?.removeAt(position)
                    favoriteList.set(list)
                    adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                }
            }
        )
    }

    fun moveFavorite(from: Int, to: Int) {
        val list = favoriteList.get()?.toMutableList()
        list?.let {
            if (from < to) {
                for (i in from until to) {
                    Collections.swap(it, i, i+1)
                }
            } else {
                for (i in from downTo to+1) {
                    Collections.swap(it, i, i-1)
                }
            }
        }
        favoriteList.set(list)
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

        override fun onFavoriteClick(view: View, adapterPosition: Int) {
            selectedBaseCurrency.set(favoriteList.get()?.get(adapterPosition - 1))
        }

        override fun onCurrencyClick(view: View, adapterPosition: Int) {
            val favoriteCount = favoriteList.get()?.size ?: 0
            selectedBaseCurrency.set(currencyList.get()?.get(adapterPosition - favoriteCount - 2))
        }

        override fun onCurrencyLongClick(view: View, adapterPosition: Int): Boolean {
            val favoriteCount = favoriteList.get()?.size ?: 0
            val item = currencyList.get()?.get(adapterPosition - favoriteCount - 2)
            val favList = favoriteList.get()?.toMutableList()
            item?.let { rate ->
                favList?.let {
                    if (!it.contains(rate)) {
                        rate.isFavorite = 1
                        it.add(rate)
                        favoriteList.set(it)
                        GlobalScope.launch {
                            (currencyRepository as CurrencyRepository).setFavoriteRate(rate)
                        }
                    }
                }
            }
            val newCount = favoriteList.get()?.size ?: 0
            return if (newCount > favoriteCount) {
                adapter?.notifyItemInserted(favoriteList.get()?.size ?: 0)
                true
            } else false
        }
    }

    fun setCurrencyListCache(list: List<Rate>?) {
        currencyList.set(list)
    }

    fun saveFavoriteList() {
        favoriteList.get()?.let {
            preferences.favoriteCurrency = it
        }
    }

    fun sortCurrencyListCache() {
        val list = currencyList.get()
        val comparator = Comparator<Rate> { a, b -> compareValues(a.isFavorite, b.isFavorite) }.reversed().thenBy { it.currency }
        currencyList.set(list?.sortedWith(comparator))
        currencyList.get()?.forEach {
            Log.w("Cash", "$it")
        }
    }

    private val favoriteObserver = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            GlobalScope.launch {
                Log.w("Cash", "before set, favorite list = ${favoriteList.get()}")
                (currencyRepository as CurrencyRepository).setFavoriteRateList(favoriteList.get())
                printFavorite()
            }
        }
    }

    fun loadFavoriteList() {
        favoriteList.set(preferences.favoriteCurrency)
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

    fun getFavoriteRateList(): LiveData<List<Rate>> {
        return liveData {
            emitSource((currencyRepository as CurrencyRepository).getFavoriteRateList())
        }
    }

    fun printFavorite() {
        (currencyRepository as CurrencyRepository).printFavorite()
    }

    override fun onCleared() {
        super.onCleared()
    }

}
