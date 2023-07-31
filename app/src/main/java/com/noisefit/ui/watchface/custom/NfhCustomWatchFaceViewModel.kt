package com.noisefit.ui.watchface.custom

import android.graphics.RectF
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.data.enums.CustomWatchFaceViewState
import com.noisefit_commans.data.enums.GridType
import com.noisefit_commans.data.enums.WidgetDimensions
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.CustomGrid
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.models.WatchFacesCustomHybrid
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NfhCustomWatchFaceViewModel @Inject constructor(
    val rewardsRepository: RewardsRepository,
    val localDataStore: DataStoredInterface,
    ) : BaseViewModel() {

    var imagePath: String? = null
    var backgroundColor: String? = null
    var slotAvailableArray: Array<BooleanArray> = arrayOf(
        booleanArrayOf(true, true, true),
        booleanArrayOf(true, true, true),
        booleanArrayOf(true, true, true)
    )

    var watchfaceSelectedPos: Int? = null
    var addedViews = ArrayList<AddedCustomWatchFaceView>()
    var selectedPosition = -1

    private val _pickWidget = MutableLiveData<Event<GridType>>()
    val pickWidget: LiveData<Event<GridType>> = _pickWidget

    private val _viewState =
        MutableLiveData(CustomWatchFaceViewState.WALLPAPER)

    val viewState: LiveData<CustomWatchFaceViewState> = _viewState


    fun onBackClicked(view: View) {
        _viewState.value = CustomWatchFaceViewState.WALLPAPER

    }

    fun onNextClicked(view: View) {
        _viewState.value = CustomWatchFaceViewState.GRID
    }

    var isWatchFaceAwardEarned = false

    init {
        isWatchFaceAwardEarned = localDataStore.getIsWatchFaceRewardEarned()
    }


    fun earnRewardsPoints() {
        if (isWatchFaceAwardEarned) return

        viewModelScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        isWatchFaceAwardEarned = true
                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }
                    else -> {}
                }
            }
        }
    }

    fun reset() {
        addedViews.clear()
        slotAvailableArray = arrayOf(
            booleanArrayOf(true, true, true),
            booleanArrayOf(true, true, true),
            booleanArrayOf(true, true, true)
        )
    }

    fun getGridOptions(): List<CustomGrid> {
        return arrayListOf(
            CustomGrid(GridType.TYPE_1_X_1, R.drawable.ic_grid_1_x_1),
            CustomGrid(GridType.TYPE_1_X_2, R.drawable.ic_grid_1_x_2),
            CustomGrid(GridType.TYPE_1_X_3, R.drawable.ic_grid_1_x_3),
            CustomGrid(GridType.TYPE_2_X_1, R.drawable.ic_grid_2_x_1),
            CustomGrid(GridType.TYPE_2_X_2, R.drawable.ic_grid_2_x_2),
            CustomGrid(GridType.TYPE_2_X_3, R.drawable.ic_grid_2_x_3),
            CustomGrid(GridType.TYPE_3_X_1, R.drawable.ic_grid_3_x_1),
            CustomGrid(GridType.TYPE_3_X_2, R.drawable.ic_grid_3_x_2)
        )
    }


    // Single Grid Size 90x90
    // Full Grid 270x270
    fun getView(gridType: GridType): RectF? {
        getStartPoint(gridType)?.let {
            val (x, y) = it
            val rect = when (gridType) {
                GridType.TYPE_1_X_1 -> RectF(x, y, x + 90f, y + 90f)
                GridType.TYPE_1_X_2 -> RectF(x, y, x + 180f, y + 90f)
                GridType.TYPE_1_X_3 -> RectF(x, y, x + 270f, y + 90f)
                GridType.TYPE_2_X_1 -> RectF(x, y, x + 90f, y + 180f)
                GridType.TYPE_2_X_2 -> RectF(x, y, x + 180f, y + 180f)
                GridType.TYPE_2_X_3 -> RectF(x, y, x + 270f, y + 180f)
                GridType.TYPE_3_X_1 -> RectF(x, y, x + 90f, y + 270f)
                GridType.TYPE_3_X_2 -> RectF(x, y, x + 180f, y + 270f)
                else -> {
                    //TODO: Using same adapter for vision. this case might not appear, if its appear then need to create a new adapter
                    RectF(x, y, x + 180f, y + 270f)
                }
            }
            addedViews.add(AddedCustomWatchFaceView(y, x, gridType, rect, null, null))
            return rect
        } ?: return null
    }

    private fun getStartPoint(gridType: GridType): Pair<Float, Float>? {
        when (gridType) {
            GridType.TYPE_1_X_1 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            slotAvailableArray[x][y] = false
                            return Pair((y * 90).toFloat(), (x * 90).toFloat())
                        }
                    }
                }
                return null
            }
            GridType.TYPE_1_X_2 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if (y == 0 || y == 1) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }
                            }
                        }
                    }
                }
                return null
            }
            GridType.TYPE_1_X_3 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if (y == 0) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }

                            }
                        }
                    }
                }
            }
            GridType.TYPE_2_X_1 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if (x == 0 || x == 1) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }
                            }
                        }
                    }
                }
            }
            GridType.TYPE_2_X_2 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if ((x == 0 || x == 1) && (y == 0 || y == 1)) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }


                            }
                        }
                    }
                }
            }
            GridType.TYPE_2_X_3 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if ((x == 0 || x == 1) && y == 0) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }


                            }
                        }
                    }
                }
            }
            GridType.TYPE_3_X_1 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if (x == 0) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }


                            }
                        }
                    }
                }
            }
            GridType.TYPE_3_X_2 -> {
                slotAvailableArray.forEachIndexed { x, booleans ->
                    booleans.forEachIndexed { y, b ->
                        if (b) {
                            if (x == 0 && (y == 0 || y == 1)) {
                                if (checkForAvailableSpace(gridType, x, y)) {
                                    return Pair((y * 90).toFloat(), (x * 90).toFloat())
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
        return null
    }

    fun checkForAvailableSpace(gridType: GridType, x: Int, y: Int): Boolean {
        when (gridType) {
            GridType.TYPE_1_X_1 -> {
                return false
            }
            GridType.TYPE_1_X_2 -> {
                if (slotAvailableArray[x][y] && slotAvailableArray[x][y + 1]) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x][y + 1] = false
                    return true
                }
            }
            GridType.TYPE_1_X_3 -> {
                if (slotAvailableArray[x][y] &&
                    slotAvailableArray[x][y + 1] &&
                    slotAvailableArray[x][y + 2]
                ) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x][y + 1] = false
                    slotAvailableArray[x][y + 2] = false
                    return true
                }
            }
            GridType.TYPE_2_X_1 -> {
                if (slotAvailableArray[x][y] &&
                    slotAvailableArray[x + 1][y]
                ) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x + 1][y] = false
                    return true
                }
            }
            GridType.TYPE_2_X_2 -> {
                if (slotAvailableArray[x][y] &&
                    slotAvailableArray[x][y + 1] &&
                    slotAvailableArray[x + 1][y] &&
                    slotAvailableArray[x + 1][y + 1]
                ) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x][y + 1] = false
                    slotAvailableArray[x + 1][y] = false
                    slotAvailableArray[x + 1][y + 1] = false
                    return true
                }
            }
            GridType.TYPE_2_X_3 -> {
                if (slotAvailableArray[x][y] &&
                    slotAvailableArray[x][y + 1] &&
                    slotAvailableArray[x][y + 2] &&
                    slotAvailableArray[x + 1][y] &&
                    slotAvailableArray[x + 1][y + 1] &&
                    slotAvailableArray[x + 1][y + 2]
                ) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x][y + 1] = false
                    slotAvailableArray[x][y + 2] = false
                    slotAvailableArray[x + 1][y] = false
                    slotAvailableArray[x + 1][y + 1] = false
                    slotAvailableArray[x + 1][y + 2] = false
                    return true
                }
            }
            GridType.TYPE_3_X_1 -> {
                if (slotAvailableArray[x][y] &&
                    slotAvailableArray[x + 1][y] &&
                    slotAvailableArray[x + 2][y]
                ) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x + 1][y] = false
                    slotAvailableArray[x + 2][y] = false
                    return true
                }
            }
            GridType.TYPE_3_X_2 -> {
                if (slotAvailableArray[x][y] &&
                    slotAvailableArray[x + 1][y] &&
                    slotAvailableArray[x + 2][y] &&
                    slotAvailableArray[x][y + 1] &&
                    slotAvailableArray[x + 1][y + 1] &&
                    slotAvailableArray[x + 2][y + 1]
                ) {
                    slotAvailableArray[x][y] = false
                    slotAvailableArray[x + 1][y] = false
                    slotAvailableArray[x + 2][y] = false
                    slotAvailableArray[x][y + 1] = false
                    slotAvailableArray[x + 1][y + 1] = false
                    slotAvailableArray[x + 2][y + 1] = false
                    return true
                }
            }
            else -> {}
        }
        return false
    }

    fun onImageClicked(x: Float, y: Float) {
        LOGS.d("x : $x  y : $y")
        addedViews.forEachIndexed { index, it ->
            if (it.rect.contains(x, y)) {
                LOGS.d("Point in rectangle ${it.gridType}")
                selectedPosition = index
                _pickWidget.postValue(Event(it.gridType))
                return@forEachIndexed
            }
        }

    }

    fun generateWatchFaceData(): WatchFacesCustomHybrid? {
        if (backgroundColor == null && imagePath == null) {
            sendMessage("Please select background color or wallpaper")
            return null
        }
        val widgetsData = generateCoordinates()
        if (widgetsData.isNullOrEmpty()) {
            sendMessage("Please select Widgets")
            return null
        }


        return WatchFacesCustomHybrid(
            backgroundColor = backgroundColor,
            localImagePath = imagePath,
            watchFacesSizeCoor = generateCoordinates()
        )
    }

    private fun generateCoordinates(): List<WatchFacesCustomHybrid.WatchFaceSizeCoordinate> {
        val list = ArrayList<WatchFacesCustomHybrid.WatchFaceSizeCoordinate>()

        addedViews.forEach {


            //320/3 107
            val height = (it.rect.height() / 90) * 107
            val width = (it.rect.width() / 90) * 107
            LOGS.d("height $height, width $width")

            val x = (it.x / 90) * 107
            val y = (it.y / 90) * 107

            LOGS.d("x $x, y $y")


            list.add(
                WatchFacesCustomHybrid.WatchFaceSizeCoordinate(
                    coordinateX = y.toInt(),
                    coordinateY = x.toInt(),
                    pointX = width.toInt(),
                    pointY = height.toInt(),
                    typeTxt = it.widget.let { widget -> widget?.type } ?: -1,
                    color = it.color ?: "#FFFFFF"
                )
            )

        }
        return list
    }

    fun getWidgetIconName(selectedWidget: WatchFaceWidgets): String {
        //R.drawable.ic_wf_distance
        val currentView = addedViews[selectedPosition]

        selectedWidget.type
        currentView.gridType

        val type = when (selectedWidget.type) {
            13 -> "time_"
            14 -> "reminder_"
            9 -> "active_minutes_"
            12 -> "activity_graph_"
            8 -> "battery_"
            2 -> "calories_"
            4 -> "date_"
            3 -> "distance_"
            0 -> "heart_rate_"
            else -> ""
        }

        val grid = when (currentView.gridType) {
            GridType.TYPE_1_X_1 -> "1_1"
            GridType.TYPE_1_X_2 -> "1_2"
            GridType.TYPE_1_X_3 -> "1_3"
            GridType.TYPE_2_X_1 -> "2_1"
            GridType.TYPE_2_X_2 -> "2_2"
            GridType.TYPE_2_X_3 -> "2_3"
            GridType.TYPE_3_X_1 -> "3_1"
            GridType.TYPE_3_X_2 -> "3_2"
            else -> {
                //TODO: Using same adapter for vision. this case might not appear, if its appear then need to create a new adapter
                "3_2"
            }
        }
        return "$type$grid.png"


    }

    fun getWidgetDimensions(widgetDimensions: WidgetDimensions): Pair<Int, Int> {
        when (widgetDimensions) {
            WidgetDimensions._50x66 -> {
                return Pair(50, 66)
            }
            WidgetDimensions._86x36 -> TODO()
            WidgetDimensions._89x91 -> TODO()
            WidgetDimensions._145x56 -> TODO()
            WidgetDimensions._168x32 -> TODO()
            WidgetDimensions._100x32 -> TODO()
            WidgetDimensions._236x32 -> TODO()
            WidgetDimensions._151x32 -> TODO()
            WidgetDimensions._14x29 -> TODO()
            WidgetDimensions._29x14 -> TODO()
            WidgetDimensions._59x33 -> TODO()
            WidgetDimensions._33x59 -> TODO()
            WidgetDimensions._148x35 -> TODO()
            WidgetDimensions._248x45 -> TODO()
            WidgetDimensions._203x45 -> TODO()
            WidgetDimensions._173x43 -> TODO()
        }
    }
}



data class AddedCustomWatchFaceView(
    val x: Float,
    val y: Float,
    val gridType: GridType,
    val rect: RectF,
    var widget: WatchFaceWidgets?,
    var color: String?
)