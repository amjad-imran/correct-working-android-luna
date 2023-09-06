package com.oreo.ui.sleep.scoredetails

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.BottomSheetDataMetricsBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetDataMetrics : BaseBottomSheetWithTransparent<BottomSheetDataMetricsBinding>(
    BottomSheetDataMetricsBinding::inflate
) {
    val args: BottomSheetDataMetricsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tvData.text = Html.fromHtml(getContent(args.metricsType))

    }

    fun getContent(metricsType: ViewItemClickType): String {
        return when (metricsType) {
            ViewItemClickType.SLEEP_SCORE -> {
                "<h1>Sleep Score</h1><p>Sleep Score is a measure of how well you slept last night." +
                        " It takes into account factors like total sleep time, sleep efficiency, REM sleep, and deep sleep." +
                        " A higher Sleep Score indicates better quality sleep.</p>" +

                        "<p>Optimal: A sleep score > 75 " +
                        "<br>Good: A sleep score between 45 and 74 " +
                        "<br>Pay attention: A sleep score &lt;45</p>" +

                        "<p>An optimal sleep score indicates that your overall sleep quality is optimal and you had undisturbed sleep. A good sleep score indicates that your overall sleep quality is good, but you may have had minor disturbances while asleep. &quot;Pay attention&quot; is indicative of disturbed sleep throughout the night.</p>" +

                        "<p>Aim for a sleep score of >75 every night to reach your maximum potential during the day.</p>"
            }

            ViewItemClickType.TOTAL_SLEEP -> {
                "<h1>Total sleep</h1><p>Total sleep is a metric that indicates the total number of hours you have slept, including light, REM, and deep sleep.<br><br>" +
                        "Most adults need between 7 and 9 hours of sleep to perform well and stay healthy, but individual needs may vary.<br><br>" +
                        "To improve your total sleep score, ensure you are getting enough rest each night. Remember, quality sleep is essential for your overall well-being and productivity.<br>" +
                        "</p>"
            }

            ViewItemClickType.TIME_IN_BED -> {
                "<h1>Time in bed</h1><p>Time in Bed is the total number of hours you spend in bed, including" +
                        " both sleep and awake time.<br><br>" +
                        "Aim for a consistent sleep schedule, create a relaxing bedtime routine, " +
                        "and ensure your sleep environment is comfortable and conducive to good rest.<br><br>" +
                        "Remember, a good night's sleep sets the foundation for a productive day ahead!<br>" +
                        "</p>"
            }

            ViewItemClickType.SLEEP_EFFICIENCY -> {
                "<h1>Sleep efficiency</h1><p>Sleep efficiency is the ratio of your total sleep time to" +
                        " the total time you spent in bed. This parameter gives insight into how many" +
                        " hours you slept versus the time in bed.<br><br>" +
                        "Your sleep efficiency helps you gauge your sleep quality. By monitoring your " +
                        "sleep efficiency, you can make adjustments to your sleep habits and increase" +
                        " the duration of restful sleep vs the time in bed.<br><br>" +
                        "A good night's sleep means more energy and focus throughout the day!<br>" +
                        "</p>"
            }

            ViewItemClickType.RESTING_HR -> {
                "<h1>Resting HR</h1><p>Resting heart rate is the number of times your heart beats per " +
                        "minute when you are at rest. It is typically the lowest when you are asleep." +
                        " It is an important indicator of cardiovascular health as well as overall fitness " +
                        "and recovery.<br><br>" +
                        "The typical range for resting heart rate in adults is between 60 and 100 beats per minute," +
                        " with lower numbers generally indicating better cardiovascular health. When asleep," +
                        " factors such as intense training, late meals, elevated body temperature, and strong" +
                        " emotions like excitement and stress can influence and keep your heart rate elevated." +
                        " Regular exercise, a healthy diet, and stress management play key roles in improving " +
                        "your resting heart rate.<br><br>" +
                        "Monitoring your resting heart rate can help you track improvements in your cardiovascular fitness over time.<br>" +
                        "</p>"
            }

            ViewItemClickType.READINESS_SCORE -> {
                "<h1>Readiness Score</h1><p>The Readiness Score is a measure of how prepared you are to " +
                        "perform at your best based on your activity and sleep over the past weeks. It provides" +
                        " an indication of your readiness to take on the day's challenges.<br><br>" +
                        "Optimal: A readiness score > 75<br>" +
                        "Good: A readiness score between 45 and 74<br>" +
                        "Pay attention: A readiness score &lt;45<br><br>" +
                        "To improve your Readiness Score, aim for an optimal activity score above 75, " +
                        "get between 7-9 hours of total sleep with efficient sleep efficiency (above 84%), " +
                        "and ensure your resting heart rate is within a healthy range.<br><br>" +
                        "Remember, taking care of your physical and mental well-being is essential for achieving optimal readiness levels throughout the day. Keep up the good work!<br>" +
                        "</p>"
            }

            ViewItemClickType.HR_VARIABILITY -> {
                "<h1>Hr variability</h1><p>Heart Rate Variability (HRV) measures the variation in the time between " +
                        "two heartbeats. It is calculated by analyzing the intervals between consecutive heartbeats. " +
                        "It can range from 20 to 120 milliseconds (ms) for a healthy person.<br><br>" +
                        "A high HRV indicates that you have a healthy and more responsive cardiovascular system. " +
                        "It implies that you can adapt to stress with ease. A high HRV indicates optimal fitness.<br><br>" +
                        "To improve your HRV score, focus on activities that promote relaxation and stress reduction, " +
                        "such as deep breathing exercises, meditation, and getting enough sleep.<br><br>" +
                        "Boost your HRV to meet your major health goals!<br>" +
                        "</p>"
            }

            ViewItemClickType.BODY_TEMPERATURE -> {
                "<h1>Body temperature</h1><p>Your skin temperature is a measure of the heat in the outermost " +
                        "surface of the body. It tends to fluctuate throughout the day, in different environments," +
                        " and after physical activity.<br><br>" +
                        "Normal skin temperature for adults is typically between 91.4 - 98.6 degree" +
                        " Fahrenheit, but this can vary slightly from person to person as well as " +
                        "based on where you live. If you consistently have a higher or lower skin temperature," +
                        " it may be worth consulting a healthcare professional.<br><br>" +
                        "To improve your skin temperature readings, make sure to wear the Luna ring " +
                        "consistently and follow the device's instructions for proper placement and usage.<br><br>" +
                        "A stable skin temperature is key to optimal physical function.<br>" +
                        "</p>"
            }

            ViewItemClickType.RESPIRATORY_RATE -> {
                "<h1>Respiratory rate</h1><p>Respiratory rate is a measure of how many breaths you take per minute." +
                        " It is calculated by counting the number of inhalations and exhalations within 60 seconds." +
                        " Adults have an average respiratory rate of around 12 to 18 breaths per minute.<br><br>" +
                        "A healthy respiratory rate implies that your lungs can adjust to physical exertion and " +
                        "also recover smoothly after each session.<br><br>" +
                        "To improve your respiratory rate, you can engage in activities that promote lung " +
                        "health, such as aerobic exercises or deep breathing techniques. Maintaining a " +
                        "healthy lifestyle and avoiding smoking can also contribute to a healthier respiratory rate.<br><br>" +
                        "Remember, maintaining a balanced and healthy respiratory rate is important for " +
                        "overall well-being and optimal performance.<br>" +
                        "</p>"
            }

            ViewItemClickType.ACTIVITY_SCORE -> {
                "<h1>Activity score</h1><p>Activity score is a measure of your level of activity over the day as well as over the past 7 days." +
                        " It is calculated based on the contributors that track your activities throughout the day.</p>" +
                        "<p>Optimal: An activity score >75 <br>Good: An activity score between 45 and 74 <br><br>" +
                        "Pay attention: An activity score &lt;45</p>" +
                        "<p>Stay motivated and keep moving towards a higher activity score!</p>"
            }

            ViewItemClickType.ACTIVE_CALORIES -> {
                "<h1>Active calories</h1><p>The Luna Ring sets the activity goal for you based on your height, weight, age and gender. You get updated through the day based on the calories you burn while active, including exercise and everyday movements. This gives you an understanding of how close or far you are to your day&rsquo;s activity goal<br><br>" +
                        "With the Luna ring, you can track your progress and strive towards achieving optimal activity levels for a healthier lifestyle. Keep moving to achieve your goals!<br><br>" +
                        "</p>"
            }
            ViewItemClickType.TOTAL_CALORIES_BURNED -> {
                "<h1>Total calories</h1><p>Total Burn is a comprehensive measure of daily calorie " +
                        "expenditure, encompassing both active and inactive calories, including " +
                        "those burned during workouts and at rest.<br><br>" +
                        "Focus on staying active throughout the day, completing your calorie goal, " +
                        "and incorporating medium- or high-intensity workouts at least three times a week.<br><br>" +
                        "Keep up the great work and keep striving for an optimal Total Burn!<br>" +
                        "</p>"
            }
            ViewItemClickType.STEPS -> {
                "<h1>Steps</h1><p>The Luna Ring measures your daily steps, which includes any " +
                        "activity that involves walking. We recommend completing 10,000 steps a day.<br><br>" +
                        "You can use this parameter to monitor your physical activity throughout the day and set goals for yourself to encourage more movement. Walking is a simple yet effective way to improve your health, and measuring your steps can help you track your performance.<br><br>" +
                        "Keep stepping up towards a healthier lifestyle!<br>" +
                        "</p>"
            }

            ViewItemClickType.DISTANCE -> {
                "<h1>Distance</h1><p>Distance is a representative metric that quantifies all " +
                        "forms of physical activity as a unit of distance covered throughout the day." +
                        " It takes into account all of your movements, whether it's walking, running," +
                        " or any other physical activity.<br><br>" +
                        "The average daily distance covered by individuals varies widely depending " +
                        "on factors such as age, fitness level, and lifestyle. To improve your distance " +
                        "score, try incorporating more physical activities into your daily routine," +
                        " such as walking or jogging. <br><br>" +
                        "Remember, every step counts towards a healthier you!<br>" +
                        "</p>"
            }
        }

    }

    override fun initListener() {


    }

    override fun subscribeObservers() {

    }
}