package com.noisefit.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentProfileEditBinding
import com.noisefit.ui.common.bottomSheet.DATE_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.friends.profile.InterestSharedViewModel
import com.noisefit.ui.profile.BottomSheetImagePicker.Companion.IMAGE_PICKER_RESULT
import com.noisefit_commans.data.model.User
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.UserLocation
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadProfileEditImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event

import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventAttributes
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

const val SAVE_LOCATION_REQUEST_KEY = "SAVE_LOCATION_REQUEST_KEY"
const val CLOSED_SEARCH_STATE_KEY = "CLOSED_SEARCH_STATE_KEY"
const val INTEREST_UPDATE_KEY = "INTEREST_UPDATE_KEY"

@AndroidEntryPoint
class ProfileEditFragment :
    BaseFragment<FragmentProfileEditBinding>(FragmentProfileEditBinding::inflate) {

    private val viewModel: ProfileEditViewModel by viewModels()
    private val sharedViewModel: InterestSharedViewModel by activityViewModels()


    private val REQUEST_LAUNCH_LIBRARY = 384
    private val REQUEST_LAUNCH_IMAGE_CAPTURE = 664
    var outputUri: Uri? = null
    var photoURI: Uri? = null


    private fun setDeviceUnit() {
        binding.tvUnitValue.text = viewModel.getSelectedUnit()
    }

    private fun setGenderValue() {
        binding.tvGenderValue.text = viewModel.getGenderValue()
    }

    private fun setUserLocation() {
        binding.tvLocationValue.text = viewModel.getUserCity()
    }

    private fun setDob() {
        binding.tvDobValue.text = DateFormats.formatBirthDateDisplay(viewModel.getDob())
    }

    private fun setHeightValue() {
        binding.tvHeightValue.text = viewModel.getHeight()
    }

    private fun setWeightValue() {
        binding.tvWeightValue.text = viewModel.getWeight()
    }

    private fun setProfile() {
        binding.ivUserImage.loadProfileEditImage(
            requireContext(),
            viewModel.profileLink.value,
            R.drawable.ic_default_profile_image
        )
    }

    private fun openLocationBottom() {
        navigate(
            ProfileEditFragmentDirections.actionProfileEditFragmentToLocationBottomSheet().apply {
                this.shouldUpdate = false
            })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserLocation()
    }

    override fun initListener() {

        requireActivity().supportFragmentManager.setFragmentResultListener(
            CLOSED_SEARCH_STATE_KEY,
            this
        ) { _, bundle ->
            val isClosed = bundle.getBoolean("closed")
            if (isClosed) {
                openLocationBottom()
            }

        }



        binding.tvLocationValue.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResultListener(
                SAVE_LOCATION_REQUEST_KEY,
                this
            ) { _, bundle ->
                val isSaved = bundle.getBoolean("save")
                if (isSaved) {

                    setUserLocation()
                } else {
                    val cityId = bundle.getInt("cityId")
                    val stateId = bundle.getInt("stateId")
                    val cityName = bundle.getString("city")

                    viewModel.tempLocation = UserLocation(
                        cityId = cityId,
                        stateId = stateId,
                        city = cityName
                    )

                    setUserLocation()

                }

            }

            openLocationBottom()
        }
        binding.tvSave.setOnClickListener {
            if (viewModel.userName.value.isNullOrEmpty()) {
                context.showShortToast("First userName is required")
                return@setOnClickListener
            }
            /*if (viewModel.interests.value == -1) {
                context.showShortToast("EndGame is required")
                return@setOnClickListener
            }*/
            if (viewModel.gender.value.isNullOrEmpty()) {
                context.showShortToast("Gender is required")
                return@setOnClickListener
            }
            if (viewModel.heightInCm.value.isNullOrEmpty()) {
                context.showShortToast("Height is required")
                return@setOnClickListener
            }
            if (viewModel.weightInKg.value.isNullOrEmpty()) {
                context.showShortToast("Weight is required")
                return@setOnClickListener
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.user_profile_updated
            )

            viewModel.updateUserProfile()


        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvChangeNameLabel.setOnClickListener {
            navigate(ProfileEditFragmentDirections.actionProfileEditFragmentToBottomSheetImagePicker())
        }
        binding.ivUserImage.setOnClickListener {
            navigate(ProfileEditFragmentDirections.actionProfileEditFragmentToBottomSheetImagePicker())
        }
        binding.tvNameValue.setOnClickListener {
            setFragmentResultListener(NAME_REQUEST_KEY) { _, bundle ->
                val name = bundle.getString("name")
                if (!name.isNullOrEmpty()) {
                    viewModel.updateName(name)
                }

            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_name_edit_click)
            navigate(
                ProfileEditFragmentDirections.actionProfileEditFragmentToNameUpdateBottomSheet(
                    viewModel.userName.value
                )
            )
        }
        binding.tvGenderValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.setGender(it1)
                    setGenderValue()
                }

            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_gender_edit_click)
            navigate(
                ProfileEditFragmentDirections.actionProfileEditFragmentToValueSelectorBottomSheet(
                    viewModel.getGenderValue(),
                    AppStaticData.getGenderValues(requireContext()),
                    getString(R.string.gender)
                )
            )

        }
        binding.tvDobValue.setOnClickListener {
            setFragmentResultListener(DATE_REQUEST_KEY) { _, bundle ->
                val date = bundle.getInt("date")
                val month = bundle.getInt("month")
                val year = bundle.getInt("year")

                viewModel.setDob(year, month, date)
                setDob()
            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_dob_edit_click)
            navigate(
                ProfileEditFragmentDirections.actionProfileEditFragmentToDateBottomSheet(
                    getString(R.string.dob),
                    viewModel.dobDate,
                    viewModel.dobMonth,
                    viewModel.dobYear,
                    true
                )
            )

        }
        binding.tvHeightValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                try {
                    if (selectedValue != null) {
                        viewModel.setHeight(selectedValue.split(" ")[0])
                        setHeightValue()
                    }

                } catch (exp: Exception) {
                    exp.printStackTrace()

                }


            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_height_edit_click)
            navigate(
                ProfileEditFragmentDirections.actionProfileEditFragmentToValueSelectorBottomSheet(
                    viewModel.getHeight(),
                    viewModel.getHeightList().toTypedArray(),
                    getString(R.string.height)
                )
            )
        }
        binding.tvWeightValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                try {
                    if (selectedValue != null) {
                        viewModel.setWeight(selectedValue.split(" ")[0])
                        setWeightValue()
                    }

                } catch (exp: Exception) {
                    exp.printStackTrace()

                }


            }
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_profile_weight_edit_click)
            navigate(
                ProfileEditFragmentDirections.actionProfileEditFragmentToValueSelectorBottomSheet(
                    viewModel.getWeight(),
                    viewModel.getWeightList().toTypedArray(),
                    getString(R.string.weight)
                )
            )
        }


        binding.tvUnitValue.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.setSelectedUnit(it1)
                    setDeviceUnit()
                    if (bundle.getBoolean("isValueChanged")) {
                        viewModel.convertValues()
                    }
                }

            }
            navigate(
                ProfileEditFragmentDirections.actionProfileEditFragmentToValueSelectorBottomSheet(
                    viewModel.getSelectedUnit(),
                    viewModel.unitList.toTypedArray(),
                    getString(R.string.unit)
                )
            )

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedViewModel.selectedInterests = ArrayList()
    }


    private fun updateName() {
        binding.tvNameValue.text = viewModel.userName.value
    }


    private fun logProfileEvent(user: User) {


        viewModel.localDataStore.getUser()?.id?.let {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.PROFILE_UPDATE)
            HashMap<String, Any>().apply {
                this["UUID"] = it
            }
        }

        val gender: String =
            if (user.userInfo?.gender?.lowercase() == Gender.MALE.name.lowercase()) {
                "Male"
            } else if (user.userInfo?.gender?.lowercase() == Gender.FEMALE.name.lowercase()) {
                "Female"
            } else {
                "Other"
            }
        viewModel.sessionManager.addUserAttributeToMoEngage(true,
            HashMap<String, Any>().apply
            {
                this[MoEngageAppEventAttributes.name] = user.firstName.toString()
                this[MoEngageAppEventAttributes.gender] = gender
                this[MoEngageAppEventAttributes.age] = user.userInfo?.age ?: 0
                this[MoEngageAppEventAttributes.dob] = user.userInfo?.dob.toString()
                this[MoEngageAppEventAttributes.height] = user.userInfo?.height ?: 0
                this[MoEngageAppEventAttributes.weight] = user.userInfo?.weight ?: 0
            })
    }

    private fun updateWatchData() {
        val user = viewModel.localDataStore.getUser()
        if (user?.userInfo != null && user.userGoals != null) {
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetUserInfo(user.userInfo!!, user.userGoals!!, user.firstName)
            )
            val unit = DeviceUnits(
                unitSystem = user.userGoals!!.unitSystem
            )
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetDeviceUnits(unit)
            )
            logProfileEvent(user)
        }


        viewModel.sessionManager.reloadNotification(Event(true))
        navigateUpSafe()
    }

    override fun subscribeObservers() {
        setFragmentResultListener(IMAGE_PICKER_RESULT) { key, bundle ->
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

        viewModel.userDetailsUpdated.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                viewModel.googleFitDataObservers.saveUserWeightAndHeight()
                updateWatchData()
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


        viewModel.profileLink.observe(this) {
            it?.let {
                setProfile()
            }
        }
        viewModel.dobValue.observe(this) {
            if (it == true) {
                setDob()
            }
        }
        viewModel.userName.observe(this) {
            it?.let {
                updateName()
            }
        }
        viewModel.email.observe(this) {
            it?.let {
                binding.tvEmailValue.text = it
            }
        }
        viewModel.interests.observe(this) {
            it?.let {
//                updateInterests(it)
            }
        }
        viewModel.unit.observe(this) {
            it?.let {
                setDeviceUnit()
            }
        }

        viewModel.gender.observe(this) {
            it?.let {
                setGenderValue()
            }
        }
        viewModel.heightInCm.observe(this) {
            it?.let {
                setHeightValue()
            }
        }

        viewModel.weightInKg.observe(this) {
            it?.let {
                setWeightValue()
            }
        }
        viewModel.phoneNumber.observe(this) {
            if (it.isNullOrEmpty()) {
                binding.tvNumberLabel.gone()
                binding.tvNumberValue.gone()
                binding.include8.root.gone()
            } else {
                binding.tvNumberLabel.visible()
                binding.tvNumberValue.visible()
                binding.include8.root.visible()
                binding.tvNumberValue.text = it
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

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
                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(photoURI!!, outputUri!!)
                        .withAspectRatio(1f, 1f)
                        .withOptions(uCropOptions)
                        .withMaxResultSize(720, 720)
                        .start(requireActivity(), this)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, outputUri!!)
                        .withAspectRatio(1f, 1f)
                        .withOptions(uCropOptions)
                        .withMaxResultSize(720, 720)
                        .start(requireActivity(), this)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }

                    viewModel.profileLink.value = resultUri.toString()

                    /*binding.ivProfile.setImageURI(null)
                    binding.ivProfile.setImageURI(resultUri)*/
//                    binding.tvImageText.text = getString(R.string.text_change)
                    resultUri?.let {
                        viewModel.uploadUserImage(it)
                    }
                    val selectedImagePath = resultUri.toString()
                }
            }
        }
    }


}