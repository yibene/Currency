package cash.practice.currency.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import cash.practice.currency.R
import cash.practice.currency.data.local.AppPreference
import cash.practice.currency.databinding.FragmentMainBinding
import cash.practice.currency.data.remote.Status
import cash.practice.currency.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment(), KodeinAware {
    override val kodein: Kodein by closestKodein()

    private val viewModel: MainViewModel by instance()
    private val preference: AppPreference by instance()
    private var binding by autoCleared<FragmentMainBinding>()

    companion object {
        private const val TAG = "MainFragment"
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        currency_list?.adapter = viewModel.getAdapter()

        val layoutManager = LinearLayoutManager(context)
        currency_list?.layoutManager = layoutManager
        currency_list?.itemAnimator = DefaultItemAnimator()
        currency_list?.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        getCurrencies()
    }

    private fun getCurrencies() {
        viewModel.getCurrencyList(getString(R.string.currency_access_key)).observe(this, Observer { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    viewModel.setCurrencyListCache(result.data)
                    getRates()
                }
                Status.LOADING -> {
                    viewModel.isLoading.set(true)
                }
                Status.ERROR -> {
                    Log.e(TAG, "get currency list error: ${result.message} (${result.code})")
                    viewModel.isLoading.set(false)
                }
            }
        })
    }

    private fun getRates() {
        viewModel.getRateList(getString(R.string.currency_access_key)).observe(this, Observer { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    Log.w(TAG, "load rate list success! length = ${result.data?.size}")
                    viewModel.setCurrencyListCache(result.data)
                    viewModel.isLoading.set(false)
                    viewModel.lastUpdateTime.set(SimpleDateFormat("MM/dd/yy HH:mm:ss", Locale.getDefault()).format(Date(preference.lastUpdateTime*1000)))
                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                    Log.e(TAG, "load rate list error: ${result.message} (${result.code})")
                    viewModel.isLoading.set(false)
                }
            }
        })
    }

}
