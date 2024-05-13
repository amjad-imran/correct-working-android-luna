package com.oreo.ui.stress.help

import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.StressUnderstandingOverview
import com.oreo.data.model.StressUnderstandingSubList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StressUnderstandingViewModel
@Inject
constructor(val sessionManager: SessionManager) : BaseViewModel() {

    fun getData(): ArrayList<StressUnderstandingOverview> {
        val dataList = ArrayList<StressUnderstandingOverview>()

        dataList.add(
            StressUnderstandingOverview.ImageWithText(
                "What is stress?",
                R.drawable.image_what_is_stress,
                "Stress is the body’s natural response to physical or mental challenges. It's commonly known that stress can have negative impacts on the body and brain, but healthy levels can actually enhance focus, memory, and overall performance.<br><br>Luna Ring measures your heart rate, HRV & motion, aiding in tracking physiological stress for effective understanding, management, and recovery."
            )
        )

        dataList.add(
            StressUnderstandingOverview.ImageWithText(
                "What are the different Stress Zones?",
                R.drawable.image_stress_zone,
                "<font color='#3fe8b5'>Calm</font> (0-32)<br>Your body is currently resting and recharging itself. You may feel calm, at ease, or relaxed.<br><br>" +
                        "<font color='#ffed91'>Focussed</font> (33-74)<br>There are some indications of stress, yet this state may be advantageous for work as it is in either a neutral, alert, or slightly stimulated state.<br><br>" +
                        "<font color='#ffad60'>Stressed</font> (75-100)<br>It represents the highest level of stress. Excitation, tension, or high activity are typical and normal, but they indicate the need for relaxation."
            )
        )

        dataList.add(
            StressUnderstandingOverview.ImageWithText(
                "Making Sense of Stress Tracking",
                R.drawable.image_stress_tracking,
                "Stress encompasses both physical and mental responses to internal and external factors. When managed well, stress can foster personal growth in productivity, creativity, and resilience.<br><br>" +
                        "Stress tracking via Luna Ring relies on understanding of the autonomic nervous system (ANS), indicating stress levels and recovery processes. For instance, a lower-than-usual HRV may indicate an overworked sympathetic nervous system, while a faster resting heart rate may signify a stress response."

            )
        )

        dataList.add(
            StressUnderstandingOverview.LottieWithText(
                "Correlating Movement & stress",
                R.raw.anim_correlating_movement,
                "Apart from stress zones, you can examine the correlation between activity and physiological stress. Access this feature by tapping the downward arrow on your stress graph. For instance, it helps you identify times when stress occurred despite absence of medium or high-intensity movement."
            )
        )

        dataList.add(
            StressUnderstandingOverview.LottieWithText(
                "Mapping out Non-Activity stress",
                R.raw.anim_mapping_non_activity,
                "Your Luna ring measures physiological stress your body experiences over the course of the entire day. Non-Activity stress measures stress experienced outside of sleep & workouts. Reviewing this breakdown can provide insights into how your body accumulates and manages stress triggered by emotional or interpersonal factors."
            )
        )

        dataList.add(
            StressUnderstandingOverview.TextWithAdapter(
                "Managing acute stress (Short term)",
                "Acute stress typically arises in response to immediate threats or challenges and tends to subside once the situation is addressed. Strategies for managing acute stress include:",
                getShortTermListText(),
                true
            )
        )

        dataList.add(
            StressUnderstandingOverview.TextWithAdapter(
                "Managing chronic stress (Long term)",
                "Chronic stress persists over an extended period and can stem from various sources, including job loss, financial worries, or serious illness. Strategies for managing chronic stress include:",
                getLongTermListText(),
                false
            )
        )

        return dataList
    }


    private fun getShortTermList(): ArrayList<StressUnderstandingSubList> {
        val dataList = ArrayList<StressUnderstandingSubList>()
        dataList.add(
            StressUnderstandingSubList(
                "Practice breathework",
                R.drawable.bg_dummy_placeholder
            )
        )
        dataList.add(StressUnderstandingSubList("Exercise", R.drawable.bg_dummy_placeholder))
        return dataList
    }

    private fun getShortTermListText(): List<String> {
        val dataList = ArrayList<String>()
        dataList.add("Cultivating new lifestyle habits")
        dataList.add("Practicing breathwork")
        dataList.add("Meditating")
        dataList.add("Exercising")
        dataList.add("Connecting with loved ones")
        dataList.add("Spending time in nature")
        dataList.add("Ensuring high-quality sleep")
        dataList.add("Exploring new hobbies")
        return dataList
    }

    private fun getLongTermList(): ArrayList<StressUnderstandingSubList> {
        val dataList = ArrayList<StressUnderstandingSubList>()
        dataList.add(StressUnderstandingSubList("Meditate", R.drawable.bg_dummy_placeholder))
        dataList.add(
            StressUnderstandingSubList(
                "Connect with loved ones",
                R.drawable.bg_dummy_placeholder
            )
        )
        return dataList
    }

    private fun getLongTermListText(): List<String> {
        val dataList = ArrayList<String>()
        dataList.add("Engaging in regular exercise")
        dataList.add("Incorporating cold exposure, such as cold showers or ice baths")
        dataList.add("Deliberate heat exposure, like saunas")
        dataList.add("Experimenting with intermittent fasting to regulate metabolism and promote overall well-being")
        return dataList
    }
}