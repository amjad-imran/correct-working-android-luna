package com.oreo.ui.timelineScreen.addActivity

import android.view.View
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.addLogBottomSheetModels.AddLogBottomSheetDataModels
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import com.oreo.ui.timelineScreen.addActivity.activities.DropdownDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.remove

@HiltViewModel
class AddActivityTimelineSharedViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
) : BaseViewModel() {

    var sourceKey: String ?= null
    var showTimeline = false

    val loadFragment = MutableLiveData<Event<Triple<AddActivityItemsEnum, ItemTimelineResponseModel?, String?>>>()
    val navigateUp = MutableLiveData<Event<Boolean>>()

    val deleteBtnClickedEvent = MutableLiveData<Event<Boolean>>()

    var isDeleteClicked = false

    fun loadFragmentByType(type: AddActivityItemsEnum, editData: ItemTimelineResponseModel?=null, lunaOption: String ?= null) {
        loadFragment.postValue(Event(Triple(type, editData, lunaOption)))
    }

    fun navigateUp() {
        navigateUp.postValue(Event(true))
    }

    fun getAllActivityListMap(): List<AddActivityListTimelineModel> {
        val list = ArrayList<AddActivityListTimelineModel>()

        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_caffeine_intake),
                type = AddActivityItemsEnum.CAFFEINE,
                titleColor = "#EEB69F".toColorInt()
            )
        )

        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_meal_intake),
                type = AddActivityItemsEnum.MEAL,
                titleColor = "#D8D3A3".toColorInt()
            )
        )

        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_light_exposure),
                type = AddActivityItemsEnum.LIGHT_EXPOSURE,
                titleColor = "#F1C48E".toColorInt()
            )
        )

        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_workout),
                type = AddActivityItemsEnum.WORKOUT,
                titleColor = "#8ED3F1".toColorInt()
            )
        )

        /*AddActivityListTimelineModel(
            name = getString(R.string.text_water_consumption),
            type = AddActivityItemsEnum.WATER,
            titleColor = "#8EF1C3".toColorInt()
        ),*/
        val user = localDataStore.getUser()
        if (user?.userInfo?.gender.equals("female", true)) {
            list.add(
                AddActivityListTimelineModel(
                    name = resourcesProvider.getString(R.string.text_period_symptom),
                    type = AddActivityItemsEnum.CYCLE_LOG,
                    titleColor = "#F18EBD".toColorInt()
                )
            )
        }

        /* AddActivityListTimelineModel(
             name = resourcesProvider.getString(R.string.text_nap),
             type = AddActivityItemsEnum.NAP,
             titleColor = "#A8A8ED".toColorInt()
         ),*/
        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_sleep),
                type = AddActivityItemsEnum.SLEEP,
                titleColor = "#C5A8ED".toColorInt()
            )
        )

        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_supplements),
                type = AddActivityItemsEnum.SUPPLEMENTS,
                titleColor = "#A8EDE6".toColorInt()
            )
        )
        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_alcohol_intake),
                type = AddActivityItemsEnum.ALCOHOL,
                titleColor = "#BE7A64".toColorInt()
            )
        )

        list.add(
            AddActivityListTimelineModel(
                name = resourcesProvider.getString(R.string.text_recovery),
                type = AddActivityItemsEnum.RECOVERY,
                titleColor = "#95DCFF".toColorInt()
            )
        )

        return list

    }

    fun showDropdownDialog(anchorView: View, currentItem: AddActivityItemsEnum) {

        val filteredList = (getAllActivityListMap() as ArrayList).apply {
            remove(this.find { it.type == currentItem })
        }

        val dropdownDialog = DropdownDialog(
            context = anchorView.context,
            anchorView = anchorView,
            items = filteredList
        ) { selectedItem ->
            loadFragmentByType(selectedItem)
        }

        dropdownDialog.show()
    }

}

enum class AddActivityItemsEnum {
    ACTIVITIES_LISTING,
    MEAL,
    LIGHT_EXPOSURE,
    WORKOUT,
    CAFFEINE,
    WATER,
    CYCLE_LOG,
    NAP,
    SLEEP,
    SUPPLEMENTS,
    ALCOHOL,
    RECOVERY
}