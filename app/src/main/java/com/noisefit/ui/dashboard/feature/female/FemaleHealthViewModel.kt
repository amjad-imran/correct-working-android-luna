package com.noisefit.ui.dashboard.feature.female

import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.MenstrualData
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FemaleHealthViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var lastMsyear: Int? = null
    var lastMsMonth: Int? = null
    var lastMsDay: Int? = null
    var menstrualData: MenstrualData = MenstrualData(false)

    var reminderTimeMinute = 0
    var reminderTimeHour = 9

    var msReminderAdvance: Int = 1
    var ovulationReminderAdvance: Int = 1

    fun initMsData(msData: MenstrualData?) {
        if (msData == null) {
            menstrualData = MenstrualData(status = false)
        }
        menstrualData = msData!!

        if (menstrualData.menstrualCycleLength == 0) {
            menstrualData.menstrualCycleLength = 25
        }
        if (menstrualData.menstrualLength == 0) {
            menstrualData.menstrualLength = 2
        }
        if (menstrualData.menstrualReminder == null) {
            menstrualData.menstrualReminder = MenstrualData.MenstrualReminder()
        }
        if (menstrualData.menstrualReminder!!.remindOvulationDayBefore == 0) {
            menstrualData.menstrualReminder!!.remindOvulationDayBefore = 1
        }
        if (menstrualData.menstrualReminder!!.remindStartDayBefore == 0) {
            menstrualData.menstrualReminder!!.remindStartDayBefore = 1
        }
        if (menstrualData.menstrualReminder!!.reminderTime.isNullOrEmpty()) {
            val calendar = Calendar.getInstance()
            menstrualData.menstrualReminder!!.reminderTime =
                DateFormats.timeFormat.format(calendar.time)
        }

        if (menstrualData.lastMenstrualDate.isEmpty()) {
            val cal = Calendar.getInstance()
            lastMsDay = cal.get(Calendar.DAY_OF_MONTH)
            lastMsMonth = cal.get(Calendar.MONTH) + 1
            lastMsyear = cal.get(Calendar.YEAR)
            menstrualData.lastMenstrualDate =
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)}/${cal.get(Calendar.YEAR)}"
        } else {
            try {
                val cal = DateFormats.getCalendarFromDate(
                    menstrualData.lastMenstrualDate,
                    DateFormats.dateFormat
                )
                lastMsDay = cal.get(Calendar.DAY_OF_MONTH)
                lastMsMonth = cal.get(Calendar.MONTH) + 1
                lastMsyear = cal.get(Calendar.YEAR)
            } catch (exp: Exception) {
                val cal = Calendar.getInstance()
                lastMsDay = cal.get(Calendar.DAY_OF_MONTH)
                lastMsMonth = cal.get(Calendar.MONTH) + 1
                lastMsyear = cal.get(Calendar.YEAR)
                menstrualData.lastMenstrualDate =
                    "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)}/${cal.get(Calendar.YEAR)}"
            }
        }
        menstrualData.menstrualReminder?.let {
            msReminderAdvance = it.remindStartDayBefore
            ovulationReminderAdvance = it.remindOvulationDayBefore
            try {
                it.reminderTime?.let { time ->
                    reminderTimeHour = time.split(":")[0].toInt()
                    reminderTimeMinute = time.split(":")[1].toInt()
                }
            } catch (exp: Exception) {
            }
        }

    }

    fun getLastMsDate(): String {
        return if (lastMsDay == null) {
            val cal = Calendar.getInstance()
            "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)}/${cal.get(Calendar.YEAR)}"
        } else {
            "${lastMsDay}/${lastMsMonth}/${lastMsyear}"
        }
    }
}