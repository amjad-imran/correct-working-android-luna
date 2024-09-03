package com.oreo.ui.sleep2.help

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLearnMoreBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.sleep.SleepLearnMoreDataModel
import io.noties.markwon.Markwon


class LearnMoreFragment :
    BaseFragment<FragmentLearnMoreBinding>(FragmentLearnMoreBinding::inflate) {

    val args: LearnMoreFragmentArgs by navArgs()

    companion object {

        fun getStartData(data: SleepLearnMoreDataModel): Pair<Int, Bundle?> {
            return Pair(R.id.learnMoreFragmentSleep, Bundle().apply {
                putParcelable("data", data)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUi(args.data)
    }

    private fun setUi(data: SleepLearnMoreDataModel) {

        binding.toolbar.tvTitle.text = data.toolbarTitle

        data.internalImg?.let {
            binding.ivHeaderImage.setImageResource(it)
        }

        val markwon = Markwon.create(this.binding.tvContent.context)
        markwon.setMarkdown(binding.tvContent, data.content ?: "")
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }


}