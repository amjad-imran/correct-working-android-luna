package com.noisefit.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.noisefit.data.DataFactory
import com.noisefit.data.local.FakeIOfflineApiResponseSourceImpl
import com.noisefit.data.local.FakeKeyValueDataSourceImpl
import com.noisefit.data.local.FakeLastSyncStoreSourceImpl
import com.noisefit.data.local.dataStored.abstraction.ILastSyncStore
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.remote.FakeNetworkDataSourceImpl
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.repository.LastSyncProvider
import org.mockito.Mockito.mock


class DependencyContainer {

    private val context = mock(Context::class.java)
    lateinit var dataFactory: DataFactory
    lateinit var networkService: NetworkService
    lateinit var iOfflineApiResponseStore: IOfflineApiResponseStore
    lateinit var lastSyncProvider: LastSyncProvider
    lateinit var keyValueDataSource: KeyValueDataSource
    lateinit var iLastSyncStore: ILastSyncStore

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            dataFactory = DataFactory(classLoader)
        }



        val sharedPref = mock(SharedPreferences::class.java)
        //db = mock(DataBase::class.java)
        iLastSyncStore = FakeLastSyncStoreSourceImpl()
        networkService =
            FakeNetworkDataSourceImpl(dataFactory.produceHmOfChallenges(dataFactory.produceListOfChallenges().data!!))
        lastSyncProvider = LastSyncProvider(iLastSyncStore)
        keyValueDataSource = FakeKeyValueDataSourceImpl()
        iOfflineApiResponseStore = FakeIOfflineApiResponseSourceImpl()
    }
}