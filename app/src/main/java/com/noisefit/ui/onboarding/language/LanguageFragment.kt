package com.noisefit.ui.onboarding.language

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.luna.databinding.FragmentLanguageBinding
import com.noisefit.ui.SplashActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
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
                    //binding.btnContinue.visible()
                    viewModel.selectedLanguage = language.languageCode
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.hideContinue = args.hideContinue

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)


        if (viewModel.hideContinue.not()) {
            binding.toolbar.backBtn.invisible()
            binding.btnContinue.visible()
        }

        setLanguageRecycler()
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(viewModel.hideContinue){
                    navigateUpSafe()
                }
            }
        }

    private fun setLanguageRecycler() {
        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = adapter
    }

    override fun initListener() {
        binding.btnContinue.setOnClickListener {
            viewModel.selectedLanguage.let {
                viewModel.updateSelectedLanguage(ApplicationUtils.getAppLanguageByCode(it))
            }
        }
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.languages.observe(this) {
            adapter.setDataSet(it, viewModel.selectedLanguage)

            val selectedPos = viewModel.selectedListPosition(it, viewModel.selectedLanguage)
            if (selectedPos != -1) {
                binding.rvLanguages.scrollToPosition(selectedPos)
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
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.languageUpdated.observe(this) {
            startActivity(SplashActivity.getStartIntent(requireContext()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}