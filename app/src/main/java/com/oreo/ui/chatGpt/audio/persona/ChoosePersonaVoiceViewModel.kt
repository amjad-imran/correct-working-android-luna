package com.oreo.ui.chatGpt.audio.persona

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.chatGPT.voice.persona.ItemPersonaVoiceResponse
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoosePersonaVoiceViewModel @Inject constructor(
    private val oreoDeviceRepository: OreoDeviceRepository,
    private val ringDataStore: RingDataStore,
): BaseViewModel() {

    private val _personaData = MutableLiveData<List<ItemPersonaVoiceResponse>>()
    val personaData: LiveData<List<ItemPersonaVoiceResponse>> get() = _personaData

    fun loadPersonaData() {
        viewModelScope.launch {
             oreoDeviceRepository.getPersonaVoiceData().collect { resource ->
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
                                     override fun yes() { }
                                     override fun no() { }
                                 }
                         })
                     }
                     is Resource.Success -> {
                         resource.data?.data?.let {
                             _personaData.value = it
                         }
                     }
                 }
             }
        }
    }

    private fun createDummyData() {
        val jsonString = """
            {
              "data": [
                {
                  "id": 560,
                  "persona_type": "halo",
                  "persona_title": "Halo",
                  "persona_features": ["ascas", "aciacak", "csaklasa"],
                  "img_url": "aslcknacascn.com/img",
                  "voice_url": "aslcknacascn.com/voice1.mp3"
                },
                {
                  "id": 561,
                  "persona_type": "spartan",
                  "persona_title": "Spartan",
                  "persona_features": ["strength", "resilience", "tactical"],
                  "img_url": "spartanimg.com/img",
                  "voice_url": "spartanvoice.com/voice1.mp3"
                },
                {
                  "id": 562,
                  "persona_type": "covenant",
                  "persona_title": "Covenant",
                  "persona_features": ["elite", "precise", "combat"],
                  "img_url": "covenantimg.com/img",
                  "voice_url": "covenantvoice.com/voice1.mp3"
                },
                {
                  "id": 563,
                  "persona_type": "forerunner",
                  "persona_title": "Forerunner",
                  "persona_features": ["ancient", "mysterious", "advanced technology"],
                  "img_url": "forerunnerimg.com/img",
                  "voice_url": "forerunnervoice.com/voice1.mp3"
                },
                {
                  "id": 564,
                  "persona_type": "guardian",
                  "persona_title": "Guardian",
                  "persona_features": ["protector", "guardian", "unwavering"],
                  "img_url": "guardianimg.com/img",
                  "voice_url": "guardianvoice.com/voice1.mp3"
                }
              ]
            }
        """.trimIndent()

        // Parse the JSON and update LiveData with the dummy data
        /*val personaVoiceResponse = Gson().fromJson(jsonString, PersonaVoiceResponse::class.java)
        _personaData.value = personaVoiceResponse*/
    }

    fun saveUserPersona(persona: String){
        viewModelScope.launch(Dispatchers.IO) { ringDataStore.setUserSelectedPersona(persona) }
    }
}