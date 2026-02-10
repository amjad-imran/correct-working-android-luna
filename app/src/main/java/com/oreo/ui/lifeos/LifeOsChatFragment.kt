package com.oreo.ui.lifeos

import android.Manifest
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AiHeaderInsight1
import com.noisefit.data.model.AiMeals
import com.noisefit.data.model.AiWorkout
import com.oreo.ui.chatGpt.ChatGptAdapter
import com.oreo.data.model.ChatGptOverview
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsChatBinding
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ATTACHMENT_KEY
import com.oreo.ui.chatGpt.ChatClickListener
import com.oreo.ui.chatGpt.ChatGptViewModel
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.chatGpt.SuggestionAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.noisefit.luna.BuildConfig
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.ui.hideKeyboard
import com.noisefit_commans.ui.scrollToBottom
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.lifeos.insightsLvl1.HelpUsImproveBottomSheet

@AndroidEntryPoint
class LifeOsChatFragment :
    BaseFragment<FragmentLifeOsChatBinding>(FragmentLifeOsChatBinding::inflate) {

    private var previousSoftInputMode: Int? = null
    private val viewModel: ChatGptViewModel by viewModels()
    private val mAdapter: ChatGptAdapter by lazy { ChatGptAdapter() }
    private val args: LifeOsChatFragmentArgs by navArgs()

    // Track last reviewed message to enforce one-time like/dislike
    private var lastReviewedMessageId: UUID? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var pickDocumentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>


    private val suggestionsAdapter: SuggestionAdapter by lazy {
        SuggestionAdapter(binding.rvChats) { ques ->
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_suggested_q_clicked,
                hashMapOf(
                    "source" to getSrcEventNameFromSrcKey(),
                    "question" to ques
                )
            )
            sendMessage(ques)
        }
    }


    companion object {
        fun getStartData(
            threadId: String?,
            userMessage: String?,
            title: String?,
            aiTopic: AITopics,
            meal: AiMeals? = null,
            workout: AiWorkout? = null,
            headerInsight1: AiHeaderInsight1? = null,
            planType: PlanType? = null,
            srcKey: String? = null,
            displayAddAttachmentBS: Boolean? = false,
        ): Pair<Int, Bundle?> {
            return Pair(R.id.lifeOsChatFragment, Bundle().apply {
                putString("threadId", threadId ?: "")
                putString("userMessage", userMessage ?: "")
                putString("title", title ?: "")
                putSerializable("aiTopic", aiTopic)
                putSerializable("planType", planType ?: PlanType.NONE)
                putParcelable("meal", meal)
                putParcelable("workout", workout)
                putParcelable("headerInsight1", headerInsight1)
                putString("sourceKey", srcKey)
                putBoolean("displayAddAttachmentBS", displayAddAttachmentBS == true)
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = requireActivity().window
        if (previousSoftInputMode == null) previousSoftInputMode = window.attributes.softInputMode
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = binding.lytChatBox.chatEtx
        if(args.displayAddAttachmentBS){
            binding.lytChatBox.ivAddAttachment.performClick()
        }else{
            editText.requestFocus()
        }

        editText.setHint(getString(R.string.text_ask_anything))

        viewModel.threadId = args.threadId
        viewModel.userMessage = args.userMessage
        viewModel.meal = args.meal
        viewModel.workout = args.workout
        viewModel.headerInsight1 = args.headerInsight1
        viewModel.planType = args.planType
        viewModel.srcKey = args.sourceKey

        viewModel.getAiTopQuestions(args.aiTopic)

        setActionButtonState()

        //setupImeAnimation()
        setSuggestedQuestionsRecycler()

        registerAttachmentPickers()
        binding.rvChats.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        if (viewModel.planType == PlanType.NONE) {
            if (viewModel.threadId.isNullOrEmpty()) {
                if(viewModel.headerInsight1==null){
                    viewModel.generateThreadId()
                }else{
                    viewModel.generateThreadId {
                        binding.ivLogo.gone()
                        if(viewModel.headerInsight1!!.headerText!=null){
                            sendInsight1HeaderMessage(viewModel.headerInsight1!!)
                        }else{
                            viewModel.headerInsight1!!.mainText?.let { sendMessage(it) }
                        }
                    }
                }
            } else {
                viewModel.loadMessagesByThreadId(viewModel.threadId!!)
                binding.ivLogo.gone()
                binding.ivLogoTop.visible()
            }
            binding.lytChatBox.ivAddAttachment.visible()
        } else {
            binding.lytChatBox.ivAddAttachment.gone()
            viewModel.generateInitMessage()
        }


        mAdapter.itemClickListener = object : ChatClickListener {
            override fun onCopyMessage(message: ChatGptOverview.ReceivedMessage) {
                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.lifeos_copy_button,
                    hashMapOf(
                        "source" to getSrcEventNameFromSrcKey()
                    )
                )
                val plain = markdownToPlainText(message.message)
                try {
                    plain.copyToClipBoard()
                } catch (_: Exception) {
                }
            }

            override fun onLikeMessage(message: ChatGptOverview.ReceivedMessage) {
                val isAlreadyLiked = mAdapter.checkRateState(message.id)
                if (isAlreadyLiked) return

                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.lifeos_response_feedback_clicked,
                    hashMapOf(
                        "feedback" to "positive"
                    )
                )

                mAdapter.markLiked(message.id)
                viewModel.postChatReview(message.message, 1, message.id)
            }

            override fun onDislikeMessage(message: ChatGptOverview.ReceivedMessage) {
                val isAlreadyLiked = mAdapter.checkRateState(message.id)
                if (isAlreadyLiked) return

                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.lifeos_response_feedback_clicked,
                    hashMapOf(
                        "feedback" to "negative"
                    )
                )

                displayHelpUsImproveBS(message)

            }

        }

        val showIme: () -> Unit = {
            ViewCompat.getWindowInsetsController(view)?.show(WindowInsetsCompat.Type.ime())
                ?: run {
                    requireContext().getSystemService<InputMethodManager>()?.showSoftInput(
                        editText,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
        }

        view.post {
            showIme()
            //kickstartImeTranslation()
        }
    }

    private fun displayHelpUsImproveBS(message: ChatGptOverview.ReceivedMessage) {
        binding.lytChatBox.chatEtx.clearFocus()
        binding.lytChatBox.chatEtx.hideKeyboard()
        setFragmentResultListener(HelpUsImproveBottomSheet.HELP_US_IMPROVE_BS_INSIGHTS){ _, bundle ->
            val feedbackText = bundle.getString("feedbackText")
            val reasons = bundle.getString("reasons")

            mAdapter.markDisliked(message.id)
            viewModel.postChatReview(
                message.message,
                0,
                message.id,
                feedbackText,
                reasons
            )
        }
        navigate(
            R.id.helpUsImproveBottomSheet,
            Bundle().apply {
                putStringArrayList(
                    "reasons",
                    ArrayList<String>().apply {
                        this.add(getString(R.string.text_inaccurate))
                        this.add(getString(R.string.text_out_of_date))
                        this.add(getString(R.string.text_too_short))
                        this.add(getString(R.string.text_this_isn_t_helpful))
                    }
                )
            }
        )
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

    override fun initListener() {
        binding.ivBack.setOnClickListener {
            navigateUpSafe()
        }

        binding.ivScrollDown.setOnClickListener {
            binding.rvChats.scrollToBottom()
        }

        binding.btnRetry.setOnClickListener {
            if(!ApplicationUtils.isInternetConnected()){
                context.showShortToast(getString(R.string.text_check_your_internet_connection))
                return@setOnClickListener
            }
            viewModel.retryApi()
        }
        binding.ivNewChat.setOnClickListener {
            navigate(
                LifeOsChatFragmentDirections.actionLifeOsChatFragmentSelf(
                    "",
                    "",
                    args.aiTopic,
                    PlanType.NONE,
                )
            )
        }

        binding.ivHistory.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_history_view,
                hashMapOf(
                    "source" to getSrcEventNameFromSrcKey()
                )
            )
            navigateUpSafe()
            navigate(R.id.chatHistoryFragment)
        }

        binding.lytChatBox.chatEtx.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val message = v.text?.toString()?.trim().orEmpty()
                if (message.isNotEmpty()) sendMessage(message)
                true
            } else false
        }

        binding.lytChatBox.ivAddAttachment.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.luna_file_upload_plus
            )
            try {
                ViewCompat.getWindowInsetsController(requireView())
                    ?.hide(WindowInsetsCompat.Type.ime())
            } catch (_: Exception) {
            }
            binding.lytChatBox.chatEtx.clearFocus()
            hideKeyboard()
            //resetImePadding()
            //kickstartImeTranslation()
            setFragmentResultListener(ATTACHMENT_KEY) { _, bundle ->
                when (bundle.getString("type")) {
                    "camera" -> {
                        logChatPlusActionEvent(0)
                        launchCameraPicker()
                    }
                    "photo" -> {
                        logChatPlusActionEvent(2)
                        pickImageLauncher.launch("image/*")
                    }
                    "file" -> {
                        logChatPlusActionEvent(1)
                        pickDocumentLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    }
                }
            }
            navigate(R.id.bottomSheetAttachmentPicker)
        }

        binding.lytChatBox.chatEtx.addTextChangedListener(afterTextChanged = {
            setActionButtonState()
        })

        binding.lytChatBox.btnAction.setOnClickListener {
            if (viewModel.fetchInProgress.value == true) {
                viewModel.stopResponseGeneration()
                return@setOnClickListener
            }
            val text = binding.lytChatBox.chatEtx.text?.toString().orEmpty().trim()
            if (text.isEmpty() && viewModel.pendingAttachment == null) return@setOnClickListener
//                val (frag, bundle) = AudioAiFragment.getStartData(
//                    PlanType.NONE
//                )
//                navigateUpSafe()
//                navigate(frag, bundle)
//            } else {
            if(viewModel.pendingAttachment != null){
                viewModel.sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.lifeos_chat_media,
                    hashMapOf(
                        "source" to getSrcEventNameFromSrcKey()
                    )
                )
            }
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.lifeos_message_sent,
                hashMapOf(
                    "source" to getSrcEventNameFromSrcKey()
                )
            )
            val message = text.ifEmpty { getString(R.string.text_analyse_this) }
            sendMessage(message)
        }

        binding.lytChatBox.ivRemoveAttachment.setOnClickListener {
            viewModel.clearPendingAttachment()
        }
    }

    private fun logChatPlusActionEvent(key: Int){
        if(viewModel.srcKey==null){
            return
        }

        val eventName = when(key){
            0 -> MoEngageLunaAppEvents.luna_file_upload_camera
            1 -> MoEngageLunaAppEvents.luna_file_upload_file
            else -> MoEngageLunaAppEvents.luna_file_upload_image
        }
        viewModel.sessionManager.logMoEngageAppEvent(
            eventName,
            hashMapOf(
                "source" to viewModel.srcKey!!
            )
        )
    }

    override fun subscribeObservers() {
        viewModel.questions.observe(this) {
            suggestionsAdapter.setDataSet(it)
        }

        binding.rvChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                checkScrollState(recyclerView)
            }
        })

        viewModel.showRetry.observe(this) {
            if (it) {
                binding.btnRetry.visible()
            } else {
                binding.btnRetry.gone()
            }
        }
        viewModel.fetchInProgress.observe(this) {
            setActionButtonState()
        }

        viewModel.showSuggestedQuestions.observe(this) { show ->
            if (show) {
                binding.lytSuggestions.root.visible()

                /*setSuggestedQuestions(
                    arrayListOf(
                        "Teach me about my sleep score",
                        "Create a diet plan for me",
                        "Teach me about my sleep score jkdshf kjd gfkjsd fhk",
                        "Create a diet plan for me",
                        "Create a workout plan for me"
                    )
                )*/
                binding.ivLogo.visible()
                binding.ivLogoTop.gone()
            } else {
                binding.ivLogoTop.visible()
                binding.lytSuggestions.root.gone()
                binding.ivLogo.gone()
            }
        }


        viewModel.attachmentPreview.observe(this) { data ->
            //val enteredText = binding.lytChatBox.chatEtx.text?.toString().orEmpty()
            setActionButtonState()
            if (data == null) {
                binding.lytChatBox.lytAttachment.gone()
                return@observe
            }

            binding.lytChatBox.lytAttachment.visible()
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
            // layout will remeasure due to visibility change; no manual padding needed
        }

        viewModel.scrollToBottom.observe(this) {
            it.getContent()?.let {
                binding.rvChats.smoothScrollToPosition(
                    (binding.rvChats.adapter?.itemCount ?: 1) - 1
                )
            }
        }

        viewModel.chatGptOverview.observe(this) { list ->
            list?.let {
                mAdapter.setDataSet(it)
                /*binding.rvChats.post {
                    binding.rvChats.smoothScrollToPosition(mAdapter.itemCount - 1)
                }*/

                nullableBinding?.rvChats?.post {
                    nullableBinding?.rvChats?.let {
                        checkScrollState(it)
                    }
                }
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }


    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setActionButtonState() {
        val text = binding.lytChatBox.chatEtx.text?.toString().orEmpty()
        val hasAttachment = viewModel.pendingAttachment != null

        val isGenerating = viewModel.fetchInProgress.value ?: false

        if (isGenerating) {
            binding.lytChatBox.btnAction.setImageResource(R.drawable.ic_ai_stop)
            binding.lytChatBox.btnAction.alpha = 1f
            return
        }

        binding.lytChatBox.btnAction.setImageResource(R.drawable.image_ai_message_send_3)
        if (text.isNotEmpty() || hasAttachment) {
            binding.lytChatBox.btnAction.alpha = 1f
        } else {
            binding.lytChatBox.btnAction.alpha = 0.5f
        }
    }

    private fun setupImeAnimation() {
        val root = requireView()

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, /*sysBars.bottom +*/ imeBottom)
            insets
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            root,
            object :
                WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    root.setPadding(
                        root.paddingLeft,
                        root.paddingTop,
                        root.paddingRight,
                        /*sysBars.bottom +*/ imeBottom
                    )
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    val currentInsets = ViewCompat.getRootWindowInsets(root) ?: return
                    val sysBars = currentInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                    val imeBottom = currentInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    root.setPadding(
                        root.paddingLeft,
                        root.paddingTop,
                        root.paddingRight,
                        /*sysBars.bottom +*/ imeBottom
                    )
                }
            }
        )
    }

    private fun kickstartImeTranslation() {
        val root = view ?: return
        root.post { root.requestLayout() }
    }

    private fun resetImePadding() {
        val root = view ?: return
        val insets = ViewCompat.getRootWindowInsets(root) ?: return
        val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        root.setPadding(root.paddingLeft, root.paddingTop, root.paddingRight, sysBars.bottom)
    }


    private fun registerAttachmentPickers() {
        requestCameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    launchCameraPicker()
                } else {
                    val showRationale =
                        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    if (showRationale) {
                        context.showShortToast(getString(R.string.text_camera_permission))
                    } else {
                        showCameraPermissionSettingsDialog()
                    }
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                val uri = viewModel.cameraUri
                if (success && uri != null) {
                    handlePickedUri(uri, "image/jpeg")
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
                    handlePickedUri(
                        it,
                        viewModel.getMimeType(requireContext(), it) ?: "application/octet-stream"
                    )
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
                        object : com.noisefit_commans.data.BinaryActionCallback {
                            override fun yes() {
                                openAppSettings()
                            }

                            override fun no() {}
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
            return
        }
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val cacheDir = requireContext().externalCacheDir ?: requireContext().cacheDir
        val imageFile = File(cacheDir, fileName)
        imageFile.parentFile?.mkdirs()

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.noisefit.luna.fileprovider",
            imageFile
        )
        viewModel.cameraUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun handlePickedUri(uri: Uri, fallbackMime: String) {
        val mime = viewModel.getMimeType(requireContext(), uri) ?: fallbackMime
        val name = viewModel.getDisplayName(requireContext(), uri) ?: "file"
        val isImage = mime.startsWith("image/")
        val isSupportedDoc = mime == "application/pdf"

        if (!(isImage || isSupportedDoc)) {
            context.showShortToast(getString(R.string.text_unsupported_file_type))
            return
        }

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

    private fun sendInsight1HeaderMessage(message: AiHeaderInsight1) {
        hideKeyboard()
        viewModel.showSuggestedQuestions.value = false
        viewModel.addInsight1HeaderMsg(message)
        viewModel.addThinkingMessage()
        binding.lytChatBox.chatEtx.setText("")

        val formattedMsg = "${message.footerText}"
        viewModel.askQuestionStream(formattedMsg.replace("\n", ""))
    }

    private fun sendMessage(message: String) {
        hideKeyboard()
        viewModel.showSuggestedQuestions.value = false
        if (message.isNotEmpty()) {
            viewModel.addSentMessageWithPendingAttachment(message)
            viewModel.addThinkingMessage()
            binding.lytChatBox.chatEtx.setText("")
            viewModel.askQuestionStream(message.replace("\n", ""))
        }
    }

    private fun setSuggestedQuestionsRecycler() {
        binding.lytSuggestions.apply {
            rvSuggestions.layoutManager =
                LinearLayoutManager(rvSuggestions.context, LinearLayoutManager.HORIZONTAL, false)
            rvSuggestions.adapter = suggestionsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previousSoftInputMode?.let { requireActivity().window.setSoftInputMode(it) }
        previousSoftInputMode = null
    }

    private fun getLatestReceivedMessage(): ChatGptOverview.ReceivedMessage? {
        val list = viewModel.chatGptOverview.value ?: return null
        for (i in list.size - 1 downTo 0) {
            val item = list[i]
            if (item is ChatGptOverview.ReceivedMessage) return item
        }
        return null
    }

    private fun markdownToPlainText(markdown: String): String {
        var text = markdown
        // Remove code fences
        text = text.replace(Regex("""```[\n\r]?[\s\S]*?```"""), "")
        // Inline code
        text = text.replace(Regex("""`([^`]+)`"""), "$1")
        // Images ![alt](url) -> alt
        text = text.replace(Regex("""!\[([^\]]*)\]\(([^)]+)\)"""), "$1")
        // Links [text](url) -> text
        text = text.replace(Regex("""\[([^\]]+)\]\(([^)]+)\)"""), "$1")
        // Headings ###, ##, #
        text = text.replace(Regex("""^#{1,6}\s*""", RegexOption.MULTILINE), "")
        // Blockquotes
        text = text.replace(Regex("""\n>+\s?"""), "\n")
        // Bold/Italic
        text = text.replace(Regex("""[*_]{1,3}([^*_]+)[*_]{1,3}"""), "$1")
        // Unordered/ordered list markers
        text = text.replace(Regex("""^\s*[-*+]\s+""", RegexOption.MULTILINE), "• ")
        text = text.replace(Regex("""^\s*\d+\.\s+""", RegexOption.MULTILINE), "• ")
        // Tables: strip pipes
        text = text.replace(Regex("""^\|""", RegexOption.MULTILINE), "")
        text = text.replace("|", " ")
        // Extra whitespace
        text = text.replace(Regex("""\r"""), "")
        return text.trim()
    }


    private fun addCommonHeaders(builder: Request.Builder) {
        try {
            val token = viewModel.localDataStore.getUserToken()
            token?.let {
                builder.addHeader("access-token", "Bearer ${it.access_token}")
            }
        } catch (_: Exception) {
        }
        builder.addHeader("wearable-type", "ring")
        val tz = try {
            viewModel.localDataStore.getLastKnownTimezone()
        } catch (_: Exception) {
            null
        }
        builder.addHeader("timezone", tz ?: TimeZone.getDefault().id)
        val offset = try {
            viewModel.localDataStore.getLastKnownOffset()
        } catch (_: Exception) {
            null
        }
        val fallbackOffset = TimeZone.getDefault().rawOffset.toLong() / (60 * 1000)
        builder.addHeader("offset", (offset ?: fallbackOffset.toString()))
    }

    private fun jsonEscape(text: String): String {
        // returns quoted JSON string value, safe for inclusion
        val escaped = text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
        return "\"$escaped\""
    }

    private fun getSrcEventNameFromSrcKey(srcKey: String?=null): String{
        val src = srcKey ?: viewModel.srcKey
        return when(src){
            "lifeos" -> "LifeOS tab [new chat]"
            "sleep" -> "Sleep"
            "cycle_tracker" -> "Cycle Tracker"
            "readiness" -> "Readiness"
            "activity" -> "Activity"
            "circadian" -> "Circadian"
            else -> "Unknown"
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.lifeos_chat_opened,
            hashMapOf(
                "source" to getSrcEventNameFromSrcKey(args.sourceKey)
            )
        )
    }

}
