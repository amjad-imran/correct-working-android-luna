package com.oreo.ui.helpsupport.questionaries

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHSQuestionariesResponseModel

class OHSQAViewModel : BaseViewModel() {
    fun getQAData(): ArrayList<OHSQuestionariesResponseModel> {
        val resultList = ArrayList<OHSQuestionariesResponseModel>()
        resultList.add(
            OHSQuestionariesResponseModel(
                id = "123",
                "Title1",
                "Why i am always",
                isExpendable = false
            )
        )
        resultList.add(
            OHSQuestionariesResponseModel(
                id = "123",
                "Title1",
                "Why i am always",
                isExpendable = false
            )
        )
        resultList.add(
            OHSQuestionariesResponseModel(
                id = "123",
                "Title1",
                "Why i am always",
                isExpendable = false
            )
        )
        resultList.add(
            OHSQuestionariesResponseModel(
                id = "123",
                "Title1",
                "Why i am always",
                isExpendable = false
            )
        )
        resultList.add(
            OHSQuestionariesResponseModel(
                id = "123",
                "Title1",
                "Why i am always",
                isExpendable = false
            )
        )
        resultList.add(
            OHSQuestionariesResponseModel(
                id = "123",
                "Title1",
                "Why i am always",
                isExpendable = false
            )
        )
        return resultList
    }
}