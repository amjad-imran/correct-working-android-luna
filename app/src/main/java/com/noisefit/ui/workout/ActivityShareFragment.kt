package com.noisefit.ui.workout

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.HandlerCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.databinding.FragmentActivityShareBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.workout.adapter.ActivityShareImageAdapter
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.ActivityConvertUtils
import com.noisefit_commans.utils.ImageUtil
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.ArrayList
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible

@AndroidEntryPoint
class ActivityShareFragment :
    BaseFragment<FragmentActivityShareBinding>(FragmentActivityShareBinding::inflate) {
    var photoURI: Uri? = null

    var shareBitmap: Bitmap? = null
    var shareUri: Uri? = null

    private var isAlreadyShared = false

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var dataUnitConverter: DataUnitConverter

    @Inject
    lateinit var watchesSDK: WatchesSDK


    private val viewModel: ActivityShareViewModel by viewModels()


    private val imageSliderAdapter: ActivityShareImageAdapter by lazy {
        ActivityShareImageAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = arguments?.let { ActivityDetailsFragmentArgs.fromBundle(it).activity }


        activity?.let {
            initUi(it)
        }

        //setImageSlider()
    }

    private fun setImageSlider() {

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                shareBitmap?.recycle()
                shareBitmap = null
                shareUri = null
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }


    fun generateImagesData(): List<Pair<String?, Uri?>> {
        val returnValue = ArrayList<Pair<String?, Uri?>>()
        viewModel.imagesList.value?.forEach {
            returnValue.add(Pair(it, null))
        }
        if (photoURI != null) {
            returnValue.add(Pair("", photoURI))
        }
        return returnValue
    }


    private fun initUi(it: SportsModeResponse) {

        with( binding.layoutShare.lytTemplate){
            tvWorkoutName.text = it.getFormattedActivityName()
            tvCalorie.text = "${it.calories ?: 0}"

            val distance = dataUnitConverter.formatDistance(
                it.distance?.toInt() ?: 0,
                Units.METRIC
            )
            tvDistance.text = "$distance"
            val activityName = it.type ?: it.activityType
            imageView8.setImageResource(
                com.noisefit.util.ImageUtil().getImageFromActivity(activityName)
            )

            setAveragePace(it)
            setAverageSpeed(it)
            tvWorkoutTime.text = it.getActivityDurationFormat3()//"2h 30m 10s"


            tvTemperature.text = if (it.temp != null) "${it.temp}° C" else "_"
            tvHumidity.text = if (it.humidity != null) "${it.humidity}%" else "_"
            tvUvIndex.text = if (it.uvi != null) "${it.uvi}" else ""


            if (!it.end.isNullOrEmpty() && it.end != it.start) {
                tvStartLoc2.invisible()
                ivStartLocAsset.invisible()
                groupStartAndEnd.visible()
                tvStartLoc1.text = if (it.start.isNullOrEmpty()) "" else it.start
                tvEndLoc.text = it.end
            } else {
                tvStartLoc2.visible()
                if (it.start.isNullOrEmpty()) {
                    ivStartLocAsset.invisible()
                } else {
                    ivStartLocAsset.visible()
                }
                groupStartAndEnd.invisible()
                tvStartLoc2.text = if (it.start.isNullOrEmpty()) "" else it.start
            }


        }
    }

    private fun setAveragePace(act: SportsModeResponse) {
        var avgPaceUnit = ""
        var avgPace = ""
        val watches = watchesSDK.getWatchType()
        if (watches == SDKWatchType.SDK_NAV_PLUS || watches == SDKWatchType.SDK_ZH) {
            avgPace = ActivityConvertUtils.avgPaceUltra(
                Units.METRIC,
                act.distance,
                act.duration
            )
            avgPaceUnit = if (Units.METRIC == Units.METRIC) " /km" else " /miles"
        } else {
            if (act.distance != null && act.distance!! > 0) {
                if (localDataStore.getConnectedDevice()?.deviceType?.equals(DeviceType.COLORFIT_PULSE_2.deviceType) == true) {
                    avgPace = ActivityConvertUtils.avgPacePulse2(
                        Units.METRIC,
                        act.distance,
                        act.duration
                    )
                    avgPaceUnit = "/km"
                } else {
                    avgPace = ActivityConvertUtils.avgPace(
                        Units.METRIC,
                        act.distance,
                        act.duration
                    )
                    avgPaceUnit = "/km"
                }

            } else {
                avgPace = java.lang.String.format(
                    Locale.ENGLISH,
                    "%1$02d'%2$02d",
                    0,
                    0
                )
                avgPaceUnit = "/km"
            }

        }


        LOGS.d("PACEEEE $avgPace")
        binding.layoutShare.lytTemplate.tvPace.text = avgPace
        binding.layoutShare.lytTemplate.textView72.text = avgPaceUnit

    }

    private fun setAverageSpeed(act: SportsModeResponse) {
        val avgSpeed = ActivityConvertUtils.averageSpeed(
            Units.METRIC,
            act.distance,
            act.duration
        )
        binding.layoutShare.lytTemplate.tvAverageSpeed.text = avgSpeed
    }

    override fun initListener() {
        binding.backImage.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.SHARE_WORKOUT_IMAGE_CHANGE_CLICK)

            setFragmentResultListener(BottomSheetImagePicker.IMAGE_PICKER_RESULT) { key, bundle ->
                val value = bundle.getInt("selectedValue")
                if (value == 0) {
                    val libraryIntent = Intent(Intent.ACTION_PICK)
                    libraryIntent.type = "image/*"
                    startActivityForResult(
                        Intent.createChooser(libraryIntent, null),
                        REQUEST_LAUNCH_LIBRARY
                    )
                } else {
                    checkCameraPermission {
                        dispatchTakePictureIntent()
                    }
                }
            }
            navigate(ActivityShareFragmentDirections.actionActivityShareFragmentToBottomSheetImagePicker())
            /* checkCameraPermission {
                 dispatchTakePictureIntent()
             }*/
        }

        binding.backSave.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.SHARE_WORKOUT_SAVE_INSIDE_DEVICE_CLICK)
            saveToGallery()

        }

        binding.backWhatsapp.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.SHARE_WORKOUT_VIA_INSTAGRAM_CLICK)
            shareImageSocial(2)
        }
        binding.backMore.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.SHARE_WORKOUT_VIA_OTHERS_TOOLS_CLICK)
            shareImageSocial(0)
        }

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    /**
     * @param shareOn 0->Default 1->Facebook 2->Insta 3->Whatsapp
     */
    fun shareImageSocial(shareOn: Int) {
        if (!isAlreadyShared) {
            viewModel.earnRewardsPoints()
            localDataStore.setWorkoutShareCount(localDataStore.getWorkoutShareCount() + 1)
            isAlreadyShared = true
        }
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
        val view = binding.layoutShare.root
        executorService.execute {
            mainThreadHandler.post {
                binding.progressBar.root.visible()
            }
            if (shareBitmap == null) {
                shareBitmap = ImageUtil.getBitmapFromView(view)
            }
            shareBitmap?.let {
                if (shareUri == null) {
                    shareUri = ImageUtil.getTempImageUri(it, requireContext())
                }
                mainThreadHandler.post {
                    binding.progressBar.root.gone()

                    when (shareOn) {
                        0 -> {

                            ShareUtil.shareOthers(requireContext(), shareUri)
                        }
                        1 -> {

                            ShareUtil.shareOnFacebook(requireContext(), shareUri)
                        }
                        2 -> {

                            ShareUtil.shareOnInsta(requireContext(), shareUri)
                        }
                        3 -> {

                            ShareUtil.shareOnWhatsapp(requireContext(), shareUri)
                        }
                    }
                }
            }
            mainThreadHandler.post {
                binding.progressBar.root.gone()
            }
        }
    }


    private fun saveToGallery() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())

        val view = binding.layoutShare.root

        executorService.execute {
            mainThreadHandler.post {
                binding.progressBar.root.visible()
            }
            if (shareBitmap == null) {
                shareBitmap = ImageUtil.getBitmapFromView(view)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkStoragePermission {
                    saveImage()
                }
            } else {
                saveImage()
            }

        }
    }

    private fun saveImage() {
        shareBitmap?.let {
            ImageUtil.saveMediaToStorage(requireContext(), it)


        }
        activity?.runOnUiThread {
            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            binding.progressBar.root.gone()
        }
//        mainThreadHandler.post {

//        }
    }

    private fun checkStoragePermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            storagePermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val storagePermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            saveImage()
        } else {
            context.showShortToast("Permission Required")
        }
    }


    override fun subscribeObservers() {

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        shareBitmap = null
        shareUri = null
    }

    private fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            cameraPermissionResult.launch(Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            dispatchTakePictureIntent()
        } else {
            context.showShortToast("Permission Required")
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_LAUNCH_IMAGE_CAPTURE)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {

            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (photoURI == null) return
                    shareBitmap?.recycle()
                    shareBitmap = null
                    shareUri = null


                    binding.layoutShare.ivBack.destroyDrawingCache()
                    binding.layoutShare.ivBack.setImageURI(null)
                    binding.layoutShare.ivBack.setImageURI(photoURI)

                    //loadImages()
                }
                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return
                    shareBitmap?.recycle()
                    shareBitmap = null
                    shareUri = null
                    photoURI = selectedImageUri

                    binding.layoutShare.ivBack.destroyDrawingCache()
                    binding.layoutShare.ivBack.setImageURI(null)
                    binding.layoutShare.ivBack.setImageURI(photoURI)
                    //loadImages()
                }
            }
        }
    }

}