package cash.practice.currency.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.DecimalFormat

object BindingAdapter {
    @JvmStatic
    @BindingAdapter(value = ["app:baseRate", "app:itemRate", "app:currentValue"])
    fun getFormattedValue(view: TextView, baseRate: Double, itemRate: Double, currentValue: Double) {
        if (baseRate == 0.0) view.text = "0"
        else {
            val value = (itemRate / baseRate) * currentValue
            view.text = DecimalFormat("#.###").format(value)
        }
    }
}