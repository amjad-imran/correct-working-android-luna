package com.noisefit.ui.onboarding

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseUpdateViewModel @Inject constructor(
    private val localDataStore: DataStoredInterface,
    private val userRepository: UserRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

    fun generateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                LOGS.d("Fetching FCM registration token failed, ${task.exception}")
                return@OnCompleteListener
            }
            val token = task.result
            LOGS.d("FCM TOKEN:: $token")
            updateFcmToken(token)
        })
    }

    fun updateFcmToken(token: String) {
        localDataStore.getUser() ?: return

        if (localDataStore.getFcmToken().equals(token, true)) {
            return
        }

        val userObject = JsonObject().apply {
            addProperty("push_token", token)
        }

        GlobalScope.launch {
            userRepository.updatePushToken(userObject).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        localDataStore.setFcmToken(token)
                    }
                    else -> {}
                }

            }
        }
    }

}