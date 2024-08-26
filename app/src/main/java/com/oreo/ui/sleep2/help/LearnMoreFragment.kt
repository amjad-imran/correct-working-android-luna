package com.oreo.ui.sleep2.help

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLearnMoreBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.sleep2.internal.learnmore.SleepLearnMoreDataModel
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

        val content = "**Introduction**  \n" +
                "Sleep duration is a critical component of overall health and well-being. It refers to the total amount of sleep an individual gets each night and plays a crucial role in various bodily functions, including cognitive performance, physical recovery, and emotional regulation. Understanding the concept of sleep duration and how it varies across different stages of life is essential for optimizing health.\n" +
                "\n" +
                "**The basics of sleep duration**  \n" +
                "Sleep duration is the amount of time spent asleep from the moment you fall asleep until you wake up. This period encompasses all stages of sleep, including light sleep, deep sleep, and REM (rapid eye movement) sleep, each of which is vital for different aspects of physical and mental recovery.\n" +
                "\n" +
                "**Why sleep duration matters**  \n" +
                "Adequate sleep duration is necessary for the body to repair tissues, regulate hormones, and consolidate memories. Chronic sleep deprivation, where an individual consistently gets less sleep than needed, can lead to various health issues such as weakened immune function, increased risk of chronic diseases, impaired cognitive function, and mood disturbances.\n" +
                "\n" +
                "**Sleep duration across different age groups**  \n" +
                "Sleep needs are not static and change throughout the human lifespan.\n" +
                "\n" +
                "• **Infants and young children:** In the early stages of life, sleep is essential for growth and development. Infants typically require 14 to 17 hours of sleep, which gradually decreases as they grow older. By the time children reach school age, they generally need 9 to 11 hours of sleep.  \n" +
                "• **Teenagers:** Adolescents need about 8 to 10 hours of sleep to support their rapid physical and cognitive development. This stage often sees a shift in sleep patterns, with teenagers naturally inclined to stay up later, which can conflict with early school start times.  \n" +
                "• **Adults:** Most adults function best with 7 to 9 hours of sleep. This range helps maintain optimal cognitive performance, emotional stability, and physical health.  \n" +
                "• **Older adults:** Although the amount of sleep needed doesn’t drastically change in older age, sleep patterns can shift. Older adults may find it more challenging to achieve continuous, deep sleep, but it remains important to aim for 7 to 8 hours to support overall health.  \n" +
                "\n" +
                "**Factors affecting sleep duration**  \n" +
                "Several factors can influence how much sleep you get, including lifestyle choices, sleep environment, and health conditions. Stress, caffeine, screen time, and irregular sleep schedules can all negatively impact sleep duration. Understanding these factors and how they affect your sleep can help you make adjustments to improve your sleep quality and duration."


        val markwon = Markwon.create(this.binding.tvContent.context)
        markwon.setMarkdown(binding.tvContent, data.content ?: "")
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}