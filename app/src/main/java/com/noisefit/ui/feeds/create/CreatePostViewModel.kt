package com.noisefit.ui.feeds.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit.data.model.ImageTemplate
import com.noisefit.data.model.MentionUser
import com.noisefit.data.model.PostBackground
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.session.SessionManager
import com.noisefit.ui.feeds.create.styles.challenges.ChallengesStyle1Fragment
import com.noisefit.ui.feeds.create.styles.challenges.ChallengesStyle2Fragment
import com.noisefit.ui.feeds.create.styles.health.RingStyle1Fragment
import com.noisefit.ui.feeds.create.styles.health.RingStyle2Fragment
import com.noisefit.ui.feeds.create.styles.health.RingStyle3Fragment
import com.noisefit.ui.feeds.create.styles.workout.WorkoutCyclingStyle1Fragment
import com.noisefit.ui.feeds.create.styles.workout.WorkoutStyle1Fragment
import com.noisefit.ui.feeds.create.styles.workout.WorkoutStyle2Fragment
import com.noisefit.util.formatPostLeadingString
import com.noisefit.util.formatPostTrailingString
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject
constructor(
    private val feedRepository: FeedRepository,
    private val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {


    var searchText = MutableLiveData<String?>()
    var selectedWorkout: SportsModeResponse? = null
    var selectedChallenge: ChallengeModel? = null


    var selectedBackgroundPosition: Int = -1
    var selectedStylePosition: Int = -1

    var selectedAction: PostContent? = null
    var selectedContentAction: PostContentAction? = null

    var userSelectedBackImage = MutableLiveData<Uri?>()
    var hasSelectedBackImage = MutableLiveData<Boolean>(false)
    var hasContent = false

    var showImagePicker = MutableLiveData<Event<Boolean>>()
    val mappedUser = ArrayList<MentionUser>()


    private val _templateImages = MutableLiveData<List<ImageTemplate>>()
    val templateImages: LiveData<List<ImageTemplate>>
        get() = _templateImages

    private val _friendsList = MutableLiveData<List<MentionUser>>()
    val friendsList: LiveData<List<MentionUser>>
        get() = _friendsList

    val postSuccess = MutableLiveData<Event<Boolean>>()
    val uploadProgress = MutableLiveData<Event<Int>>()


    init {
        getImageTemplates()
    }


    fun getBackgroundImagesList(): List<PostBackground> {
        val list = ArrayList<PostBackground>()

        list.add(
            PostBackground(
                title = "Upload",
                resourceId = R.drawable.temp_icon_upload
            )
        )

        if (userSelectedBackImage.value != null) {
            list.add(
                PostBackground(
                    title = "Image",
                    imageUri = userSelectedBackImage.value.toString()
                )
            )
        }

        templateImages.value?.forEach {
            list.add(
                PostBackground(
                    title = it.title,
                    imageUrl = it.image_url
                )
            )

        }
        return list
    }

    fun getStyles(currentAction: PostContent): List<PostBackground> {
        val list = ArrayList<PostBackground>()

        when (currentAction) {
            PostContent.RINGS -> {
                list.add(
                    PostBackground(
                        isBackground = false,
                        title = "Style 1",
                        resourceId = R.drawable.image_ring_style1
                    )
                )

                list.add(
                    PostBackground(
                        isBackground = false,
                        title = "Style 2",
                        resourceId = R.drawable.image_ring_style2
                    )
                )

                list.add(
                    PostBackground(
                        isBackground = false,
                        title = "Style 3",
                        resourceId = R.drawable.image_ring_style3
                    )
                )
            }

            PostContent.WORKOUTS -> {
                if (selectedWorkout?.activityType.equals("outdoor_cycling__", true)) {
                    list.add(
                        PostBackground(
                            isBackground = false,
                            title = "Style 1",
                            resourceId = R.drawable.image_workout_cycle_style1
                        )
                    )
                } else {
                    list.add(
                        PostBackground(
                            isBackground = false,
                            title = "Style 1",
                            resourceId = R.drawable.image_workout_style1
                        )
                    )

                    list.add(
                        PostBackground(
                            isBackground = false,
                            title = "Style 2",
                            resourceId = R.drawable.image_workout_style2
                        )
                    )
                }

            }

            PostContent.CHALLENGES -> {
                list.add(
                    PostBackground(
                        isBackground = false,
                        title = "Style 1",
                        resourceId = R.drawable.image_challenge_style1
                    )
                )

                list.add(
                    PostBackground(
                        isBackground = false,
                        title = "Style 2",
                        resourceId = R.drawable.image_challenge_style2
                    )
                )
            }

            else -> {}
        }


        return list
    }

    fun getSelectedBackgroundPos(): Int {
        if (selectedBackgroundPosition != -1) return selectedBackgroundPosition
        return 1
    }

    fun getSelectedLayoutPos(): Int {
        if (selectedStylePosition != -1) return selectedStylePosition
        return 0
    }

    fun getStyleFragment(position: Int, postContent: PostContent?): Fragment? {
        if (postContent == null) return null

        return when (postContent) {
            PostContent.RINGS -> {
                when (position) {
                    0 -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.LAYOUT_STYLE1_CLICK)
                        RingStyle1Fragment()
                    }

                    1 -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.LAYOUT_STYLE2_CLICK)
                        RingStyle2Fragment()
                    }

                    2 -> {
                        RingStyle3Fragment()
                    }
                    else ->  RingStyle1Fragment()
                }
            }

            PostContent.WORKOUTS -> {
                if (selectedWorkout == null) return null
                if (selectedWorkout!!.activityType.equals("outdoor_cycling__", true)) {
                    when (position) {
                        0 -> WorkoutCyclingStyle1Fragment.newInstance(selectedWorkout!!)//WorkoutStyle1Fragment.newInstance(selectedWorkout!!)
                        else -> WorkoutCyclingStyle1Fragment.newInstance(selectedWorkout!!)
                    }
                } else {
                    when (position) {
                        0 -> WorkoutStyle1Fragment.newInstance(selectedWorkout!!)
                        1 -> WorkoutStyle2Fragment.newInstance(selectedWorkout!!)
                        else -> WorkoutStyle1Fragment.newInstance(selectedWorkout!!)
                    }
                }
            }

            PostContent.CHALLENGES -> {
                if (selectedChallenge == null) return null

                when (position) {
                    0 -> ChallengesStyle1Fragment.newInstance(selectedChallenge!!)
                    1 -> ChallengesStyle2Fragment.newInstance(selectedChallenge!!)
                    else -> ChallengesStyle1Fragment.newInstance(selectedChallenge!!)
                }
            }

            else -> return null
        }
    }

    fun getImageTemplates() {
        viewModelScope.launch {
            feedRepository.getImageTemplates().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getImageTemplates()
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _templateImages.postValue(it)
                        }
                    }
                }
            }
        }

    }

    private fun convertBitmapToFile(fileName: String, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(NoiseFitApplicationMain.context?.cacheDir, fileName)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun createPost(bitmap: Bitmap?, content: String) {
        viewModelScope.launch {
            feedRepository.removeOfflineFeedData()
            uploadProgress.value = Event(0)
            val file = if (bitmap != null) {
                convertBitmapToFile("post.jpeg", bitmap)
            } else {
                null
            }
            uploadProgress.value = Event(50)
            val leadingFormatting = content.formatPostLeadingString()
            val lengthDifference = content.length - leadingFormatting.length
            if (lengthDifference > 0) {
                mappedUser.forEach {
                    it.start_pos = (it.start_pos?.minus(lengthDifference))
                }
            }

            feedRepository.createPost(
                file?.toURI(),
                leadingFormatting.formatPostTrailingString(),
                mappedUser
            )
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }

                        is Resource.Loading -> {

                        }

                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        createPost(bitmap, content)
                                    }

                                    override fun no() {}
                                }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.let {
                                file?.deleteRecursively()
                                uploadProgress.value = Event(100)
                                postSuccess.postValue(Event(true))
                            }
                        }
                    }
                }
        }

    }

    fun getFriendsList(spanText: String): List<MentionUser> {
        val searchText = spanText.replace("@", "")
        if (searchText.isEmpty()) return ArrayList()
        return if (_friendsList.value == null) {
            getTagFriendsList()
            ArrayList()
        } else {
            _friendsList.value?.filter {
                (it.first_name ?: "").startsWith(searchText, true)
            } ?: ArrayList()
        }
    }

    fun getTagFriendsList() {
        viewModelScope.launch {
            feedRepository.getTagFriendsList().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getTagFriendsList()
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _friendsList.value = it
                            searchText.postValue(searchText.value)
                        }
                    }
                }
            }
        }

    }

    fun addTagMapping(user: MentionUser, start: Int) {
        mappedUser.add(
            MentionUser(
                id = user.id,
                first_name = user.first_name,
                start_pos = start
            )
        )
    }

    fun getUserImage(): String? {
        return localDataStore.getUser()?.imageUrl
    }

    fun getFeedPosCount(): Int {
        return localDataStore.getFeedPostCreateCount()
    }

    fun updateFeedPostCount(count:Int){
        localDataStore.setFeedPostCreateCount(count)
    }


}

enum class PostContent {
    UPLOAD, RINGS, WORKOUTS, CHALLENGES
}

enum class PostContentAction {
    BACKGROUND, LAYOUT
}