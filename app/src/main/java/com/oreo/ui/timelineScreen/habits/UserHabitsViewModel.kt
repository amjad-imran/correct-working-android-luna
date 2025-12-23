package com.oreo.ui.timelineScreen.habits

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.HabitsByDateResponse
import com.noisefit.data.model.HabitsResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.usecases.GetAllHabitsUseCase
import com.oreo.data.usecases.GetHabitsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserHabitsViewModel @Inject constructor(
    private val habitsByDateUC: GetHabitsByDateUseCase,
    private val getAllHabitUC: GetAllHabitsUseCase
) : BaseViewModel() {
    private val _allHabitsState = MutableLiveData<HabitsResponse>()
    val allHabitsState: LiveData<HabitsResponse> get() = _allHabitsState

    private val _habitsByDateState = MutableLiveData<HabitsByDateResponse>()
    val habitsByDateState: LiveData<HabitsByDateResponse> get() = _habitsByDateState

    fun getHabitsByDate(context: Context, date: String) {
        viewModelScope.launch {
            habitsByDateUC.invoke(date).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        Toast.makeText(context, "All Habit API Loading", Toast.LENGTH_LONG).show()
                    }

                    is Resource.GenericError -> {
                        Toast.makeText(context, "All Habit API Gen Error", Toast.LENGTH_LONG).show()
                    }

                    is Resource.NetworkError -> {
                        Toast.makeText(context, "All Habit API Net Error", Toast.LENGTH_LONG).show()
                    }

                    is Resource.Success -> {
                        Toast.makeText(context, "All Habit API Success", Toast.LENGTH_LONG).show()
//                        resource.data?.data.let {
//                            _habitsByDateState.postValue(it)
//                        }
                    }
                }
            }
        }
    }

    fun getAllHabits(context: Context) {
        viewModelScope.launch {
            getAllHabitUC.invoke().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        Toast.makeText(context, "All Habit API Loading", Toast.LENGTH_LONG).show()
                    }

                    is Resource.GenericError -> {
                        println("Sahil: "+resource?.errorBody)
                    }

                    is Resource.NetworkError -> {
                        Toast.makeText(context, "All Habit API Net Error", Toast.LENGTH_LONG).show()
                    }

                    is Resource.Success -> {
                        Toast.makeText(context, "All Habit API Success", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}