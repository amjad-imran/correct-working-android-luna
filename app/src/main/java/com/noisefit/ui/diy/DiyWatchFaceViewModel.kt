package com.noisefit.ui.diy

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.model.DiyBackground
import com.noisefit.data.model.DiyCustomWatchColor
import com.noisefit.data.model.DiyCustomWatchFaceBg
import com.noisefit.data.model.DiyCustomWatchType
import com.noisefit.data.model.DiyWatchFaceModal
import com.noisefit.data.model.diy.DiyCustomWatchFacesData
import com.noisefit.data.model.diy.DiyWatchFacePlacement
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.Event
import com.noisefit.util.FilterUtils
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class Filter(var type: String) {
    TEAL("Teal"), RUBY("Ruby"), LILAC("Lilac"), OCHRE("Ochre"), INDIGO("Indigo")
}

enum class FilterType(var type: String) {
    BACKGROUND("Background"), FILTER("Filter"), TEXT("Text"),
}

@HiltViewModel
class DiyWatchFaceViewModel
@Inject constructor(
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK,
    private val downloadRepository: DownloadRepository,
    val sessionManager: SessionManager,
    private val rewardsRepository: RewardsRepository,
    private val watchFaceRepository: WatchFaceRepository
) : BaseViewModel() {
    var pairingTimeTaken: Long = 0
    var currentPhotoPath: String? = null
    val REQUEST_LAUNCH_LIBRARY = 384
    val REQUEST_LAUNCH_IMAGE_CAPTURE = 664
    var outputUri: Uri? = null
    var photoURI: Uri? = null

    var updatePositionList = ArrayList<Int>()
    var minimumBatteryLevel = watchesSDK.getMinimumBatteryLevel()

    var currentDiyCustomWatchFaceBg = DiyCustomWatchFaceBg()

    var localFilePath: String? = null


    var tempHoldDiyCreation: DiyMyCreation? = null
    var alreadyCreatedDiyMyCreation: DiyMyCreation? = null

    var hasUserChangedSomething = false

    private val _filterType = MutableLiveData<FilterType>()
    val filterType: LiveData<FilterType> = _filterType

    private val _updateRv = MutableLiveData<Boolean>()
    val updateRv: LiveData<Boolean> = _updateRv


    private val _bgDiyImage = MutableLiveData<DiyBackground>()
    val bgDiyImage: LiveData<DiyBackground> = _bgDiyImage

    private val _setDiyCustomWatchFace =
        MutableLiveData<Event<Pair<DiyMyCreation, DiyCustomWatchFace>>>()
    val setDiyCustomWatchFace: LiveData<Event<Pair<DiyMyCreation, DiyCustomWatchFace>>> =
        _setDiyCustomWatchFace

    private val _watchFaceDownloadProgress = MutableLiveData<Event<Int>>()
    private val _setWatchFace = MutableLiveData<Event<Pair<File, DiyCustomWatchFace>>>()
    val setWatchFace: LiveData<Event<Pair<File, DiyCustomWatchFace>>> = _setWatchFace
    val downloadCancelled = MutableLiveData<Event<Boolean>>()
    val watchFaceDownloadProgress: LiveData<Event<Int>> = _watchFaceDownloadProgress
    var widthHeight: Pair<Int, Int> = watchesSDK.getWatchWidthHeight()
    val screenType = watchesSDK.getWatchForm()

    val colorHaxCodeList = ArrayList<String>()

    //
    var selectedBackgroundType: String = "Featured"
    var selectedImageType: String = ""
    var selectedFeatureImageName: String = ""
    var selectedFilterName: String = "None"
    var selectedFilterIntensityValue: Int = 0
    var selectedPlacement: String = ""
    var selectedTextStyle: String = "Default"
    var selectedTextColorCode: String = "#ffffff"


    var fontStylesMap: HashMap<String, List<DiyWatchFacePlacement>>? = null


    fun reset() {
        outputUri = null
        currentPhotoPath = null
        photoURI = null
    }


    val diyWatchFaceList = ArrayList<DiyWatchFaceModal>()

    init {
        getImageTemplates()
    }


    private fun feedInitialData(data: DiyCustomWatchFacesData) {
        val list = ArrayList<DiyWatchFaceModal>()

        var selectedColor: Int = 0
        val placementList = ArrayList<DiyCustomWatchFaceBg>()

        val placementDefaultSelectedIndex =
            if (alreadyCreatedDiyMyCreation != null && alreadyCreatedDiyMyCreation!!.textLayerName.isNotEmpty() && alreadyCreatedDiyMyCreation!!.textType.isNotEmpty()) {
                data.diyWatchFacePlacement.indexOfFirst { it.title.lowercase() == alreadyCreatedDiyMyCreation!!.textLayerName.lowercase() && it.font_style?.lowercase() == alreadyCreatedDiyMyCreation!!.textType.lowercase() }
            } else if (alreadyCreatedDiyMyCreation != null && alreadyCreatedDiyMyCreation!!.textLayerName.isNotEmpty()) {
                data.diyWatchFacePlacement.indexOfFirst { it.title.lowercase() == alreadyCreatedDiyMyCreation!!.textLayerName.lowercase() }
            } else {
                data.diyWatchFacePlacement.indexOfFirst { it.title.lowercase() == "top-centre" || it.title.lowercase() == "top-center" }
            }


        var placementSelectedLayer = data.diyWatchFacePlacement.firstOrNull()

        if (placementDefaultSelectedIndex != -1) {
            placementSelectedLayer = data.diyWatchFacePlacement[placementDefaultSelectedIndex]
            selectedPlacement = placementSelectedLayer.title
        }

        val defaultColorIntensity = if (alreadyCreatedDiyMyCreation != null) {
            alreadyCreatedDiyMyCreation!!.filterIntensity
        } else {
            50
        }
        selectedFilterIntensityValue = defaultColorIntensity
        val featureImageList = ArrayList<DiyCustomWatchFaceBg>()
        val myImageList = ArrayList<DiyCustomWatchFaceBg>()
        val colorList: ArrayList<DiyCustomWatchColor> = ArrayList()

        if (watchesSDK.getWatchType() != SDKWatchType.SDK_EVOLVE) {

            data.colour?.forEach { s ->
                colorList.add(
                    DiyCustomWatchColor(
                        Color.parseColor(s), false
                    )
                )
                colorHaxCodeList.add(s)
            }
            var index1 = -1
            if (alreadyCreatedDiyMyCreation != null && alreadyCreatedDiyMyCreation!!.colour.isNotEmpty()) {
                index1 = colorList.indexOfFirst {
                    it.color == Color.parseColor(alreadyCreatedDiyMyCreation!!.colour)
                }
            }

            if (index1 == -1) {
                index1 = 0
            }

            if (colorList.isNotEmpty()) {
                selectedColor = colorList[index1].color
                colorList[index1].isSelected = true
            } else {
                selectedColor = 0
            }

        }

        if (alreadyCreatedDiyMyCreation != null) {
            currentDiyCustomWatchFaceBg = DiyCustomWatchFaceBg(
                bgLink = alreadyCreatedDiyMyCreation!!.backgroundUrl,
                textLayerLink = placementSelectedLayer?.text_layer,
                name = "Image",
                color = selectedColor,
                binUrl = placementSelectedLayer?.bin_url,
                isSelected = true,
                textLayerName = placementSelectedLayer?.title,
                filterIntensity = alreadyCreatedDiyMyCreation!!.filterIntensity
            )

            myImageList.add(
                DiyCustomWatchFaceBg(
                    bgLink = alreadyCreatedDiyMyCreation!!.backgroundUrl,
                    textLayerLink = placementSelectedLayer?.text_layer,
                    name = "Image",
                    color = selectedColor,
                    binUrl = placementSelectedLayer?.bin_url,
                    isSelected = true,
                    filterIntensity = alreadyCreatedDiyMyCreation!!.filterIntensity
                )
            )
        }



        data.diyWatchFaceBackground.forEachIndexed { index, background ->

            if (index == 0) {
                myImageList.add(
                    DiyCustomWatchFaceBg(
                        bgLink = background.image_url,
                        textLayerLink = null,
                        name = "Upload",
                        isSelected = false
                    )
                )
            } else {
                if (index == 1 && currentDiyCustomWatchFaceBg.bgLink.isNullOrEmpty()) {

                    currentDiyCustomWatchFaceBg = DiyCustomWatchFaceBg(
                        bgLink = background.image_url,
                        textLayerLink = placementSelectedLayer?.text_layer,
                        name = background.title,
                        color = selectedColor,
                        binUrl = placementSelectedLayer?.bin_url,
                        isSelected = true,
                        textLayerName = placementSelectedLayer?.title
                    )
                    selectedFeatureImageName = background.title
                }

                featureImageList.add(
                    DiyCustomWatchFaceBg(
                        bgLink = background.image_url,
                        textLayerLink = placementSelectedLayer?.text_layer,
                        name = background.title,
                        color = selectedColor,
                        binUrl = placementSelectedLayer?.bin_url,
                        isSelected = index == 1 && alreadyCreatedDiyMyCreation == null
                    )
                )
            }
        }


        val tempSelectedDiyCustomWatchFaceBg = currentDiyCustomWatchFaceBg.bgLink
        _bgDiyImage.value = DiyBackground(
            currentDiyCustomWatchFaceBg,
            width = widthHeight.first,
            height = widthHeight.second,
            screenType = screenType
        )

        myImageList.reverse()

        list.add(
            DiyWatchFaceModal.BackgroundList(
                "My images", myImageList, screenType, FilterType.BACKGROUND
            )
        )

        list.add(
            DiyWatchFaceModal.FeaturedList(
                "Featured images", featureImageList, screenType, FilterType.BACKGROUND
            )
        )

        val filterList = ArrayList<DiyCustomWatchFaceBg>()
        filterList.add(
            DiyCustomWatchFaceBg(
                tempSelectedDiyCustomWatchFaceBg,
                placementSelectedLayer?.text_layer,
                "None",
                isSelected = false,
                colorMatrix = null
            )
        )
        filterList.add(
            DiyCustomWatchFaceBg(
                tempSelectedDiyCustomWatchFaceBg,
                placementSelectedLayer?.text_layer,
                Filter.TEAL.type,
                colorMatrix = FilterUtils().getFilter(Filter.TEAL.type, defaultColorIntensity)
            )
        )
        filterList.add(
            DiyCustomWatchFaceBg(
                tempSelectedDiyCustomWatchFaceBg,
                placementSelectedLayer?.text_layer,
                Filter.RUBY.type,
                colorMatrix = FilterUtils().getFilter(Filter.RUBY.type, defaultColorIntensity)
            )
        )
        filterList.add(
            DiyCustomWatchFaceBg(
                tempSelectedDiyCustomWatchFaceBg,
                placementSelectedLayer?.text_layer,
                Filter.LILAC.type,
                colorMatrix = FilterUtils().getFilter(Filter.LILAC.type, defaultColorIntensity)
            )
        )
        filterList.add(
            DiyCustomWatchFaceBg(
                tempSelectedDiyCustomWatchFaceBg,
                placementSelectedLayer?.text_layer,
                Filter.OCHRE.type,
                colorMatrix = FilterUtils().getFilter(Filter.OCHRE.type, defaultColorIntensity)
            )
        )
        filterList.add(
            DiyCustomWatchFaceBg(
                tempSelectedDiyCustomWatchFaceBg,
                placementSelectedLayer?.text_layer,
                Filter.INDIGO.type,
                colorMatrix = FilterUtils().getFilter(Filter.INDIGO.type, defaultColorIntensity)
            )
        )

        var index = -1
        if (alreadyCreatedDiyMyCreation != null && alreadyCreatedDiyMyCreation!!.filter.isNotEmpty()) {
            index = filterList.indexOfFirst {
                it.name.lowercase() == alreadyCreatedDiyMyCreation!!.filter.lowercase()
            }

        }

        if (index == -1) {
            index = 0
        } else {
            currentDiyCustomWatchFaceBg.colorMatrix = filterList[index].colorMatrix
        }

        filterList[index].isSelected = true
        list.add(
            DiyWatchFaceModal.FilterList(
                "Filters", filterList, screenType, defaultColorIntensity, FilterType.FILTER
            )
        )


        fontStylesMap = HashMap()
        val fontStyleList = ArrayList<DiyCustomWatchFaceBg>()

        val uniqueStyles = data.diyWatchFacePlacement.distinctBy { it.font_style }

        if (uniqueStyles.size <= 1) {

            data.diyWatchFacePlacement.forEachIndexed { _, data1 ->
                var selected = false
                if (data1.title.lowercase() == currentDiyCustomWatchFaceBg.textLayerName?.lowercase()) {
                    selected = true
                }
                placementList.add(
                    DiyCustomWatchFaceBg(
                        bgLink = tempSelectedDiyCustomWatchFaceBg,
                        textLayerLink = data1.text_layer,
                        name = data1.title,
                        isSelected = selected,
                        binUrl = data1.bin_url
                    )
                )
            }
        } else {
            var defaultStyle: String? = null
            uniqueStyles.forEachIndexed { _, styles ->

                val filteredData = data.diyWatchFacePlacement.filter {
                    it.font_style.equals(styles.font_style, true)
                }
                fontStylesMap!!.put(styles.font_style ?: "", filteredData)

            }

            data.diyWatchFacePlacement.forEachIndexed { _, styles ->

                if (styles.title.lowercase() == currentDiyCustomWatchFaceBg.textLayerName?.lowercase()) {

                    fontStyleList.add(
                        DiyCustomWatchFaceBg(
                            bgLink = styles.text_layer,
                            textLayerLink = null,
                            name = styles.font_style ?: "",
                            isSelected = false,
                            binUrl = styles.bin_url
                        )
                    )
                }
            }

            if (fontStyleList.isNotEmpty()) {
                var index = -1
                if (alreadyCreatedDiyMyCreation != null && alreadyCreatedDiyMyCreation!!.textType.isNotEmpty()) {
                    index = fontStyleList.indexOfFirst {
                        it.name.lowercase() == alreadyCreatedDiyMyCreation!!.textType.lowercase()
                    }
                }

                if (index == -1) {
                    index = 0
                }
                defaultStyle = fontStyleList[index].name
                fontStyleList[index].isSelected = true
            }


            val firstList = fontStylesMap!![defaultStyle]

            currentDiyCustomWatchFaceBg.fontStyleName = defaultStyle
            firstList?.forEachIndexed { _, data1 ->
                var selected = false
                if (data1.title.lowercase() == currentDiyCustomWatchFaceBg.textLayerName?.lowercase()) {
                    selected = true
                }
                placementList.add(
                    DiyCustomWatchFaceBg(
                        bgLink = tempSelectedDiyCustomWatchFaceBg,
                        textLayerLink = data1.text_layer,
                        name = data1.title,
                        isSelected = selected,
                        binUrl = data1.bin_url
                    )
                )
            }
        }


        list.add(
            DiyWatchFaceModal.PlacementList(
                "Placement", placementList, screenType, FilterType.TEXT
            )
        )

        if (fontStyleList.isNotEmpty()) {
            list.add(
                DiyWatchFaceModal.FontList(
                    "Text style",
                    ArrayList(sortTextStyleList(fontStyleList)),
                    screenType,
                    FilterType.TEXT
                )
            )
        }

        if (colorList.isNotEmpty()) {
            list.add(DiyWatchFaceModal.ColorList("Text colour", FilterType.TEXT, colorList))
        }


        diyWatchFaceList.addAll(list)
        setFilterType(FilterType.BACKGROUND)
    }

    fun setFilterType(filterType: FilterType) {
        _filterType.postValue(filterType)
    }

    fun setFilter(
        data: DiyBackground, bgImv: ShapeableImageView, link: String?, isTextLayer: Boolean
    ) {


        Glide.with(bgImv.context)
            .asBitmap()
            .load(link)
            .apply(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(data.width, data.height)
                    .dontTransform()
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {

                    if (!resource.isRecycled) {
                        val rawBitmap = Bitmap.createBitmap(resource)

                        LOGS.d("rawBitmap___ ${rawBitmap.width} ${rawBitmap.height} ${data.width} ${data.height}")
                        if (isTextLayer) {
                            data.textBitmap = rawBitmap
                            ApplicationUtils.loadImage(bgImv, data.textBitmap, data.screenType)
                        } else {


                            if (data.diyCustomWatchFaceBg?.colorMatrix != null) {

                                val paint = Paint()
                                paint.colorFilter =
                                    ColorMatrixColorFilter(data.diyCustomWatchFaceBg!!.colorMatrix!!)
                                val canvas = Canvas(rawBitmap)
                                canvas.drawBitmap(rawBitmap, 0f, 0f, paint)
                                data.bgBitmap = rawBitmap

                            } else {


                                data.bgBitmap = rawBitmap
                            }

                            ApplicationUtils.loadImage(bgImv, data.bgBitmap, data.screenType)

                        }
                    } else {
                        // The bitmap is recycled, so we need to load it again before displaying it
                        setFilter(data, bgImv, link, isTextLayer)
                    }


                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })


    }


    fun updateList(
        imageUri: String? = null,
        textLayer1: String? = null,
        textColor: Int? = null,
        binUrl: String? = null,
        filterRest: Boolean = false,
        colorIntensity: Int? = null,
        filterName: String? = null,
        isCustomImage: Boolean = false,
        selectedFontStyleChange: Boolean = false,
        resetMyImages: Boolean = false,
        resetFeaturedImage: Boolean = false
    ) {
        if (!imageUri.isNullOrEmpty()) {
            currentDiyCustomWatchFaceBg.bgLink = imageUri
        }
        if (!textLayer1.isNullOrEmpty()) {
            currentDiyCustomWatchFaceBg.textLayerLink = textLayer1
        }
        if (filterName != null) {
            currentDiyCustomWatchFaceBg.filterName = filterName
        }
        if (textColor != null) {
            currentDiyCustomWatchFaceBg.color = textColor
        }

        if (!binUrl.isNullOrEmpty()) {
            currentDiyCustomWatchFaceBg.binUrl = binUrl
        }
        if (colorIntensity != null) {
            currentDiyCustomWatchFaceBg.filterIntensity = colorIntensity
        }
        if (filterRest) {
            currentDiyCustomWatchFaceBg.filterName = null
            currentDiyCustomWatchFaceBg.colorMatrix = null
        }


        _bgDiyImage.postValue(_bgDiyImage.value?.apply {
            diyCustomWatchFaceBg = currentDiyCustomWatchFaceBg
            LOGS.d("intensitytttt ::: ${currentDiyCustomWatchFaceBg.filterIntensity}")
            currentDiyCustomWatchFaceBg.filterName?.let {
                currentDiyCustomWatchFaceBg.colorMatrix = FilterUtils().getFilter(
                    it, currentDiyCustomWatchFaceBg.filterIntensity
                )

                if (currentDiyCustomWatchFaceBg.colorMatrix != null) {
                    colorMatrix = currentDiyCustomWatchFaceBg.colorMatrix
                }

            }
        })


        diyWatchFaceList.forEach { diyWatchFaceModal ->
            when (diyWatchFaceModal) {

                is DiyWatchFaceModal.FeaturedList -> {
                    diyWatchFaceModal.list.forEach {
                        it.textLayerLink = currentDiyCustomWatchFaceBg.textLayerLink
                        it.bgLink = it.bgLink
                        if (resetFeaturedImage) {
                            it.isSelected = false
                        }
                        it.color = currentDiyCustomWatchFaceBg.color
                    }
                }

                is DiyWatchFaceModal.BackgroundList -> {
                    val count = diyWatchFaceModal.list.size
                    if (count > 10) {
                        diyWatchFaceModal.list.removeLast()
                    }
                    if (isCustomImage) {
                        diyWatchFaceModal.list.map { it.isSelected = false }
                        diyWatchFaceModal.list.add(
                            1, DiyCustomWatchFaceBg(
                                bgLink = currentDiyCustomWatchFaceBg.bgLink,
                                textLayerLink = currentDiyCustomWatchFaceBg.textLayerLink,
                                color = currentDiyCustomWatchFaceBg.color,
                                name = "Image $count",
                                isSelected = true,
                                isCustomImage = true
                            )
                        )
                    } else {
                        diyWatchFaceModal.list.forEach {
                            if (resetMyImages) {
                                it.isSelected = false
                            }
                        }

                    }


                }

                is DiyWatchFaceModal.FilterList -> {

                    if (currentDiyCustomWatchFaceBg.filterIntensity >= 0) {
                        diyWatchFaceModal.colorIntensity =
                            currentDiyCustomWatchFaceBg.filterIntensity
                    }
                    diyWatchFaceModal.list.forEach {
                        it.bgLink = currentDiyCustomWatchFaceBg.bgLink
                        it.textLayerLink = currentDiyCustomWatchFaceBg.textLayerLink
                        it.color = currentDiyCustomWatchFaceBg.color

                        if (currentDiyCustomWatchFaceBg.filterIntensity >= 0) {
                            it.colorMatrix = FilterUtils().getFilter(
                                it.name, currentDiyCustomWatchFaceBg.filterIntensity
                            )
                        }
                    }
                }

                is DiyWatchFaceModal.PlacementList -> {

                    if (!selectedFontStyleChange) {
                        diyWatchFaceModal.list.forEach {
                            it.bgLink = currentDiyCustomWatchFaceBg.bgLink
                            it.color = currentDiyCustomWatchFaceBg.color
                        }
                    } else {
                        val placementList = ArrayList<DiyCustomWatchFaceBg>()
                        val firstList = fontStylesMap!![currentDiyCustomWatchFaceBg.fontStyleName]
                        firstList?.forEachIndexed { index, data1 ->

                            var selected = false
                            if (data1.title.lowercase() == "top-centre" || data1.title.lowercase() == "top-center") {
                                selected = true
                            }
                            placementList.add(
                                DiyCustomWatchFaceBg(
                                    bgLink = currentDiyCustomWatchFaceBg.bgLink,
                                    color = currentDiyCustomWatchFaceBg.color,
                                    textLayerLink = data1.text_layer,
                                    name = data1.title,
                                    isSelected = selected,
                                    binUrl = data1.bin_url
                                )
                            )
                        }
                        diyWatchFaceModal.list = placementList
                    }


                }

                is DiyWatchFaceModal.ColorList -> {

                }

                is DiyWatchFaceModal.FontList -> {


                }
            }
        }

        _updateRv.postValue(true)

    }


    fun logInsiderEvent() {
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_BACKGROUND + selectedBackgroundType)
        if (selectedImageType.isNotEmpty())
            sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_IMAGE + selectedImageType)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_FEATURED + selectedFeatureImageName)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_FILTER + selectedFilterName)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_FILTERINTENSITY + selectedFilterIntensityValue)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_PLACEMENT + selectedPlacement)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_TEXTSTYLE + selectedTextStyle)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TRANSFERRED_TEXTCOLOUR + selectedTextColorCode)
    }

    fun getImageTemplates() {
        viewModelScope.launch {
            watchFaceRepository.getCustomDiyWatchFacesData().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
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
                            feedInitialData(it)
                        }
                    }
                }
            }
        }

    }


    fun deleteTempFile() {
        if (localFilePath == null) return

        try {
            val cacheFile = File(Uri.parse(localFilePath).toString())
            cacheFile.deleteRecursively()
        } catch (exp: Exception) {
            LOGS.d("Delete Failed")
        }
    }


    fun earnRewardsPoints() {
        if (localDataStore.getIsWatchFaceRewardEarned()) return

        GlobalScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }

                    else -> {}
                }
            }
        }
    }

    fun getDiyType(): ArrayList<DiyCustomWatchType> {
        val data = ArrayList<DiyCustomWatchType>()
        data.add(DiyCustomWatchType("Background", true, R.drawable.ic_bg, FilterType.BACKGROUND))
        data.add(DiyCustomWatchType("Filters", false, R.drawable.ic_filters, FilterType.FILTER))
        data.add(DiyCustomWatchType("Text", false, R.drawable.ic_wf_text, FilterType.TEXT))
        return data
    }

    fun createDiyOnline(
        diyMyCreation: DiyMyCreation, diyCustomWatchFace: DiyCustomWatchFace
    ) {
        //String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
        viewModelScope.launch(Dispatchers.IO) {

            watchFaceRepository.createDiyWatchFaceOnline(diyMyCreation).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    createDiyOnline(
                                        diyMyCreation, diyCustomWatchFace
                                    )
                                }

                                override fun no() {

                                }
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _setDiyCustomWatchFace.postValue(
                                Event(
                                    Pair(
                                        diyMyCreation, diyCustomWatchFace
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    private fun sortTextStyleList(fontStyleList: ArrayList<DiyCustomWatchFaceBg>): List<DiyCustomWatchFaceBg> {
        return fontStyleList.sortedWith(compareBy<DiyCustomWatchFaceBg> {
            when (it.name.lowercase()) {
                "modern" -> 0
                "classic" -> 1
                "light" -> 2
                else -> 3
            }
        }.thenByDescending {
            it.sortIndex
        })
    }


}