package com.oreo.ui.chatGpt

import android.media.audiofx.Visualizer
import android.os.Build
import android.graphics.ImageDecoder
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.FileOutputStream
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AiHeaderInsight1
import com.noisefit.data.model.AiMeals
import com.noisefit.data.model.AiWorkout
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentChatGptBinding
import com.noisefit.ui.common.bottomSheet.DATE_REQUEST_KEY
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.revealFromBottom
import com.noisefit_commans.ui.scrollToBottom
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChatGptOverview
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue


@AndroidEntryPoint
class ChatGptFragment : BaseFragment<FragmentChatGptBinding>(FragmentChatGptBinding::inflate) {
    /**
     *   val (frag, bundle) = ChatGptFragment.getStartData(
     *                 null,
     *                 null,
     *                 null,
     *                 null,
     *                 AITopics.GENERAL
     *             )
     *             navigate(frag, bundle)
     */
    companion object {
        fun getStartData(
            threadId: String?,
            defaultMessage: String?,
            userMessage: String?,
            title: String?,
            aiTopic: AITopics,
            meal: AiMeals? = null,
            workout: AiWorkout? = null,
            headerInsight1: AiHeaderInsight1? = null,
            planType: PlanType? = null,
            attachmentUri: String? = null,
            attachmentMime: String? = null,
            attachmentName: String? = null,
            attachmentSize: Long? = null
        ): Pair<Int, Bundle?> {
            return Pair(R.id.chatGptFragment, Bundle().apply {
                putString("threadId", threadId ?: "")
                putString("defaultMessage", defaultMessage ?: "")
                putString("userMessage", userMessage ?: "")
                putString("title", title ?: "")
                putSerializable("aiTopic", aiTopic)
                putSerializable("planType", planType ?: PlanType.NONE)
                putParcelable("meal", meal)
                putParcelable("workout", workout)
                putParcelable("headerInsight1", headerInsight1)

                attachmentUri.let { putString("attachmentUri", it) }
                attachmentMime.let { putString("attachmentMime", it) }
                attachmentName.let { putString("attachmentName", it) }
                attachmentSize.let { putLong("attachmentSize", it?:-1) }
            })
        }
    }

    private val viewModel: ChatGptViewModel by viewModels()
    private val args: ChatGptFragmentArgs by navArgs()

    private val mAdapter: ChatGptAdapter by lazy {
        ChatGptAdapter()
    }

    private var cameraUri: Uri? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var pickDocumentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.threadId = args.threadId
        viewModel.defaultMessage = args.defaultMessage
        viewModel.userMessage = args.userMessage
        viewModel.meal = args.meal
        viewModel.workout = args.workout
        viewModel.headerInsight1 = args.insightHeader1
        viewModel.planType = args.planType

