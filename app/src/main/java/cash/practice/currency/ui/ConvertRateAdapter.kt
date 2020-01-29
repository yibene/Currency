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

class ConvertRateAdapter : RecyclerView.Adapter<ConvertRateAdapter.ConvertRateHolder>() {

    private var viewModel: MainViewModel? = null
    private var clickListener: ItemClickListener? = null

    companion object {
        private const val TAG = "ConvertRateAdapter"
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ConvertRateHolder(val binding: RateItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentPosition: Int = 0

        fun onBind(position: Int) {
            binding.viewModel = viewModel
            currentPosition = position
            itemView.setOnClickListener { v ->
                clickListener?.onItemClick(v, adapterPosition)
            }
        }

        fun getCurrentPosition() = currentPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvertRateHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ConvertRateHolder(DataBindingUtil.inflate(inflater, R.layout.rate_item, parent, false))
    }

    private fun getDataList(): List<Rate>? {
        return viewModel?.currencyListCache?.get()
    }

    override fun onBindViewHolder(holder: ConvertRateHolder, position: Int) {
        holder.onBind(position)
        if (holder.getCurrentPosition() == position) {
            val item = getDataList()?.get(position)
            holder.binding.item = item
        }
    }

    override fun getItemCount(): Int {
        return getDataList()?.size ?: 0
    }

    fun setViewModel(viewModel: MainViewModel?) {
        if (this.viewModel != null) {
            Log.e(TAG, "set view model twice!")
        }
        this.viewModel = viewModel
        notifyDataSetChanged()
    }

    fun setItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

}
