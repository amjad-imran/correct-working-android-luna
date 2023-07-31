package com.noisefit.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.ChallengeListingResponse
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.models.WatchFace
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class DataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfChallenges(): com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse> {
       return Gson().fromJson<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.ChallengeListingResponse>>(getFileFromName("challenge_list.json"))
    }



    fun produceHmOfChallenges(challengeList: com.noisefit_commans.data.response.ChallengeListingResponse): HashMap<Int, com.noisefit_commans.data.response.ChallengeModel> {
        val map = HashMap<Int, com.noisefit_commans.data.response.ChallengeModel>()
        challengeList.new?.forEach {
            map[it.challenge_id] = it
        }
        challengeList.completed?.forEach {
            map[it.challenge_id] = it
        }
        challengeList.joined?.forEach {
            map[it.challenge_id] = it
        }
        return map
    }

    private fun getFileFromName(fileName: String): String {
        return testClassLoader.getResource(fileName).readText()
    }
}