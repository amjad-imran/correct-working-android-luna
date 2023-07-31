package com.noisefit.ui.settings.help

import com.noisefit_commans.data.model.FitnessHealthModel
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FitnessHealthViewModel @Inject constructor(
    private val localDataStore: DataStoredInterface,
    private val deviceRepository: DeviceRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {
    var listData = ArrayList<FitnessHealthModel>()
    var title: String = ""

    fun setDataInfoList(data: ArrayList<FitnessHealthModel>) {
        listData = data
    }

    fun getDataInfoList(): ArrayList<FitnessHealthModel> {
        return listData
    }

    fun tempHeartRateData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "What is Heart Rate?",
            "What are Heart Rate zones?",
            "What is Resting heart rate?",
            "Disclaimer"
        )
        val msgArray = arrayListOf(
            "Your heart rate is the number of times your heart beats per minute.",
            "Your maximum heart rate is divided into five different zones. Understanding these zones can help you control the intensity of your workout.\n\n" +
                    "Zone 1: Super light: 50 to 60% of maximum heart rate.\n\n" +
                    "Zone 2: Light: 60 to 70% of maximum heart rate.\n\n" +
                    "Zone 3: Moderate: 70 to 80% of maximum heart rate.\n\n" +
                    "Zone 4: Hard: 80 to 90% of maximum heart rate.\n\n" +
                    "Zone 5: Super hard: 90 to 100% of maximum heart rate.",
            "Resting heart rate is the number of times a heart beats per minute while you are at rest. A healthy adult's heart beats around 60 to 100 times per minute.",
            "- The heart rate information is for non-medical purpose. \n\n" +
                    "- It can only be used for general fitness and wellness purposes. \n\n" +
                    "- In case you are feeling any difficulties, we suggest you consult a doctor immediately. "
        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

    fun bodyTemperatureData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "What is body temperature, and how is it measured?",
            "What is the normal range of body temperature?",
            "What to do in case of a high body temperature? "
        )
        val msgArray = arrayListOf(
            "Body temperature measures how well your body can make and get rid of the heat. Your temperature varies during the day owing to several factors – activity levels, gender, age, what you’ve eaten or had to drink, what time of the day it is, where you are in your menstrual cycle, etc.",
            "A normal body temperature for a typical adult can be anywhere between 97 F to 99 F. Babies and children have a slightly higher range: 97.9 F to 100.4 F.",
            "Some common ways to cool down the body’s temperature in case of fever are:\n\n" +
                    "1. Stay hydrated – Drink water, iced tea, and very diluted juices to replenish the water loss due to sweat.\n\n" +
                    "2. Stay cool – remove extra layers of clothing and blankets unless you have the chills.\n\n" +
                    "3. Take a lukewarm bath or use a cold compress to relieve the fever.\n\n" +
                    "4. You can also take over-the-counter medicines only after consulting with your doctor.\n\n" +
                    "If the fever persists or you have any concerns, you should definitely consult your doctor."
        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

    fun distanceData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "How much distance should you walk to keep yourself fit?",
            "How does walking help your health?",
            "Tips to help you walk more"
        )
        val msgArray = arrayListOf(
            "An average person should aim to cover 8 km daily, which is equivalent to 10000 steps.",
            "Walking offers numerous health benefits to people of all ages. It helps you:\n\n" +
                    "1. Burn calories\n\n" +
                    "2. Strengthen the heart\n\n" +
                    "3. Lower blood sugar levels\n\n" +
                    "4. Ease joint pain\n\n" +
                    "5. Boost immunity",
            "1. Get up at regular intervals. You can set a reminder for the same.\n\n" +
                    "2. Always take the stairs\n\n" +
                    "3. Walk (run) an errand instead of ordering in\n\n" +
                    "4. Listen to music and podcasts while walking\n\n" +
                    "5. Park your car far away to get in those extra steps\n\n" +
                    "6. Schedule walk meeting\n\n" +
                    "7. Meet friends for a walk\n\n" +
                    "8. Walk and talk on calls "
        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

    fun sleepData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "How much sleep do you need?",
            "How does sleep help your overall health?",
            "What are the different stages of sleep?",
            "Tips to get better sleep"
        )
        val msgArray = arrayListOf(
            "An average adult should get at least 7-9 hours of sleep.",
            "Getting a good night's sleep can help you maintain a healthy weight, improve concentration and productivity, strengthen your heart, boost your immune system, prevent mental ailments, and reduce inflammation in the body.",
            "Sleep is divided into 5 stages: \n\n" +
                    "Stage 1: This is the lightest stage of sleep during which you can wake easy, and typically lasts for a few minutes.\n\n" +
                    "Stage 2: This is a progression of the first stage. Sleep is still fairly light, as your brain waves start slowing down.\n\n" +
                    "Stage 3 to 4: Through each stage, you fall deeper into sleep which becomes harder to interrupt. During this time your body gets revitalised and immune function gets boosted.\n\n" +
                    "Stage 5 or REM: In the last stage known as REM (rapid eye movement), you begin to dream as brain activity increases. Long term memories and information is processed during this stage.",
            "In order to get a better sleep, stick to your schedule and avoid daytime naps, turn your bed into a sleep-inducing environment, be in sync with your body's natural sleep-wake cycle, control your exposure to light, exercise during the day and be mindful about what you eat and drink."
        )
        val hyperLinkArray = arrayListOf(
            "https://www.sleepfoundation.org/how-sleep-works/how-much-sl(eep-do-we-really-need",
            "https://www.medicalnewstoday.com/articles/325353#more-social-and-emotional-intelligence",
            "https://www.gonoise.com/blogs/posts/crash-course-to-tracking-your-daily-sleep",
            "https://www.helpguide.org/articles/sleep/getting-better-sleep.htm https://www.mayoclinic.org/healthy-lifestyle/adult-health/in-depth/sleep/art-20048379"


        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            child.hyperLink = hyperLinkArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

    fun stressData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "What is stress?",
            "What is automatic stress testing?",
            "Tips to reduce stress",
        )
        val msgArray = arrayListOf(
            "Stress is the feeling of being overwhelmed or unable to cope with mental or emotional pressure. It's your body's natural reaction to being under constant pressure or threat.",
            "Automatic stress testing in your smartwatch can help you detect your stress levels and take a timeout when needed.",
            "You can manage the elevated levels of your stress by:\n\n" +
                    "- Trying meditation\n\n" +
                    "- Exercising regularly\n\n" +
                    "- Maintaining a healthy sleep schedule\n\n" +
                    "- Take breaks at regular intervals during working hours\n"
        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

    fun bloodOxygenData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "What is blood oxygen?",
            "How to measure SpO2?",
            "What causes a decrease in SpO2 levels?",
            "Disclaimer",
            "Note: Please reach out to your healthcare professional, if you notice any dip in your SpO2 levels."
        )
        val msgArray = arrayListOf(
            "When you inhale oxygen, it goes to your bloodstream through your lungs. This amount of oxygen present in your blood is called blood oxygen level. Your body needs a certain amount of oxygen to properly function, and thus its fluctuating levels can lead to complications.\n\n" +
                    "The oxygen saturation level between 95% and 100% is considered normal.",
            "Hold still while measuring SpO2 on your smartwatch, and make sure that there is no gap between the back panel of the watch and the wrist.",
            "Many underlying conditions can interfere with the body’s ability to deliver normal levels of oxygen. Some of the most common causes are:\n\n" +
                    "- Heart conditions, including heart defects\n\n" +
                    "- Lung conditions such as asthma, emphysema, and bronchitis\n\n" +
                    "- Locations of high altitudes, where oxygen in the air is lower\n\n" +
                    "- Strong pain medications or other problems that slow breathing\n\n" +
                    "- Sleep apnea (impaired breathing during sleep)\n\n" +
                    "- Inflammation or scarring of the lung tissue (as in pulmonary fibrosis)",
            "- The blood oxygen information is for non-medical purpose.\n\n" +
                    "- It can only be used for general fitness and wellness purposes.\n\n" +
                    "- In case you are feeling any difficulties, we suggest you consult a doctor immediately.",
            ""

        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

    fun stepData(): ArrayList<FitnessHealthModel> {
        val tempHeart = ArrayList<FitnessHealthModel>()
        val titleArray = arrayListOf(
            "How many steps should you take to keep yourself fit?",
            "How does walking help your health?",
            "Tips to help you walk more"
        )
        val msgArray = arrayListOf(
            "An average person should take at least 10000 steps a day.",
            "Walking offers numerous health benefits to people of all ages. It helps you:\n\n" +
                    "1. Burn calories\n\n" +
                    "2. Strengthen the heart\n\n" +
                    "3. Lower blood sugar levels\n\n" +
                    "4. Ease joint pain\n\n" +
                    "5. Boost immunity",
            "Here are a few general tips to help you walk more:\n\n" +
                    "Do a few warm up exercises before the walk\n\n" +
                    "Wear comfortable socks and shoes\n\n" +
                    "Drink plenty of water\n\n" +
                    "Walk at a steady pace, swing your arms freely and stand as straight as you can"
        )
        val hyperLinkArray = arrayListOf(
            "https://www.healthline.com/health/how-many-steps-a-day#Why-10,000-steps?",
            "https://www.healthline.com/health/benefits-of-walking#safety",
            "https://www.betterhealth.vic.gov.au/health/healthyliving/walking-tips"

        )
        for (it in titleArray.indices) {
            val child = FitnessHealthModel()
            child.title = titleArray[it]
            child.msg = msgArray[it]
            child.hyperLink = hyperLinkArray[it]
            tempHeart.add(child)
        }

        return tempHeart
    }

}