package com.noisefit_commans.data.model

data class Issue(val title: String, val data: ArrayList<IssueItem>)

data class IssueItem(val type: String?, val text: String?, val image: String?, val bold: Boolean?)