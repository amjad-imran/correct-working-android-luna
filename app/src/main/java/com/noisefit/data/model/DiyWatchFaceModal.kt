package com.noisefit.data.model

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.noisefit.data.remote.response.WatchFaceCategory2
import com.noisefit.ui.diy.FilterType
import com.noisefit.watch.WatchForm
import com.noisefit_commans.models.WatchFace

sealed class DiyWatchFaceModal {


    class BackgroundList(
        val title: String,
        val list: ArrayList<DiyCustomWatchFaceBg> = ArrayList(),
        var screenType: WatchForm,
        var filterType: FilterType
    ) : DiyWatchFaceModal()

    class FeaturedList(
        val title: String,
        val list: ArrayList<DiyCustomWatchFaceBg> = ArrayList(),
        var screenType: WatchForm,
        var filterType: FilterType
    ) : DiyWatchFaceModal()

    class FilterList(
        val title: String,
        val list: ArrayList<DiyCustomWatchFaceBg> = ArrayList(),
        var screenType: WatchForm,
        var colorIntensity: Int,
        var filterType: FilterType
    ) : DiyWatchFaceModal()

    class PlacementList(
        val title: String,
        var list: ArrayList<DiyCustomWatchFaceBg> = ArrayList(),
        var screenType: WatchForm,
        var filterType: FilterType
    ) : DiyWatchFaceModal()

    class FontList(
        val title: String,
        val list: ArrayList<DiyCustomWatchFaceBg> = ArrayList(),
        var screenType: WatchForm,
        var filterType: FilterType
    ) : DiyWatchFaceModal()


    class ColorList(
        val title: String,
        var filterType: FilterType,
        val colorList: ArrayList<DiyCustomWatchColor> = ArrayList()
    ) : DiyWatchFaceModal()
}


class DiyBackground(
    var diyCustomWatchFaceBg: DiyCustomWatchFaceBg? = null,
    var bgBitmap: Bitmap? = null,
    var textBitmap: Bitmap? = null,
    var colorMatrix: ColorMatrix? = null,
    var width: Int = 0,
    var height: Int = 0,
    var screenType: WatchForm
)


sealed class WatchFace2CategoryModal {


    class CreateYourOwn(
        var screenType: WatchForm,
        var url: String,
        var isWatchSupportDiy: Boolean
    ) : WatchFace2CategoryModal()

    class CategoryList(
        var screenType: WatchForm,
        var categoryData: WatchFaceCategory2,
        var currentPage: Int,
        var currentSubListScrollPos: Int? = 0,
        var hasMoreData: Boolean = true
    ) : WatchFace2CategoryModal()

    class HavingAnIssue() : WatchFace2CategoryModal()

}


sealed class WatchFaceListModal {


    class CreateYourOwn(
        var screenType: WatchForm,
        var uri: String,
        var showCustWfState: Triple<Boolean, Boolean, Boolean>
    ) : WatchFaceListModal()

    class CategoryList(
        var dataList: List<WatchFace> = ArrayList<WatchFace>()
    ) : WatchFaceListModal()


}