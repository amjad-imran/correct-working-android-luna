package com.noisefit.ui.buddies

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.noisefit.data.repository.FakeBuddiesRepository
import com.noisefit.util.MainCoroutineRule
import com.noisefit.util.getOrAwaitValueTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BuddiesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineScope = MainCoroutineRule()

    private lateinit var viewModel: BuddiesViewModel

    @Before
    fun setup() {
        //viewModel = BuddiesViewModel(FakeBuddiesRepository())
    }


    @Test
    fun getFriendList() {
        viewModel.getFriendList()

        val result = viewModel.friendListResponse.getOrAwaitValueTest()
        assertThat(result.isNotEmpty())
    }
}