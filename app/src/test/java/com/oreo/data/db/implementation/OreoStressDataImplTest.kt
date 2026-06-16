package com.oreo.data.db.implementation

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.oreo.data.db.database.OreoStressDao
import kotlinx.coroutines.runBlocking
import org.junit.Test

class OreoStressDataImplTest {

    @Test
    fun `insertData preserves existing non-zero stress values when new payload has gaps`() = runBlocking {
        val dao = FakeOreoStressDao()
        val source = OreoStressDataImpl(dao)

        source.insertData(
            OreoStressDataBreakup(
                breakUp = Gson().toJson(listOf(15, 0, 45, 0)),
                date = "2026-04-15"
            )
        )

        source.insertData(
            OreoStressDataBreakup(
                breakUp = Gson().toJson(listOf(0, 30, 0, 0)),
                date = "2026-04-15"
            )
        )

        val stored = Gson().fromJsonList(dao.stored?.breakUp ?: "")
        assertThat(stored).containsExactly(15, 30, 45, 0).inOrder()
    }

    @Test
    fun `insertData updates stress breakup when distribution changes but total sum stays same`() = runBlocking {
        val dao = FakeOreoStressDao()
        val source = OreoStressDataImpl(dao)

        source.insertData(
            OreoStressDataBreakup(
                breakUp = Gson().toJson(listOf(10, 20, 0, 0)),
                date = "2026-04-15"
            )
        )

        source.insertData(
            OreoStressDataBreakup(
                breakUp = Gson().toJson(listOf(20, 10, 0, 0)),
                date = "2026-04-15"
            )
        )

        val stored = Gson().fromJsonList(dao.stored?.breakUp ?: "")
        assertThat(stored).containsExactly(20, 10, 0, 0).inOrder()
    }

    private class FakeOreoStressDao : OreoStressDao {
        var stored: OreoStressDataBreakup? = null

        override fun insert(obj: OreoStressDataBreakup): Long {
            stored = obj.copy(id = 1)
            return 1L
        }

        override fun insert(vararg obj: OreoStressDataBreakup) {
            stored = obj.lastOrNull()?.copy(id = 1)
        }

        override fun insertAll(obj: List<OreoStressDataBreakup>): LongArray {
            stored = obj.lastOrNull()?.copy(id = 1)
            return longArrayOf()
        }

        override fun insertAllForRepeated(obj: List<OreoStressDataBreakup>): LongArray {
            stored = obj.lastOrNull()?.copy(id = 1)
            return longArrayOf()
        }

        override fun update(obj: OreoStressDataBreakup) {
            stored = obj
        }

        override fun delete(obj: OreoStressDataBreakup) {
            if (stored?.id == obj.id) {
                stored = null
            }
        }

        override fun getTodayData(date: String): OreoStressDataBreakup? {
            return stored?.takeIf { it.date == date }
        }

        override fun updateViaDate(breakUp: String, date: String, is_synced: Boolean) {
            stored = stored?.copy(breakUp = breakUp, date = date, isSynced = is_synced)
        }

        override fun getServerUnSyncData(is_synced: Boolean): List<OreoStressDataBreakup>? {
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
