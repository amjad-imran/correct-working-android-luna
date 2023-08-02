package com.noisefit.data.local.db.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.noisefit_commans.models.SportsModeResponse
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class ActivityDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: DataBase


    private lateinit var activityDao: ActivityDao

    @Before
    fun setup() {
        hiltRule.inject()
        activityDao = database.activityDao()
    }

    @After
    fun destroy() {
        database.close()
    }

    @Test
    fun insertActivity() {
        val sportsModeResponse = Gson().fromJson<SportsModeResponse>(
            "{\"aerobic_minutes\":0,\"anaerobic_minutes\":0,\"averageSWOLF\":0,\"avg_step_frequency\":0,\"avg_step_stride\":80,\"cadence\":52,\"calories\":4,\"date\":\"22/7/2022\",\"distance\":54,\"duration\":77,\"fat_burn_minutes\":0,\"heart_rate_available\":0,\"heart_rate_average\":95,\"heart_rate_data\":[88,126,88,96,88,91],\"heart_rate_maximum\":126,\"isHrZoneInSeconds\":1,\"id\":0,\"isSynced\":false,\"max_step_frequency\":0,\"max_step_stride\":83,\"pace\":1.4,\"speed\":0.7,\"steps\":67,\"time\":\"2022-08-22T15:00:01Z\",\"totalStrokesNumber\":0,\"type\":\"outdoor_running\",\"warm_up_minutes\":0}",
            SportsModeResponse::class.java
        )
        //sportsModeResponse.date = "" TODO Dynamic
        activityDao.insertActivity(sportsModeResponse)
        val allActivities = activityDao.getActivities()
        assertThat(allActivities.contains(sportsModeResponse))
    }

    @Test
    fun deleteActivity() {
        val sportsModeResponse = Gson().fromJson<SportsModeResponse>(
            "{\"aerobic_minutes\":0,\"anaerobic_minutes\":0,\"averageSWOLF\":0,\"avg_step_frequency\":0,\"avg_step_stride\":80,\"cadence\":52,\"calories\":4,\"date\":\"22/7/2022\",\"distance\":54,\"duration\":77,\"fat_burn_minutes\":0,\"heart_rate_available\":0,\"heart_rate_average\":95,\"heart_rate_data\":[88,126,88,96,88,91],\"heart_rate_maximum\":126,\"isHrZoneInSeconds\":1,\"id\":0,\"isSynced\":false,\"max_step_frequency\":0,\"max_step_stride\":83,\"pace\":1.4,\"speed\":0.7,\"steps\":67,\"time\":\"2022-08-22T15:00:01Z\",\"totalStrokesNumber\":0,\"type\":\"outdoor_running\",\"warm_up_minutes\":0}",
            SportsModeResponse::class.java
        )
        //sportsModeResponse.date = "" TODO Dynamic
        activityDao.insertActivity(sportsModeResponse)
        activityDao.deleteActivities()
        val allActivities = activityDao.getActivities()
        assertThat(allActivities.contains(sportsModeResponse).not())


    }

}