package com.noisefit.ui.friends.location.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.data.remote.CityData
import com.noisefit.data.remote.StateData
import com.noisefit.luna.databinding.FragmentSearchStateBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

const val CLOSED_SEARCH_STATE_KEY = "CLOSED_SEARCH_STATE_KEY"

@AndroidEntryPoint
class SearchStateFragment :
    BaseFragment<FragmentSearchStateBinding>(FragmentSearchStateBinding::inflate) {

    private val viewModel: SearchStateViewModel by viewModels()

    private val mAdapter: SearchStateAdapter by lazy {
        SearchStateAdapter(object :
            SearchStateAdapter.OnSearchStateInteractionListener {
            override fun onRowClick(data: StateData) {
                viewModel.updateUserLocationState(data.name, data.id)
                closeFragment()

            }

        })
    }

    private val mCityAdapter: SearchCityAdapter by lazy {
        SearchCityAdapter(object :
            SearchCityAdapter.OnSearchCityInteractionListener {
            override fun onRowClick(data: CityData) {
                viewModel.updateUserLocationState(data.name, data.id)
                closeFragment()

            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
//            viewModel.type = SearchStateFragmentArgs.fromBundle(it).type
//            viewModel.id = SearchStateFragmentArgs.fromBundle(it).id
//            viewModel.name = SearchStateFragmentArgs.fromBundle(it).data
        }

        when (viewModel.type) {
            SearchStateType.State -> {
                var state = viewModel.name
                if (state.isNullOrEmpty()) {
                    state = getString(R.string.text_enter_your_state)
                }
                viewModel.fetchStateList()
                setTitle("State")
                setSearchText(state)
            }
            SearchStateType.City -> {
                var state = viewModel.name
                if (state.isNullOrEmpty()) {
                    state = getString(R.string.text_enter_your_city)
                }
                setTitle("City")
                viewModel.fetchCityList()
                setSearchText(state)
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        setRecycler()

    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeFragment()
            }
        }

    private fun closeFragment() {
        requireActivity().supportFragmentManager.setFragmentResult(
            CLOSED_SEARCH_STATE_KEY,
            bundleOf("closed" to true)
        )
        navigateUpSafe()
    }

    private fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    private fun setSearchText(text: String?) {
        text?.let {
            binding.etState.hint = text
        }

    }

    private fun setRecycler() {
        when (viewModel.type) {
            SearchStateType.State -> {
                with(binding.rv) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = mAdapter
                }
            }
            SearchStateType.City -> {
                with(binding.rv) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = mCityAdapter
                }
            }
        }


    }

    override fun initListener() {
        binding.etState.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (viewModel.type) {
                    SearchStateType.State -> {
                        mAdapter.filter.filter(s)
                    }
                    SearchStateType.City -> {
                        mCityAdapter.filter.filter(s)
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }


        viewModel.stateList.observe(this) {
            it?.let {
                mAdapter.setDataSet(it)
            }
        }



        viewModel.cityList.observe(this) {
            it?.let {
                mCityAdapter.setDataSet(it)
            }
        }
    }

}

enum class SearchStateType {
    City,
    State
}