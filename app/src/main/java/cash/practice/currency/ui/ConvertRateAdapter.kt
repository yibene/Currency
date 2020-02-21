package cash.practice.currency.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cash.practice.currency.databinding.RateItemBinding
import cash.practice.currency.R
import cash.practice.currency.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.header_item.view.*

class ConvertRateAdapter(private val viewModel: MainViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var clickListener: ItemClickListener? = null

    companion object {
        private const val TAG = "ConvertRateAdapter"
        const val HEADER = 0
        const val FAVORITE = 1
        const val CURRENCY = 2
    }

    interface ItemClickListener {
        fun onFavoriteClick(view: View, adapterPosition: Int)
        fun onCurrencyClick(view: View, adapterPosition: Int)
        fun onCurrencyLongClick(view: View, adapterPosition: Int): Boolean
    }

    inner class HeaderHolder(val view: View) : RecyclerView.ViewHolder(view)

    inner class FavoriteRateHolder(val binding: RateItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentPosition: Int = 0

        fun onBind(position: Int) {
            binding.viewModel = viewModel
            currentPosition = position
            itemView.setOnClickListener { v ->
                clickListener?.onFavoriteClick(v, adapterPosition)
            }
        }

        fun getCurrentPosition() = currentPosition
    }

    inner class ConvertRateHolder(val binding: RateItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentPosition: Int = 0

        fun onBind(position: Int) {
            binding.viewModel = viewModel
            currentPosition = position
            itemView.setOnClickListener { v ->
                clickListener?.onCurrencyClick(v, adapterPosition)
            }
            itemView.setOnLongClickListener { v ->
                clickListener?.onCurrencyLongClick(v, adapterPosition) == true
            }
        }

        fun getCurrentPosition() = currentPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER -> HeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.header_item, parent, false))
            FAVORITE -> FavoriteRateHolder(DataBindingUtil.inflate(inflater, R.layout.rate_item, parent, false))
            CURRENCY -> ConvertRateHolder(DataBindingUtil.inflate(inflater, R.layout.rate_item, parent, false))
            else -> ConvertRateHolder(DataBindingUtil.inflate(inflater, R.layout.rate_item, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val favoriteCount = viewModel.favoriteList.get()?.size ?: 0
        return if (position == 0 || position == favoriteCount + 1) HEADER
        else if (position in 1..favoriteCount) FAVORITE
        else CURRENCY
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val favoriteCount = viewModel.favoriteList.get()?.size ?: 0
        when (holder) {
            is HeaderHolder -> {
                val context = holder.itemView.context
                if (position == 0) {
                    holder.itemView.header_title.text = context.getString(R.string.favorite_header)
                } else if (position == favoriteCount + 1) {
                    holder.itemView.header_title.text = context.getString(R.string.currency_header)
                }
            }
            is FavoriteRateHolder -> {
                holder.onBind(position)
                if (holder.getCurrentPosition() == position) {
                    holder.binding.item = viewModel.favoriteList.get()?.get(position - 1)
                }
            }
            is ConvertRateHolder -> {
                holder.onBind(position)
                if (holder.getCurrentPosition() == position) {
                    holder.binding.item = viewModel.currencyList.get()?.get(position - favoriteCount - 2)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val favoriteCount = viewModel.favoriteList.get()?.size ?: 0
        val currencyCount = viewModel.currencyList.get()?.size ?: 0
        return favoriteCount + currencyCount + 2
    }

    fun setItemClickListener(clickListener: ItemClickListener) {
        this.clickListener = clickListener
    }

}
