package com.noisefit.ui.onboarding.language

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.luna.databinding.FragmentLanguageBinding
import com.noisefit.ui.SplashActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModel: LanguageViewModel by viewModels()
    private val args: LanguageFragmentArgs by navArgs()

    private val adapter: LanguagesAdapter by lazy {
        LanguagesAdapter(object : LanguageSelectionListener {
            override fun onLanguageSelected(language: AppLanguage) {
                if (viewModel.hideContinue) {
                    viewModel.updateSelectedLanguage(language)
                } else {
                    binding.btnContinue.visible()
                    viewModel.selectedAppLanguage = language
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.hideContinue = args.hideContinue
        setLanguageRecycler()
    }

    private fun setLanguageRecycler() {
        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter
    }

    override fun initListener() {
        binding.btnContinue.setOnClickListener {
            viewModel.selectedAppLanguage?.let {
                viewModel.updateSelectedLanguage(it)
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.languages.observe(this) {
            adapter.setDataSet(it, viewModel.selectedLanguage)
        }

        viewModel.languageUpdated.observe(this) {
            startActivity(SplashActivity.getStartIntent(requireContext()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}