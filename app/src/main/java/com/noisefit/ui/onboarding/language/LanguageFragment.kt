package com.noisefit.ui.onboarding.language

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentLanguageBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModel: LanguageViewModel by viewModels()

    private val adapter: LanguagesAdapter by lazy {
        LanguagesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLanguageRecycler()
    }

    private fun setLanguageRecycler() {
        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.languages.observe(this){
            adapter.setDataSet(it)
        }
    }
}