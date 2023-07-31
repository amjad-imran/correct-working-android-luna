package com.noisefit.ui.reward.voucher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel

private const val ACTIVE_VOUCHER = "Active Vouchers"
private const val EXPIRE_VOUCHER = "Expired"

class VoucherSharedViewModel : BaseViewModel() {
    private val _tabListData = MutableLiveData<List<String>>()
    val tabListData: LiveData<List<String>> = _tabListData

    val actVoucherCount = MutableLiveData<Int>(0)
    val expVoucherCount = MutableLiveData<Int>(0)

    fun cleanViewModelData() {
        actVoucherCount.value = 0
        expVoucherCount.value = 0
        _tabListData.value = ArrayList()
    }

    fun setSelected() {
        val tempList = ArrayList<String>()
        val activeCount = actVoucherCount.value ?: 0
        val expiredCount = expVoucherCount.value ?: 0
        val actVoucherText = "$ACTIVE_VOUCHER ($activeCount)"
        val expVoucherText = "$EXPIRE_VOUCHER ($expiredCount)"
        tempList.addAll(
            arrayListOf(
                actVoucherText,
                expVoucherText
            )
        )
        _tabListData.value = tempList
    }

}