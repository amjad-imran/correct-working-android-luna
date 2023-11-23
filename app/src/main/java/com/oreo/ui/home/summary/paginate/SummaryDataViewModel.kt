package com.oreo.ui.home.summary.paginate

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.OreoDashboardResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class SummaryDataViewModel : BaseViewModel() {


    fun parseHealthData(data: OreoDashboardResponseModel) {


    }

    private fun getInitialOfflineData(data: OreoDashboardResponseModel) {


        viewModelScope.launch(Dispatchers.IO) {

            val userActivities = ArrayList<OHealthOverview>()
            val viewedCardsData = ArrayList<OHealthOverview>()

            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
            }
            //userActivities.add(OHealthOverview.AutoSport(2))


            ringDataStore.setRegisterDay(data.registerDate ?: -1)


            LOGS.w("RESPONSE___ ${Gson().toJson(data)}")

            handleInfoCards(data, userActivities, viewedCardsData)


            when (getDaySlot()) {
                0 -> {
                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (data.registerDate != 0) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }
                }

                1 -> {

                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (data.registerDate != 0) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }

                    //Activity
                    if (data.activity?.activeCalories != null) {
                        val activeCalories = data.activity.activeCalories
                        if (activeCalories in 1..49) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else if (activeCalories >= 50) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                        }
                    }
                }

                2 -> {
                    if (data.registerDate != 0) {
                        data.readiness?.let {
                            userActivities.add(OHealthOverview.Readiness(data.readiness))
                        }

                        data.sleep?.let {
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    }

                    data.activity?.let {

                        val activeCalories = data.activity.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        }
                    }


                }

                else -> {
                    data.activity?.let {

                        val activeCalories = data.activity.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        }
                    }

                    if (data.registerDate != 0) {
                        if (data.sleep?.sleepScore != null) {
                            userActivities.add(
                                OHealthOverview.SleepMinimal(
                                    data.sleep,
                                    makeSleepArray(data.sleep)
                                )
                            )

                            data.readiness?.let {
                                userActivities.add(OHealthOverview.ReadinessMinimal(data.readiness))
                            }

                        } else {
                            data.sleep?.let {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        data.sleep,
                                        makeSleepArray(data.sleep),
                                        data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                        data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                    )
                                )
                            }
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                        }
                    }
                }
            }


            /* if (isMorningTime()) {
                 if (data.registerDate != 0) {
                     data.readiness?.let {
                         userActivities.add(OHealthOverview.Readiness(data.readiness))
                     }


                     data.sleep?.let {
                         userActivities.add(
                             OHealthOverview.Sleep(
                                 data.sleep,
                                 makeSleepArray(data.sleep),
                                 data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                 data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                             )
                         )
                     }
                 }

                 data.activity?.let {
                     val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                     userActivities.add(OHealthOverview.Activity(data.activity, caloriesGoal))
                 }
             } else {

                 data.activity?.let {
                     val caloriesGoal = summary.user?.userGoals?.caloriesGoal ?: 0
                     userActivities.add(OHealthOverview.Activity(data.activity, caloriesGoal))
                 }

                 if (data.registerDate != 0) {
                     data.readiness?.let {
                         userActivities.add(OHealthOverview.Readiness(data.readiness))
                     }
                     data.sleep?.let {
                         userActivities.add(
                             OHealthOverview.Sleep(
                                 data.sleep,
                                 makeSleepArray(data.sleep),
                                 data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                 data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                             )
                         )
                     }
                 }

             }*/

            stateSleepAvgCard.postValue(Pair(data.sleepScoreAvg, data.activityScoreAvg))
            stateReadinessAvgCard.postValue(data.readinessScoreAvg)

            summary.viewedCardsData.postValue(viewedCardsData)
            summary.healthOverviewData.postValue(userActivities)

            val device = ringDataStore.getRingDevice()
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })
            getRecentWorkoutList()

        }
    }



}