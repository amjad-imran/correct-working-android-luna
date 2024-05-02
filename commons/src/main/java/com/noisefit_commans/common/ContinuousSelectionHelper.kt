package com.noisefit_commans.common


import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.LazyThreadSafetyMode.NONE


data class DateSelection(val startDate: LocalDate? = null, val endDate: LocalDate? = null) {
    val daysBetween by lazy(NONE) {
        if (startDate == null || endDate == null) {
            null
        } else {
            dayDiff(startDate, endDate)
        }
    }
}

private fun dayDiff(startDate: LocalDate, endDate: LocalDate): Long {
    val startDate1: Date =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDate.toString())
    val endDate1: Date =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDate.toString())
    val mDifference = kotlin.math.abs(startDate1.time - endDate1.time)
    return mDifference / (24 * 60 * 60 * 1000)
}

//private val rangeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

/*
@RequiresApi(Build.VERSION_CODES.O)
fun dateRangeDisplayText(startDate: LocalDate, endDate: LocalDate): String {
    return "Selected: ${rangeFormatter.format(startDate)} - ${rangeFormatter.format(endDate)}"
}
*/


object ContinuousSelectionHelper {

    fun convertLocalDateToDate(date: LocalDate): Date {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date.toString())
    }
    fun getSelection(
        clickedDate: LocalDate,
        dateSelection: DateSelection,
        selectionStartDate: LocalDate,
        selectionEndDate: LocalDate
    ): DateSelection {
//        val (selectionStartDate, selectionEndDate) = dateSelection
        val tempClickDate= convertLocalDateToDate(clickedDate)
        val tempSelectedStartDate= convertLocalDateToDate(selectionStartDate)
        val tempSelectedEndDate= convertLocalDateToDate(selectionEndDate)

        return if (tempSelectedStartDate != null) {
            if (tempClickDate.before(tempSelectedStartDate) || tempSelectedEndDate != null) {
                DateSelection(startDate = clickedDate, endDate = null)
            } else if (clickedDate != selectionStartDate) {
                DateSelection(startDate = selectionStartDate, endDate = clickedDate)
            } else {
                DateSelection(startDate = clickedDate, endDate = null)
            }
        } else {
            DateSelection(startDate = clickedDate, endDate = null)
        }
    }


}
