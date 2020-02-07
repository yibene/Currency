package cash.practice.currency.ui

import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import cash.practice.currency.model.Rate
import java.text.DecimalFormat

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("keyListener")
    fun bindKeyListener(editText: EditText, onKeyListener: View.OnKeyListener) {
        editText.setOnKeyListener(onKeyListener)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:baseRate", "app:itemRate", "app:baseValue"])
    fun getFormattedValue(view: TextView, baseRate: Double, itemRate: Double, baseValue: String) {
        if (baseRate == 0.0) view.text = "0"
        else {
            val currentValue = if (baseValue.isEmpty()) 0.0 else baseValue.toDouble()
            val value = (itemRate / baseRate) * currentValue
            view.text = DecimalFormat("#.###").format(value)
        }
    }

    @JvmStatic
    @BindingAdapter("items")
    fun RecyclerView.bindItems(items: List<Rate>?) {
        val adapter = adapter as ConvertRateAdapter
        if (!items.isNullOrEmpty()) adapter.setDataList(items)
    }

    @JvmStatic
    @BindingAdapter("itemClickListener")
    fun RecyclerView.onItemClick(itemClickListener: ConvertRateAdapter.ItemClickListener) {
        val adapter = adapter as ConvertRateAdapter
        adapter.setItemClickListener(itemClickListener)
    }
}