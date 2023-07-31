package com.noisefit.data.dataConverter

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepType

object DummyList {

    fun getDummySleepData(): Pair<CountCardData, ArrayList<SleepData.SleepDataBreakup>> {

        val countCData = CountCardData(
            type = "Sleep",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "8h"
        countCData.countSubText = "sub"
        countCData.leftValue = "9:25 AM"
        countCData.rightValue = "3:21 PM"


        val sleepArray = arrayListOf(
            SleepData.SleepDataBreakup(sleepType = SleepType.LIGHT.type, duration = 30),
            SleepData.SleepDataBreakup(sleepType = SleepType.DEEP.type, duration = 60),
            SleepData.SleepDataBreakup(sleepType = SleepType.REM.type, duration = 10),
            SleepData.SleepDataBreakup(sleepType = SleepType.LIGHT.type, duration = 30),
            SleepData.SleepDataBreakup(sleepType = SleepType.REM.type, duration = 10),
            SleepData.SleepDataBreakup(sleepType = SleepType.LIGHT.type, duration = 30),
            SleepData.SleepDataBreakup(sleepType = SleepType.REM.type, duration = 10),
            SleepData.SleepDataBreakup(sleepType = SleepType.LIGHT.type, duration = 30),
            SleepData.SleepDataBreakup(sleepType = SleepType.REM.type, duration = 10),
            SleepData.SleepDataBreakup(sleepType = SleepType.LIGHT.type, duration = 30),
            SleepData.SleepDataBreakup(sleepType = SleepType.REM.type, duration = 10),
            SleepData.SleepDataBreakup(sleepType = SleepType.LIGHT.type, duration = 30),
            SleepData.SleepDataBreakup(sleepType = SleepType.REM.type, duration = 10),
            SleepData.SleepDataBreakup(sleepType = SleepType.AWAKE.type, duration = 40)
        )
        return Pair(countCData, sleepArray)
    }


    fun getDummyStressData(): Pair<ArrayList<BarEntry>, ArrayList<Int>> {
        val values = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()

        values.add(BarEntry(0f, 50f))
        values.add(BarEntry(1f, 12f))
        values.add(BarEntry(2f, 62f))
        values.add(BarEntry(3f, 92f))
        values.add(BarEntry(4f, 2f))
        values.add(BarEntry(5f, 2f))
        values.add(BarEntry(6f, 40f))
        values.add(BarEntry(7f, 50f))
        values.add(BarEntry(8f, 60f))
        values.add(BarEntry(9f, 80f))
        values.add(BarEntry(10f, 0f))
        values.add(BarEntry(11f, 0f))
        values.add(BarEntry(12f, 89f))
        values.add(BarEntry(13f, 4f))
        values.add(BarEntry(14f, 54f))
        values.add(BarEntry(15f, 32f))
        values.add(BarEntry(16f, 76f))
        values.add(BarEntry(17f, 90f))
        values.add(BarEntry(18f, 1f))
        values.add(BarEntry(19f, 12f))
        values.add(BarEntry(20f, 22f))
        values.add(BarEntry(21f, 92f))
        values.add(BarEntry(22f, 90f))
        values.add(BarEntry(23f, 80f))
        NoiseFitApplicationMain.context?.apply {
            colors.add(this.resources.getColor(R.color.stress_normal))
            colors.add(this.resources.getColor(R.color.stress_relax))
            colors.add(this.resources.getColor(R.color.stress_medium))
            colors.add(this.resources.getColor(R.color.stress_high))
            colors.add(this.resources.getColor(R.color.stress_relax))
            colors.add(this.resources.getColor(R.color.stress_normal))
            colors.add(this.resources.getColor(R.color.stress_normal))
            colors.add(this.resources.getColor(R.color.stress_medium))
            colors.add(this.resources.getColor(R.color.stress_normal))

            colors.add(this.resources.getColor(R.color.stress_high))
            colors.add(this.resources.getColor(R.color.stress_relax))
            colors.add(this.resources.getColor(R.color.stress_relax))

            colors.add(this.resources.getColor(R.color.stress_high))
            colors.add(this.resources.getColor(R.color.stress_relax))

            colors.add(this.resources.getColor(R.color.stress_normal))
            colors.add(this.resources.getColor(R.color.stress_normal))

            colors.add(this.resources.getColor(R.color.stress_medium))
            colors.add(this.resources.getColor(R.color.stress_high))

            colors.add(this.resources.getColor(R.color.stress_relax))

            colors.add(this.resources.getColor(R.color.stress_relax))
            colors.add(this.resources.getColor(R.color.stress_relax))

            colors.add(this.resources.getColor(R.color.stress_high))
            colors.add(this.resources.getColor(R.color.stress_high))
            colors.add(this.resources.getColor(R.color.stress_high))
        }


        return Pair(values, colors)
    }


    fun getDummyBodyTempData(): Pair<ArrayList<BarEntry>, ArrayList<Int>> {
        val values = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()

        values.add(BarEntry(0f, 50f))
        values.add(BarEntry(1f, 12f))
        values.add(BarEntry(2f, 62f))
        values.add(BarEntry(3f, 92f))
        values.add(BarEntry(4f, 2f))
        values.add(BarEntry(5f, 2f))
        values.add(BarEntry(6f, 40f))
        values.add(BarEntry(7f, 50f))
        values.add(BarEntry(8f, 60f))
        values.add(BarEntry(9f, 80f))
        values.add(BarEntry(10f, 0f))
        values.add(BarEntry(11f, 0f))
        values.add(BarEntry(12f, 89f))
        values.add(BarEntry(13f, 4f))
        values.add(BarEntry(14f, 54f))
        values.add(BarEntry(15f, 32f))
        values.add(BarEntry(16f, 76f))
        values.add(BarEntry(17f, 90f))
        values.add(BarEntry(18f, 1f))
        values.add(BarEntry(19f, 12f))
        values.add(BarEntry(20f, 22f))
        values.add(BarEntry(21f, 92f))
        values.add(BarEntry(22f, 90f))
        values.add(BarEntry(23f, 80f))
        NoiseFitApplicationMain.context?.apply {
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))

            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
            colors.add(this.resources.getColor(R.color.body_temp))
        }


