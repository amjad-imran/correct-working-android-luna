package com.noisefit.data.local.db.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.noisefit_commans.models.FavouriteWatchFace
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
class FavouriteWatchFaceDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var database: DataBase


    private lateinit var watchFaceDao: FavouriteWatchFaceDao

    @Before
    fun setup() {
        hiltRule.inject()
        watchFaceDao = database.favouriteWatchFaceDao()
    }

    @After
    fun destroy() {
        database.close()
    }

    @Test
    fun insertFavouriteWatchface() {
        val watchfaceObject = Gson().fromJson<FavouriteWatchFace>(
            "{\n" +
                    "\"image_url\": \"https://images.gonoise.com/watch_faces/production/colorfit_pro3_ultra_ocean/Ocean(N002)/Watchface-4/img_effect.png\",\n" +
                    "\"watchface_id\": 126,\n" +
                    "\"downloads\": 1018,\n" +
                    "\"name\": \"Cat fever\",\n" +
                    "\"is_favourite\": \"1\"\n" +
                    "}",
            FavouriteWatchFace::class.java
        )
        //sportsModeResponse.date = "" TODO Dynamic
        watchFaceDao.insert(watchfaceObject)
        val allWatchfaces = watchFaceDao.getFavouriteWatchFaces()
        assertThat(allWatchfaces?.contains(watchfaceObject))
    }

    @Test
    fun deleteActivity() {
        val watchfaceObject = Gson().fromJson<FavouriteWatchFace>(
            "{\n" +
                    "\"image_url\": \"https://images.gonoise.com/watch_faces/production/colorfit_pro3_ultra_ocean/Ocean(N002)/Watchface-4/img_effect.png\",\n" +
                    "\"watchface_id\": 126,\n" +
                    "\"downloads\": 1018,\n" +
                    "\"name\": \"Cat fever\",\n" +
                    "\"is_favourite\": \"1\"\n" +
                    "}",
            FavouriteWatchFace::class.java
        )
        watchFaceDao.insert(watchfaceObject)
        val allWatchfaces = watchFaceDao.getFavouriteWatchFaces()
        assertThat(allWatchfaces?.contains(watchfaceObject)?.not())

    }

}