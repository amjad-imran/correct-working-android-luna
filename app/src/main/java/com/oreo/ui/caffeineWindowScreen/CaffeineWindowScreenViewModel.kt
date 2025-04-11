package com.oreo.ui.caffeineWindowScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaffeineWindowScreenViewModel @Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userRepository: UserRepository,
    private val localDataSource: DataStoredInterface
): BaseViewModel(){

    val rvDisplayAllItemsToggleState: MutableLiveData<Boolean> = MutableLiveData(true)

    private val _myItemsList = MutableLiveData<List<CaffeineFoodItem>>()
    val myItemsList: LiveData<List<CaffeineFoodItem>> get() = _myItemsList

    private val _allItemsList = MutableLiveData<List<CaffeineFoodItem>>()
    val allItemsList: LiveData<List<CaffeineFoodItem>> get() = _allItemsList

    fun loadItems() {
        viewModelScope.launch {
            val allItems = ArrayList<CaffeineFoodItem>()
            val myItems = ArrayList<CaffeineFoodItem>()
            val itemsList = getItemsFromAPI()
            for (item in  itemsList){
                if(item.is_favourite){
                    myItems.add(item)
                }else{
                    allItems.add(item)
                }
            }
            
            if (myItems.isNotEmpty()){
                _myItemsList.postValue(myItems)
            }
            if (allItems.isNotEmpty()){
                _allItemsList.postValue(allItems)
            }
        }
    }

    private fun getItemsFromAPI(): List<CaffeineFoodItem> {
        return listOf(
            CaffeineFoodItem(
                id = "askcc",
                name = "Diet coke can",
                quantity = 30,
                unit = "mg",
                is_favourite = false
            ),
            CaffeineFoodItem(
                id = "askcc",
                name = "Diet coke can",
                quantity = 32,
                unit = "mg",
                is_favourite = false
            ),
            CaffeineFoodItem(
                id = "askcc",
                name = "Diet coke can",
                quantity = 34,
                unit = "mg",
                is_favourite = true
            ),
            CaffeineFoodItem(
                id = "askcc",
                name = "Diet coke can",
                quantity = 50,
                unit = "mg",
                is_favourite = true
            ),

        )
    }

}