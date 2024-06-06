package com.oreo.data.dataConverter

import com.oreo.data.model.PeriodCycleHistory
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.PeriodPos
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject


class FemaleHealthDataConvertor @Inject constructor() {

    suspend fun convertHealthData(
        cycleData: PeriodCycleHistory?,
        shouldGenerateFutureData: Boolean
    ): Flow<FemaleHealthGeneratorResult> {
        return flow {
            emit(FemaleHealthGeneratorResult.Loading(true))

            val mainPeriodLength = 5
            val mainCycleLength = 28

            val healthDataDateList = HashMap<LocalDate, DayState>()

            cycleData?.cycleHistory?.forEach { data ->

                val periodLength = data.periodLength ?: 0
                val cycleLength = data.cycleLength ?: 0

                val periodStart = LocalDate.parse(data.periodDate)
                val periodEnd = if (periodLength == 0) {
                    periodStart
                } else {
                    periodStart.plusDays((periodLength - 1).toLong())
                }

                try {
                    val fWindow = data.fertileWindow?.split("/")

                    val fertileDateStart = LocalDate.parse(fWindow?.get(0))
                    val fertileDateEnd = LocalDate.parse(fWindow?.get(1))

                    var loopDateFertile = fertileDateStart
                    while (loopDateFertile <= fertileDateEnd) {
                        healthDataDateList[loopDateFertile] = DayState.Fertile
                        loopDateFertile = loopDateFertile.plusDays(1)
                    }

                    val ovDate = LocalDate.parse(data.ovulationStartDate)
                    healthDataDateList[ovDate] = DayState.OvulationDay


                } catch (exp: Exception) {
                }

                var loopDate = periodStart
                while (loopDate <= periodEnd) {

                    val state = if (periodEnd == periodStart) {
                        PeriodPos.SINGLE
                    } else {
                        if (loopDate == periodStart) {
                            PeriodPos.START
                        } else if (loopDate == periodEnd) {
                            PeriodPos.END
                        } else {
                            PeriodPos.CENTER
                        }
                    }

                    healthDataDateList[loopDate] = DayState.Period(state)

                    loopDate = loopDate.plusDays(1)
                }
            }


            /**
             * Generate Future data
             */
            if (shouldGenerateFutureData) {
                val currentPeriodStart = if (cycleData?.cycleHistory.isNullOrEmpty()) {
                    LocalDate.parse(cycleData?.userDefault?.firstPeriodDate)
                } else {
                    val lastCycle = cycleData?.cycleHistory?.first()
                    LocalDate.parse(lastCycle?.nextPeriodDate)
                }

                val preProcessDataTill = LocalDate.now().plusMonths(12)

                var current = currentPeriodStart
                while (current <= preProcessDataTill) {

                    val currentDay = getCurrentCycleDay(
                        currentPeriodStart,
                        mainCycleLength,
                        current
                    )


                    val ovDay = (mainCycleLength - 13)

                    if (currentDay == ovDay) {
                        healthDataDateList[current] = DayState.OvulationDay
                        current = current.plusDays(1)
                        continue
                    }

                    if (currentDay in (ovDay - 5)..(ovDay + 1)) {
                        healthDataDateList[current] = DayState.Fertile
                        current = current.plusDays(1)
                        continue
                    }

                    if (currentDay == 1) {
                        healthDataDateList[current] = DayState.Period(PeriodPos.START)
                        current = current.plusDays(1)
                        continue
                    } else if (currentDay == mainPeriodLength) {
                        healthDataDateList[current] = DayState.Period(PeriodPos.END)
                        current = current.plusDays(1)
                        continue
                    }

                    if (currentDay <= mainPeriodLength) {
                        healthDataDateList[current] = DayState.Period(PeriodPos.CENTER)
                        current = current.plusDays(1)
                        continue
                    }

                    current = current.plusDays(1)

                }
            }
            emit(FemaleHealthGeneratorResult.Success(healthDataDateList))
            emit(FemaleHealthGeneratorResult.Loading(false))
        }.flowOn(Dispatchers.IO).catch {

        }
    }

    private fun getCurrentCycleDay(
        periodDate: LocalDate,
        cycleLength: Int,
        currentDate: LocalDate
    ): Int {
        val daysSinceLastPeriod = ChronoUnit.DAYS.between(periodDate, currentDate).toInt()
        return (daysSinceLastPeriod % cycleLength) + 1
    }
}

sealed class FemaleHealthGeneratorResult {

    data class Success(val value: HashMap<LocalDate, DayState>) :
        FemaleHealthGeneratorResult()

    class Loading(val loading: Boolean) : FemaleHealthGeneratorResult()
}