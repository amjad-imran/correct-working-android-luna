package com.noisefit.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetShopProductSortBinding
import com.noisefit_commans.ui.BaseBottomSheet


const val SHOP_FILTER_KEY = "SHOP_FILTER_KEY"

class BottomSheetShopProductSort :
    BaseBottomSheet<BottomSheetShopProductSortBinding>(BottomSheetShopProductSortBinding::inflate) {

    private val radioButtons: ArrayList<RadioButton> by lazy {
        arrayListOf(
            binding.rb1, binding.rb2, binding.rb3, binding.rb4, binding.rb5, binding.rb6
        )
    }
    private val buttonText = arrayListOf(
        SortModes.A_TO_Z, SortModes.Z_TO_A,
        SortModes.PRICE_LTH, SortModes.PRICE_HTL,
        SortModes.L_TO_O, SortModes.O_TO_L,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedSortMode = arguments?.let {
            BottomSheetShopProductSortArgs.fromBundle(it).selectedSort
        }
        setRadioButton(selectedSortMode)

        binding.tvSave.setOnClickListener {
            val selectedMode = when(binding.rgMain.checkedRadioButtonId){
                R.id.rb1->SortModes.A_TO_Z
                R.id.rb2->SortModes.Z_TO_A
                R.id.rb3->SortModes.PRICE_LTH
                R.id.rb4->SortModes.PRICE_HTL
                R.id.rb5->SortModes.L_TO_O
                R.id.rb6->SortModes.O_TO_L
                else -> SortModes.A_TO_Z
            }
            setFragmentResult(
                SHOP_FILTER_KEY,
                bundleOf("selectedMode" to selectedMode)
            )
            navigateUpSafe()
        }
    }

    private fun setRadioButton(selectedSortMode: SortModes?) {
        radioButtons.forEachIndexed { index, radioButton ->
            radioButton.text = buttonText[index].title
        }
        if (selectedSortMode == null) {
            radioButtons[0].isChecked = true
        } else {
            radioButtons[buttonText.indexOf(selectedSortMode)].isChecked = true
        }
    }
}

enum class SortModes(val title: String,val sortKey:String) {
    A_TO_Z("Title - A to Z","atoz"),
    Z_TO_A("Title - Z to A","ztoa"),
    PRICE_LTH("Price - Low To High","lowtohigh"),
    PRICE_HTL("Price - High To Low","hightolow"),
    L_TO_O("Latest To Oldest","newest"),
    O_TO_L("Oldest To Latest","oldest")
}