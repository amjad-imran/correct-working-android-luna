package com.oreo.data.db.implementation

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.model.OreoHeartRate
import com.oreo.data.db.database.OreoHeartRateDao
import kotlinx.coroutines.runBlocking
import org.junit.Test

class OreoHeartRateDataImplTest {

    @Test
    fun `insertData preserves existing non-zero heart-rate values when new payload has gaps`() = runBlocking {
        val dao = FakeOreoHeartRateDao()
        val source = OreoHeartRateDataImpl(dao)

        source.insertData(
            OreoHeartRate(
                breakUp = Gson().toJson(listOf(72, 0, 81, 0)),
                date = "2026-04-15"
            )
        )

        source.insertData(
            OreoHeartRate(
                breakUp = Gson().toJson(listOf(0, 76, 0, 0)),
                date = "2026-04-15"
            )
        )

        val stored = Gson().fromJsonList(dao.stored?.breakUp ?: "")
        assertThat(stored).containsExactly(72, 76, 81, 0).inOrder()
    }

    @Test
    fun `insertData updates heart-rate breakup when distribution changes but total sum stays same`() = runBlocking {
        val dao = FakeOreoHeartRateDao()
        val source = OreoHeartRateDataImpl(dao)

        source.insertData(
            OreoHeartRate(
                breakUp = Gson().toJson(listOf(60, 90, 0, 0)),
                date = "2026-04-15"
            )
        )

        source.insertData(
            OreoHeartRate(
                breakUp = Gson().toJson(listOf(90, 60, 0, 0)),
                date = "2026-04-15"
            )
        )

        val stored = Gson().fromJsonList(dao.stored?.breakUp ?: "")
        assertThat(stored).containsExactly(90, 60, 0, 0).inOrder()
    }

    private class FakeOreoHeartRateDao : OreoHeartRateDao {
        var stored: OreoHeartRate? = null

        override fun insert(obj: OreoHeartRate): Long {
            stored = obj.copy(id = 1)
            return 1L
        }

        override fun insert(vararg obj: OreoHeartRate) {
            stored = obj.lastOrNull()?.copy(id = 1)
        }

        override fun insertAll(obj: List<OreoHeartRate>): LongArray {
            stored = obj.lastOrNull()?.copy(id = 1)
            return longArrayOf()
        }

        override fun insertAllForRepeated(obj: List<OreoHeartRate>): LongArray {
            stored = obj.lastOrNull()?.copy(id = 1)
            return longArrayOf()
        }

        override fun update(obj: OreoHeartRate) {
            stored = obj
        }

        override fun delete(obj: OreoHeartRate) {
            if (stored?.id == obj.id) {
                stored = null
            }
        }

        override fun getTodayData(date: String): OreoHeartRate? {
            return stored?.takeIf { it.date == date }
        }

        override fun updateViaDate(breakUp: String, date: String, is_synced: Boolean) {
            stored = stored?.copy(breakUp = breakUp, date = date, isSynced = is_synced)
        }

        override fun deleteTodayData(date: String) {
            if (stored?.date == date) {
                stored = null
            }
        }

        override fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean) {
            if (stored?.id in ids) {
                stored = stored?.copy(isGoogleFitSynced = is_google_fit_sync)
            }
        }

        override fun getServerUnSyncData(is_synced: Boolean): List<OreoHeartRate>? {
            return stored?.takeIf { it.isSynced == is_synced }?.let(::listOf)
        }

        override fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int {
            if (stored?.id in ids) {
                stored = stored?.copy(isSynced = is_synced)
                return 1
            }
            return 0
        }

        override fun deleteOlderData(day: Int): Int = 0
    }

    private fun Gson.fromJsonList(json: String): List<Int> {
        return fromJson(json, object : TypeToken<List<Int>>() {}.type)
    }
}