        return Pair(values, colors)
    }

    fun getDummyDistanceData(): ArrayList<BarEntry> {
        val values = ArrayList<BarEntry>()

        values.add(BarEntry(0f, 50f))
        values.add(BarEntry(1f, 12f))
        values.add(BarEntry(2f, 62f))
        values.add(BarEntry(3f, 92f))
        values.add(BarEntry(4f, 2f))
        values.add(BarEntry(5f, 2f))
        values.add(BarEntry(6f, 40f))
        values.add(BarEntry(7f, 50f))
        values.add(BarEntry(8f, 60f))
        values.add(BarEntry(9f, 80f))
        values.add(BarEntry(10f, 0f))
        values.add(BarEntry(11f, 0f))
        values.add(BarEntry(12f, 89f))
        values.add(BarEntry(13f, 4f))
        values.add(BarEntry(14f, 54f))
        values.add(BarEntry(15f, 32f))
        values.add(BarEntry(16f, 76f))
        values.add(BarEntry(17f, 90f))
        values.add(BarEntry(18f, 1f))
        values.add(BarEntry(19f, 12f))
        values.add(BarEntry(20f, 22f))
        values.add(BarEntry(21f, 92f))
        values.add(BarEntry(22f, 90f))
        values.add(BarEntry(23f, 80f))


        return values
    }

    fun getTodayHeartRateDummyData(): ArrayList<Entry> {
        val data = ArrayList<Entry>()
        data.add(Entry(0f, 60f))
        data.add(Entry(1f, 30f))
        data.add(Entry(2f, 10f))
        data.add(Entry(3f, 30f))
        data.add(Entry(4f, 10f))
        data.add(Entry(5f, 30f))
        data.add(Entry(6f, 10f))
        data.add(Entry(7f, 30f))
        data.add(Entry(8f, 20f))
        data.add(Entry(9f, 60f))
        data.add(Entry(10f, 60f))
        data.add(Entry(11f, 30f))
        data.add(Entry(12f, 10f))
        data.add(Entry(13f, 30f))
        data.add(Entry(14f, 10f))
        data.add(Entry(15f, 30f))
        data.add(Entry(16f, 10f))
        data.add(Entry(17f, 30f))
        data.add(Entry(18f, 20f))
        data.add(Entry(19f, 60f))
        data.add(Entry(20f, 20f))
        data.add(Entry(21f, 60f))
        data.add(Entry(22f, 20f))
        data.add(Entry(23f, 60f))
        return data
    }
}