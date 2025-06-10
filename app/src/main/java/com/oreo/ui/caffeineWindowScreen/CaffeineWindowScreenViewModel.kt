package com.oreo.ui.caffeineWindowScreen

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
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CaffeineFoodItem
import com.oreo.data.model.CaffeinePostApiModel
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

    val dataUpdated= MutableLiveData<Event<Boolean>>()

    var maxQuantity: Int = 0

    fun loadItems() {
        viewModelScope.launch {

            userRepository.getCaffeineWindowItemsList().collect{ resource ->
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
                            justLoadIt(it)
                            dataUpdated.postValue(Event(true))
                        }
                    }
                }
            }

        }
    }

    fun justLoadIt(mainList: List<CaffeineFoodItem>){
        val allItemsCanTake = ArrayList<CaffeineFoodItem>()
        val allItemsCanNotTake = ArrayList<CaffeineFoodItem>()

        val myItemsCanTake = ArrayList<CaffeineFoodItem>()
        val myItemsCanNotTake = ArrayList<CaffeineFoodItem>()
        LOGS.d("caffeineData: $mainList")
        mainList.forEach {
            if(it.is_favorite == true){
                if (it.quantity < maxQuantity){
                    myItemsCanTake.add(it)
                }else{
                    myItemsCanNotTake.add(it)
                }
//                myItems.add(it)
            }else{
                if (it.quantity < maxQuantity){
                    allItemsCanTake.add(it)
                }else{
                    allItemsCanNotTake.add(it)
                }
//                allItems.add(it)
            }
        }

        myItemsCanTake.sortBy { it.name }
        myItemsCanNotTake.sortBy { it.name }
        allItemsCanTake.sortBy { it.name }
        allItemsCanNotTake.sortBy { it.name }

        allItemsCanTake.addAll(allItemsCanNotTake)

        myItemsCanTake.addAll(myItemsCanNotTake)

        _myItemsList.postValue(myItemsCanTake)
        _allItemsList.postValue(allItemsCanTake)
    }

    fun hitPostApiToUpdateItems(postData: CaffeinePostApiModel){
        viewModelScope.launch {
            userRepository.updateCaffeineItemsList(postData).collect{ resource ->

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
                            /*localDataSource.setCustomHomeScreenApiCallTimeStamps()

                            localDataSource.setCustomHomeScreenItemsPriorityList(customHomeScreenData)
                            dataUpdated.postValue(Event(true))*/
                        }
                    }
                }

            }
        }
    }

    fun performAddOrDeleteAndSorting(
        curItem: CaffeineFoodItem,
        isAllItems: Boolean,
        isAdd: Boolean
    ){
        if(isAllItems){
            _allItemsList.value?.let {
                val canTake = ArrayList<CaffeineFoodItem>()
                val canNotTake = ArrayList<CaffeineFoodItem>()

                if (isAdd) it.add(curItem) else it.remove(curItem)
                it.sortedBy { it1 -> it1.name }.forEach { item ->
                    if(item.quantity < maxQuantity){
                        canTake.add(item)
                    }else{
                        canNotTake.add(item)
                    }
                }
                canTake.addAll(canNotTake)

                _allItemsList.postValue(canTake)
            }
        }else{
            _myItemsList.value?.let {
                val canTake = ArrayList<CaffeineFoodItem>()
                val canNotTake = ArrayList<CaffeineFoodItem>()

                if (isAdd) it.add(curItem) else it.remove(curItem)
                it.sortedBy { it1 -> it1.name }.forEach { item ->
                    if(item.quantity < maxQuantity){
                        canTake.add(item)
                    }else{
                        canNotTake.add(item)
                    }
                }

                canTake.addAll(canNotTake)
                _myItemsList.postValue(canTake)
            }
        }
    }

    /*    private fun getItemsFromAPI(): List<CaffeineFoodItem> {
            return listOf(
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 30.0,
                    unit = "mg",
                    is_favorite = false
                ),
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 32.0,
                    unit = "mg",
                    is_favorite = false
                ),
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 34.0,
                    unit = "mg",
                    is_favorite = true
                ),
                CaffeineFoodItem(
                    id = "askcc",
                    name = "Diet coke can",
                    quantity = 50.0,
                    unit = "mg",
                    is_favorite = true
                ),

            )
        }*/

}