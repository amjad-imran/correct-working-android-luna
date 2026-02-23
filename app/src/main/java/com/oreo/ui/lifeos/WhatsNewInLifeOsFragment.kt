package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.noisefit.data.RemoteConfigManager
import com.noisefit.data.RemoteConfigManager.LIFE_OS_WHATS_NEW_BLOG
import com.noisefit.luna.databinding.FragmentWhatsNewInLifeOsBinding
import com.noisefit.luna.databinding.ItemLifeOsWhatsnewBinding
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.imagepicker.Util.getMarkdownString
import com.oreo.data.model.WhatsNewBlogItem
import com.oreo.data.model.WhatsNewParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WhatsNewInLifeOsFragment : BaseFragment<FragmentWhatsNewInLifeOsBinding>(
    FragmentWhatsNewInLifeOsBinding::inflate
) {
    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { navigateUpSafe() }
        setupBlogSections()
    }

    override fun initListener() {}
    override fun subscribeObservers() {}

    private fun setupBlogSections() {
        try {
            val languageCode = localDataStore.getSelectedAppLanguage() ?: "en"
            val rawJson = RemoteConfigManager.getString(LIFE_OS_WHATS_NEW_BLOG)

            val blogPosition = arguments?.getInt("blogPosition") ?: 0
            val section = WhatsNewParser.getVoiceAISection(blogPosition, rawJson, languageCode)

            if (section == null || section.blogDetails.isEmpty()) {
                binding.LayoutCards.visibility = View.GONE
                return
            }

            binding.tvToolbarTitle.text = section.title
            binding.LayoutCards.visibility = View.VISIBLE
            inflateBlogItems(section.blogDetails)

        } catch (e: Exception) {
            LOGS.e(e)
        }
    }

    private fun inflateBlogItems(items: List<WhatsNewBlogItem>) {
        val inflater = LayoutInflater.from(requireContext())
        val container = binding.LayoutCards

        items.forEach { item ->
            val itemBinding = ItemLifeOsWhatsnewBinding.inflate(inflater, container, false)
            bindBlogItem(itemBinding, item)
            container.addView(itemBinding.root)
        }
    }

    private fun bindBlogItem(itemBinding: ItemLifeOsWhatsnewBinding, item: WhatsNewBlogItem) {
        if (!item.title.isNullOrBlank()) {
            itemBinding.tvSection1Title.visibility = View.VISIBLE
            itemBinding.tvSection1Title.text = getMarkdownString(item.title)
        } else {
            itemBinding.tvSection1Title.visibility = View.GONE
        }

        if (item.imageUrl != null) {
            itemBinding.ivSection1Illustration.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(item.imageUrl)
                .into(itemBinding.ivSection1Illustration)
        } else {
            itemBinding.ivSection1Illustration.visibility = View.GONE
        }

        if (!item.description.isNullOrBlank()) {
            itemBinding.tvSection1Body.visibility = View.VISIBLE
            itemBinding.tvSection1Body.text = item.description
        } else {
            itemBinding.tvSection1Body.visibility = View.GONE
        }
    }
}