package com.oreo.ui.caffeineWindowScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
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

    val _myItemsList = MutableLiveData<ArrayList<CaffeineFoodItem>>()

    val _allItemsList = MutableLiveData<ArrayList<CaffeineFoodItem>>()

    private var itemsList = ArrayList<CaffeineFoodItem>()

    val dataUpdated= MutableLiveData<Event<Boolean>>()

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

    fun aisehi(){
        viewModelScope.launch {
            userRepository.getCaffeineWindowItemsList().collect{
                    resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            itemsList = it as ArrayList
                            dataUpdated.postValue(Event(true))
                        }
                    }
                }
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