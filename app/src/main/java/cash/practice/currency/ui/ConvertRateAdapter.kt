package cash.practice.currency.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cash.practice.currency.databinding.RateItemBinding
import cash.practice.currency.R
import cash.practice.currency.model.Rate
import cash.practice.currency.viewmodel.MainViewModel

class ConvertRateAdapter(private val viewModel: MainViewModel) : RecyclerView.Adapter<ConvertRateAdapter.ConvertRateHolder>() {

    private var clickListener: ItemClickListener? = null
    private var dataList: List<Rate>? = null

    companion object {
        private const val TAG = "ConvertRateAdapter"
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(view: View, position: Int)
    }

    inner class ConvertRateHolder(val binding: RateItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentPosition: Int = 0

        fun onBind(position: Int) {
            binding.viewModel = viewModel
            currentPosition = position
            itemView.setOnClickListener { v ->
                clickListener?.onItemClick(v, adapterPosition)
            }
            itemView.setOnLongClickListener { v ->
                clickListener?.onItemLongClick(v, adapterPosition)
                true
            }
        }

        fun getCurrentPosition() = currentPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvertRateHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ConvertRateHolder(DataBindingUtil.inflate(inflater, R.layout.rate_item, parent, false))
    }

    fun setDataList(list: List<Rate>?) {
        dataList = list
    }

    override fun onBindViewHolder(holder: ConvertRateHolder, position: Int) {
        holder.onBind(position)
        if (holder.getCurrentPosition() == position) {
            holder.binding.item = viewModel._currencyListCache.get()?.get(position)
        }
    }

    override fun getItemCount(): Int {
        return viewModel._currencyListCache.get()?.size ?: 0
    }

    fun setItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

}
