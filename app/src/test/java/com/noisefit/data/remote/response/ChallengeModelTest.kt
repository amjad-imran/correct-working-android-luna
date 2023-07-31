package com.noisefit.data.remote.response

import com.google.common.truth.Truth
import com.google.gson.Gson
import org.junit.Test


class ChallengeModelTest {

    companion object{
        val challengeString ="{ \"challenge_id\":90, \"image_url\":\"https://live.staticflickr.com/8135/30337805305_f804bb7750_b.jpg\", \"title\":\"challengecreatedbyapibyDiv8\", \"goal\":400, \"type\":\"calories\", \"start_date\":\"2022-11-15 18:30:00\", \"end_date\":\"2022-11-20 18:29:59\", \"status\":\"ongoing\", \"display_images\":[], \"participants\":0 }"
    }

    @Test
    fun `challenge display status started`() {
        val challengeModel = Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
            challengeString,
            com.noisefit_commans.data.response.ChallengeModel::class.java
        )

        Truth.assertThat(challengeModel.getChallengeDisplayStatus()).isEqualTo("Started")
    }


    @Test
    fun `challenge start in date test`() {
        val challengeModel = Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
            challengeString,
            com.noisefit_commans.data.response.ChallengeModel::class.java
        )
        challengeModel.currentTime = "2022-11-15 09:42:36"

        val startInDate =challengeModel.getStartsInData()

        Truth.assertThat(startInDate.first).isEqualTo("Tomorrow")
    }

    @Test
    fun `challenge start in date test2`() {
        val challengeString =
            "{ \"challenge_id\":90, \"image_url\":\"https://live.staticflickr.com/8135/30337805305_f804bb7750_b.jpg\", \"title\":\"challengecreatedbyapibyDiv8\", \"goal\":400, \"type\":\"calories\", \"start_date\":\"2022-11-16 18:30:00\", \"end_date\":\"2022-11-20 18:29:59\", \"status\":\"ongoing\", \"display_images\":[], \"participants\":0 }"
        val challengeModel = Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
            challengeString,
            com.noisefit_commans.data.response.ChallengeModel::class.java
        )
        challengeModel.currentTime = "2022-11-15 09:42:36"

        val startInDate =challengeModel.getStartsInData()

        Truth.assertThat(startInDate.first).isEqualTo("2")
    }

    @Test
    fun `challenge get history days`() {
        val challengeModel = Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
            challengeString,
            com.noisefit_commans.data.response.ChallengeModel::class.java
        )
        challengeModel.currentTime = "2022-11-15 09:42:36"

        val historyDays =challengeModel.getHistoryDays()

        Truth.assertThat(historyDays).hasSize(5)
    }

    @Test
    fun `challenge disqualified test for false`() {
        val challengeModel = Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
            challengeString,
            com.noisefit_commans.data.response.ChallengeModel::class.java
        )
        challengeModel.currentTime = "2022-11-15 09:42:36"

        val isDisqualified =challengeModel.isDisqualified()

        Truth.assertThat(isDisqualified).isFalse()
    }

    @Test
    fun `challenge disqualified test for true`() {
        val challengeString =
            "{ \"challenge_id\":90, \"image_url\":\"https://live.staticflickr.com/8135/30337805305_f804bb7750_b.jpg\"," +
                    " \"disqualified_msg\":\"Test Message\",\"title\":\"challengecreatedbyapibyDiv8\", \"goal\":400, \"type\":\"calories\", \"start_date\":\"2022-11-16 18:30:00\", \"end_date\":\"2022-11-20 18:29:59\", \"status\":\"ongoing\", \"display_images\":[], \"participants\":0 }"
        val challengeModel = Gson().fromJson<com.noisefit_commans.data.response.ChallengeModel>(
            challengeString,
            com.noisefit_commans.data.response.ChallengeModel::class.java
        )
        challengeModel.currentTime = "2022-11-15 09:42:36"

        val isDisqualified =challengeModel.isDisqualified()

        Truth.assertThat(isDisqualified).isTrue()
    }

}