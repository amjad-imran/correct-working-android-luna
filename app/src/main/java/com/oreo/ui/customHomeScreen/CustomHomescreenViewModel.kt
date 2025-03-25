package com.oreo.ui.customHomeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CustomHomescreenViewModel : ViewModel() {

    // Switch state
    val isListVisible = MutableLiveData(false)

    // List of items
    private val _items = MutableLiveData(mutableListOf("Item 1", "Item 2", "Item 3", "Item 4"))
    val items: LiveData<MutableList<String>> get() = _items

    // Swap items for drag-and-drop
    fun moveItem(fromPosition: Int, toPosition: Int) {
        _items.value?.let {
            val temp = it[fromPosition]
            it.removeAt(fromPosition)
            it.add(toPosition, temp)
            _items.value = it
        }
    }
}