        try {
            val attUri = arguments?.getString("attachmentUri")
            val attMime = arguments?.getString("attachmentMime")
            val attName = arguments?.getString("attachmentName")
            val attSize = arguments?.getLong("attachmentSize", -1L) ?: -1L
            if (!attUri.isNullOrEmpty() && !attMime.isNullOrEmpty() && !attName.isNullOrEmpty() && attSize > 0) {
                viewModel.setPendingAttachment(Uri.parse(attUri), attMime, attName, attSize)
            }
        } catch (_: Exception) { }

        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_page_visit)
        setAdapter()

        if (viewModel.planType == PlanType.NONE) {
            if (viewModel.threadId.isNullOrEmpty()) {
                viewModel.generateThreadId()
            } else {
                viewModel.loadMessagesByThreadId(viewModel.threadId!!)
            }
            binding.lytChatBox.ivAddAttachment.visible()
        } else {
            binding.lytChatBox.ivAddAttachment.gone()
            viewModel.generateInitMessage()
        }

        if (viewModel.meal != null) {
            viewModel.meal?.meal_type?.let {
                setMealTitle(it)
            }
            setMealSuggestions(arrayListOf("Ques1", "Ques2", "Ques3", "Ques4", "Ques5"))
        }


        setVideo()

        registerAttachmentPickers()
    }

    private fun setMealSuggestions(suggestions: ArrayList<String>) {
        binding.lytSuggestions.apply {
            root.visible()
            this.rvSuggestions.layoutManager = LinearLayoutManager(
                this.rvSuggestions.context,
                LinearLayoutManager.HORIZONTAL, false
            )
            /*this.rvSuggestions.adapter = SuggestionAdapter(suggestions) {
                sendMessage(it)
            }*/
        }
    }

    private fun setMealTitle(title: String) {
        binding.imageView32.gone()
        binding.toolbarTitle.apply {
            visible()
            text = title
        }
        binding.ivClose.setImageResource(R.drawable.ic_back_ai)
    }

    private fun setVideo() {
        val fileName =
            ("android.resource://" + requireContext().packageName) + "/raw/video_ai_generating"
        val uri = Uri.parse(fileName)
        val videoView = binding.videoView
        videoView.setVideoURI(uri)
        videoView.pause()
    }

    private fun setAdapter() {
        with(binding.rvChats) {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

       /* mAdapter.itemClickListener = { item, position ->
            when (item) {
                is ChatGptOverview.SentMessage -> {

                }

                is ChatGptOverview.ReceivedMessage -> {

                }

                is ChatGptOverview.RetryMessage -> {
                    viewModel.retryApi()

                }

                is ChatGptOverview.ThinkingMessage -> {

                }

                is ChatGptOverview.HeaderMeal -> {}
                is ChatGptOverview.HeaderWorkout -> {}
            }
        }*/
    }

    override fun initListener() {

        binding.lytChatBox.chatEtx.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val message = v.text.toString().trim()
                if (message.isNotEmpty()) {
                    sendMessage(message)
                }
                true
            } else {
                false
            }
        }

        binding.lytChatBox.ivAddAttachment.setOnClickListener {
            setFragmentResultListener(ATTACHMENT_KEY) { _, bundle ->
                when (bundle.getString("type")) {
                    "camera" -> launchCameraPicker()
                    "photo" -> pickImageLauncher.launch("image/*")
                    "file" -> pickDocumentLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                }
            }
            navigate(R.id.bottomSheetAttachmentPicker)
        }


        binding.lytChatBox.btnSend.setOnClickListener {
            val text = binding.lytChatBox.chatEtx.text.toString()
            if (text.isEmpty().not() || viewModel.pendingAttachment != null) {
                val message = text.ifEmpty { getString(R.string.text_analyse_this) }
                sendMessage(message)
            }
        }

        binding.lytChatBox.btnAudioChat.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentToAudioAiFragment(null).apply {
                planType = PlanType.NONE
            })
        }


        binding.rvChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-1)) {
                    binding.imageGradientTop.gone()
                } else {
                    if (binding.imageGradientTop.visibility == View.GONE) {
                        binding.imageGradientTop.visible()
                    }
                }
                checkScrollState(recyclerView)
            }
        })

        binding.ivScrollDown.setOnClickListener {
            binding.rvChats.scrollToBottom()
        }

        binding.ivHistory.setOnClickListener {
            navigate(ChatGptFragmentDirections.actionChatGptFragmentToChatHistoryFragment())
        }

        binding.lytGeneratingData.ivStopGenerating.setOnClickListener {
            viewModel.stopResponseGeneration()
        }

        binding.lytSaveData.btnSave.setOnClickListener {
            if (viewModel.planType == PlanType.WORKOUT || viewModel.planType == PlanType.DIET) {
                val section = if (viewModel.planType == PlanType.WORKOUT) "workout"
                else "nutrition"
                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.lunaai_wid_selection,
                    HashMap<String, Any>().apply {
                        this["section"] = section
                        this["command"] = "save"
                    }
                )
            }
            viewModel.savePlanData()
        }

        binding.lytSaveData.btnCancel.setOnClickListener {
            if (viewModel.planType == PlanType.WORKOUT || viewModel.planType == PlanType.DIET) {
                val section = if (viewModel.planType == PlanType.WORKOUT) "workout"
                else "nutrition"
                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.lunaai_wid_selection,
                    HashMap<String, Any>().apply {
                        this["section"] = section
                        this["command"] = "not now"
                    }
                )
            }
            binding.lytSaveData.root.gone()
            binding.ivGeneratingGradient.gone()
            binding.videoView.stopPlayback()
            binding.videoView.gone()
        }

        /* binding.lytChatBox.btnNewChat.setOnClickListener {
             navigate(ChatGptFragmentDirections.actionChatGptFragmentSelf("",""))
         }*/

        /* binding.lytChatBox.btnSendMessage.setOnClickListener {
             if (viewModel.fetchInProgress.value == true) {
                 viewModel.stopResponseGeneration()
             } else {
                 if (binding.lytChatBox.chatEtx.text.isNullOrEmpty().not()) {
                     sendMessage(binding.lytChatBox.chatEtx.text.toString())
                 }
             }
         }*/

        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytChatBox.chatEtx.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage(binding.lytChatBox.chatEtx.text.toString())
                true
            } else false
        })

        binding.lytChatBox.chatEtx.addTextChangedListener(afterTextChanged = {
            setSendCtaStates(it.toString())
        })
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun setSendCtaStates(text: String) {
        if (text.isEmpty() && viewModel.pendingAttachment == null) {
            binding.lytChatBox.btnSend.gone()
            binding.lytChatBox.btnAudioChat.visible()
        } else {
            binding.lytChatBox.btnSend.visible()
            binding.lytChatBox.btnAudioChat.gone()
        }
    }

    fun checkScrollState(recyclerView: RecyclerView) {
        if (recyclerView.canScrollVertically(1)) {
            // Show the button when scrolling up and more content is available to scroll down
            binding.ivScrollDown.visibility = View.VISIBLE
        } else if (!recyclerView.canScrollVertically(1)) {
            // Hide the button when already at the bottom
            binding.ivScrollDown.visibility = View.GONE
        }
    }

    private fun setupVisualizer(audioSessionId: Int) {
        Visualizer(audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1] // Maximum capture size
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer,
                    waveform: ByteArray,
                    samplingRate: Int
                ) {
                    val amplitude = waveform.map { it.toInt().absoluteValue }.average().toFloat()
                    // binding.lytAudio.viewAudioVisualizer.updateRms(amplitude)
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer,
                    fft: ByteArray,
                    samplingRate: Int
                ) {
                    // Optional: FFT data for frequency visualization
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)
            enabled = true
        }
    }

    fun sendMessage(message: String) {
        hideKeyboard()
        if (binding.lytSuggestions.root.isVisible) {
            binding.lytSuggestions.root.gone()
        }
        if (message.isNotEmpty()) {
            viewModel.addSentMessageWithPendingAttachment(message)
            viewModel.addThinkingMessage()


            //viewModel.addReceivedMessage("", true)
            binding.lytChatBox.chatEtx.setText("")

            viewModel.askQuestionStream(message.replace("\n", ""))

            //viewModel.askQuestion(message)
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_ai_message_submit)
        }

    }

    private fun registerAttachmentPickers() {
        requestCameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    launchCameraPicker()
                } else {
                    val showRationale = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    if (showRationale) {
                        context.showShortToast(getString(R.string.text_camera_permission))
                    } else {
                        showCameraPermissionSettingsDialog()
                    }
                }
            }
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                val uri = cameraUri
                if (success && uri != null) {
                    handlePickedUri(uri, "image/jpeg")
                } else {
                    //context.showShortToast(getString(R.string.text_something_went_wrong_single))
                }
            }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val mime = viewModel.getMimeType(requireContext(), it) ?: "image/*"
                val isHeic = mime.equals("image/heic", true) || mime.equals("image/heif", true)
                if (isHeic) {
                    val converted = convertHeicToJpeg(it, viewModel.DEFAULT_IMAGE_QUALITY)
                    if (converted == null) {
                        context.showShortToast(getString(R.string.text_something_went_wrong_single))
                        return@let
                    }

                    val size = viewModel.getFileSize(requireContext(), converted)
                    if (size < 0L) {
                        context.showShortToast(getString(R.string.text_something_went_wrong_single))
                        return@let
                    }
                    if (size > viewModel.IMAGE_MAX_BYTES) {
                        context.showShortToast(getString(R.string.text_file_too_large_max_5_mb))
                        return@let
                    }

                    val originalName = viewModel.getDisplayName(requireContext(), it) ?: "image"
                    val jpgName = if (originalName.contains('.')) {
                        originalName.substringBeforeLast('.') + ".jpg"
                    } else {
                        "$originalName.jpg"
                    }

                    viewModel.setPendingAttachment(converted, "image/jpeg", jpgName, size)
                } else {
                    handlePickedUri(it, mime)
                }
            }
        }

        pickDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let {
                    handlePickedUri(it, viewModel.getMimeType(requireContext(),it) ?: "application/octet-stream")
                }
            }
    }

    private fun showCameraPermissionSettingsDialog() {
        try {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_camera_perssision_message),
                        false,
                        getString(R.string.text_go_to_settings),
                        object : BinaryActionCallback {
                            override fun yes() {
                                openAppSettings()
                            }

                            override fun no() {

                            }
                        }
                    )
                )
            )
        } catch (_: Exception) {
            context.showShortToast(getString(R.string.text_camera_permission))
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun launchCameraPicker() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
            return
        }
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val cacheDir = requireContext().externalCacheDir ?: requireContext().cacheDir
        val imageFile = java.io.File(cacheDir, fileName)
        imageFile.parentFile?.mkdirs()

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.noisefit.luna.fileprovider",
            imageFile
        )
        cameraUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun handlePickedUri(uri: Uri, fallbackMime: String) {
        val mime = viewModel.getMimeType(requireContext(), uri) ?: fallbackMime
        val name = viewModel.getDisplayName(requireContext(), uri) ?: "file"
        val isImage = mime.startsWith("image/")
        val isSupportedDoc = mime == "application/pdf" /*||
                mime == "application/msword" ||
                mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"*/

        if (!(isImage || isSupportedDoc)) {
            context.showShortToast(getString(R.string.text_unsupported_file_type))
            return
        }

        /*LOGS.d(
            "Image compressed original file size  ${
                viewModel.getFileSize(
                    requireContext(),
                    uri
                )
            }"
        )*/
        if (isImage) {
            val compressedUri = viewModel.compressImage(
                requireContext(),
                uri,
                quality = viewModel.DEFAULT_IMAGE_QUALITY
            )
            if (compressedUri == null) {
                context.showShortToast(getString(R.string.text_something_went_wrong_single))
                return
            }
            val compressedSize = viewModel.getFileSize(requireContext(), compressedUri)
            if (compressedSize < 0L) {
                context.showShortToast(getString(R.string.text_something_went_wrong_single))
                return
            }
            LOGS.d("Image compressed compressed image size  ${compressedSize}")
            if (compressedSize > viewModel.IMAGE_MAX_BYTES) {
                context.showShortToast(getString(R.string.text_file_too_large_max_5_mb))
                return
            }
            viewModel.setPendingAttachment(compressedUri, "image/jpeg", name, compressedSize)
            LOGS.d("Image compressed -> $compressedUri image/jpeg $name $compressedSize")
        } else {
            val size = viewModel.getFileSize(requireContext(), uri)
            if (size < 0L) {
                context.showShortToast(getString(R.string.text_something_went_wrong_single))
                return
            }
            if (size > viewModel.IMAGE_MAX_BYTES) {
                context.showShortToast(getString(R.string.text_file_too_large_max_5_mb))
                return
            }
            viewModel.setPendingAttachment(uri, mime, name, size)
            LOGS.d("Doc picked -> $uri $mime $name $size")
        }
    }

    private fun convertHeicToJpeg(sourceUri: Uri, quality: Int): Uri? {
        return try {
            val resolver = requireContext().contentResolver
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(resolver, sourceUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                resolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
            }

            if (bitmap == null) return null

            val outFile = File(requireContext().cacheDir, "heic_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(0, 100), fos)
            }
            bitmap.recycle()

            FileProvider.getUriForFile(
                requireContext(),
                "com.noisefit.luna.fileprovider",
                outFile
            )
        } catch (_: Exception) {
            null
        }
    }


    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onDestroyView() {
        super.onDestroyView()
        requireView().viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener)
    }

    private fun showSnackBar(text: String) {
        binding.lytSnackbar.apply {
            this.tvMessage.text = text
            this.root.visible()
            startSnackBarRemoveTimer()

            this.tvView.setOnClickListener {
                if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.MEAL) {
                    navigate(ChatGptFragmentDirections.actionChatGptFragmentToAiMealPlanFragment())
                } else if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.WORKOUT) {
                    navigate(
                        ChatGptFragmentDirections.actionChatGptFragmentToWorkoutPlansFragment()
                    )
                }
            }
        }
    }

    private fun startSnackBarRemoveTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.removeSnackBar()
        }, 10 * 1000L)
    }

    override fun onResume() {
        super.onResume()
        nullableBinding?.lytSnackbar?.root?.gone()
    }

    override fun subscribeObservers() {
        viewModel.attachmentPreview.observe(this) { data ->
            val enteredText = binding.lytChatBox.chatEtx.text.toString()
            setSendCtaStates(enteredText)
            if (data == null) {
                binding.lytChatBox.lytAttachment.gone()
                binding.lytChatBox.imageView47.setImageResource(R.drawable.back_chat_message_send)
                return@observe
            }

            binding.lytChatBox.lytAttachment.visible()
            binding.lytChatBox.imageView47.setImageResource(R.drawable.back_chat_message_send_expanded)
            val isImage = data.mimeType.startsWith("image/")
            if (isImage) {
                binding.lytChatBox.ivAttachmentImage.visible()
                binding.lytChatBox.lytAttachmentDoc.gone()
                try {
                    com.bumptech.glide.Glide.with(binding.root.context)
                        .load(data.uri)
                        .into(binding.lytChatBox.ivAttachmentImage)
                } catch (_: Exception) {
                }
            } else {
                binding.lytChatBox.ivAttachmentImage.gone()
                binding.lytChatBox.lytAttachmentDoc.visible()
                binding.lytChatBox.tvDocType.text =
                    if (data.mimeType == "application/pdf") "PDF" else "DOC"
                binding.lytChatBox.tvDocName.text = data.fileName
            }
        }

        binding.lytChatBox.ivRemoveAttachment.setOnClickListener {
            viewModel.clearPendingAttachment()
        }
        viewModel.videoState.observe(this) {
            if (it) {
                binding.ivGeneratingGradient.visible()
                binding.videoView.start()
                binding.videoView.visible()
            } else {
                binding.ivGeneratingGradient.gone()
                binding.videoView.stopPlayback()
                binding.videoView.gone()
            }
        }

        viewModel.removeSnackBar.observe(this) {
            it.getContent()?.let {
                nullableBinding?.lytSnackbar?.root?.gone()
            }
        }

        viewModel.aiGeneratedPlanSaved.observe(this) {
            binding.lytSaveData.root.gone()

            binding.ivGeneratingGradient.gone()
            binding.videoView.stopPlayback()
            binding.videoView.gone()

            if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.MEAL) {
                showSnackBar(getString(R.string.text_your_diet_plan_is_saved))
            } else if (viewModel.showSavePlan.value?.peekContent() == AiPlanType.WORKOUT) {
                showSnackBar(getString(R.string.text_your_workout_plan_is_saved))
            }
        }

        /* keyboardListener = ViewTreeObserver.OnGlobalLayoutListener {
             view?.let {
                 val insets = ViewCompat.getRootWindowInsets(it)
                 val isKeyboardVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())
                 if (viewModel.threadTitle.value.isNullOrEmpty().not()) {
                     if (isKeyboardVisible == true) {
                         binding.tvChatTitle.gone()
                     } else {
                         binding.tvChatTitle.visible()
                     }
                 }
             }
         }
         requireView().viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)*/

        viewModel.showSavePlan.observe(this) {
            it.getContent()?.let {
                when (it) {
                    AiPlanType.WORKOUT -> showSaveWorkoutPlan()
                    AiPlanType.MEAL -> showSaveMealPlan()
                }
            }
        }

        viewModel.threadTitle.observe(this) {
            binding.tvChatTitle.apply {
                visible()
                text = it
            }
        }

        /*binding.lytChatBox.chatEtx.doOnTextChanged { text, start, before, count ->
            if (viewModel.fetchInProgress.value == true) return@doOnTextChanged

             if (text.isNullOrEmpty()) {
                 binding.lytChatBox.btnSendMessage.setImageResource(0)
                 binding.vOverlay.gone()
             } else {
                 binding.lytChatBox.btnSendMessage.setImageResource(R.drawable.ic_ai_send_message)
                 binding.vOverlay.visible()
             }

        }*/

        viewModel.fetchInProgress.observe(this) {
            if (it) {
                binding.lytGeneratingData.root.visible()
                binding.lytChatBox.root.gone()
            } else {
                binding.lytGeneratingData.root.gone()
                binding.lytChatBox.root.visible()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.scrollToBottom.observe(this) {
            it.getContent()?.let {
                binding.rvChats.smoothScrollToPosition(mAdapter.getItemCount() - 1)
            }
        }

        viewModel.chatGptOverview.observe(this) {
            it?.let {
                if(it.filter { it is ChatGptOverview.ReceivedMessage }.size == 2){
                    viewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.homepage_luna_sent_double
                    )
                }
                //mAdapter.items = it
                mAdapter.setDataSet(it)
                nullableBinding?.rvChats?.post {
                    nullableBinding?.rvChats?.let {
                        checkScrollState(it)
                    }
                }
            }
        }
    }

    private fun showSaveWorkoutPlan() {
        viewModel.videoState.postValue(true)

        binding.lytSaveData.apply {
            testSaveQues.text = getString(R.string.text_would_you_like_to_save_this_workout_plan)
            root.revealFromBottom()
        }
        uiController.hideSoftKeyboard()
    }


    private fun showSaveMealPlan() {
        viewModel.videoState.postValue(true)

        binding.lytSaveData.apply {
            testSaveQues.text = getString(R.string.text_would_you_like_to_save_this_diet_plan)
            root.revealFromBottom()
        }
        uiController.hideSoftKeyboard()
    }

}

enum class AITopics {
    SLEEP, READINESS, ACTIVITY, STRESS, MENSTRUAL_HEALTH, WORKOUT, GENERAL, CIRCADIAN
}

enum class PlanType {
    WORKOUT, DIET, NONE
}
