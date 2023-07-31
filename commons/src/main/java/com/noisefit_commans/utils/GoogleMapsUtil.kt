package com.noisefit_commans.utils


import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.noisefit_commans.R
import com.noisefit_commans.models.GPSDataResponse
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


class GoogleMapsUtil
@Inject
constructor() {


    suspend fun plotLocationModalGoogleMaps(
        activity: Activity,
        googleMap: GoogleMap,
        result: List<GPSDataResponse>
    ): Flow<LatLngBounds> {
        return flow {
            val startingLtnLng = LatLng(result[0].latitude, result[0].longitude)
            withContext(Dispatchers.Main) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(startingLtnLng)
                        .draggable(false)
                        .flat(false)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                createStoreMarker(
                                    activity,
                                    "",
                                    R.drawable.baseline_place_red_500_24dp
                                )
                            )
                        )
                )
            }

            val builder = LatLngBounds.Builder()
            builder.include(startingLtnLng)
            for (i in 1 until result.size) {
                val currentLtnLng = LatLng(result[i].latitude, result[i].longitude)
                val lastLtnLng = LatLng(result[i - 1].latitude, result[i - 1].longitude)
                builder.include(currentLtnLng)
                val color = "#ffcc0000"

                val options =
                    PolylineOptions()
                        .width(10f)
                        .color(Color.parseColor(color))
                        .geodesic(true)

                options.add(lastLtnLng)
                options.add(currentLtnLng)
                withContext(Dispatchers.Main) {
                    val line = googleMap.addPolyline(options)
                    line.endCap = RoundCap()
                }

            }
            if (result.size > 1) {
                val lastLtnLng = LatLng(
                    result[result.size - 1].latitude,
                    result[result.size - 1].longitude
                )
                withContext(Dispatchers.Main) {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(lastLtnLng)
                            .draggable(false)
                            .flat(false)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    createStoreMarker(
                                        activity,
                                        "",
                                        R.drawable.baseline_place_green_500_24dp
                                    )
                                )
                            )
                    )
                }

            }

            emit(builder.build())

        }
    }

    suspend fun plotLocationCyclingGoogleMaps(
        activity: Activity,
        googleMap: GoogleMap,
        result: ArrayList<DoubleArray>
    ): Flow<LatLngBounds> {
        return flow {
            val startingLatLng = result[0]
            val builder = LatLngBounds.Builder()
            if (!startingLatLng[0].isNaN() && !startingLatLng[1].isNaN()) {
                val startingLtnLng = LatLng(startingLatLng[0], startingLatLng[1])
                withContext(Dispatchers.Main) {
                    googleMap.addMarker(
                        MarkerOptions().position(startingLtnLng)
                            .draggable(false)
                            .flat(false)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    createStoreMarker(
                                        activity,
                                        "",
                                        R.drawable.ic_map_loc_start
                                    )
                                )
                            )
                    )
                }

                builder.include(startingLtnLng)
            }
//            LOGS.d("GPSSSSS ${Gson().toJson(result)}")
            for (i in 1 until result.size) {

                val current = result[i]
//                LOGS.d("GPSSSSS ${current[2].toBigDecimal()}")
                val last = result[i - 1]
                var currentLtnLng: LatLng? = null
                var lastLtnLng: LatLng? = null
                if (!current[0].isNaN() && !current[1].isNaN()) {
                    currentLtnLng = LatLng(current[0], current[1])
                    builder.include(currentLtnLng)
                }

                if (!last[0].isNaN() && !last[1].isNaN()) {
                    lastLtnLng = LatLng(last[0], last[1])
                }

                if (currentLtnLng != null && lastLtnLng != null) {
                    val color = "#ca99ff"
                    val options =
                        PolylineOptions()
                            .width(10f)
                            .color(Color.parseColor(color))
                            .geodesic(true)

                    options.add(lastLtnLng)
                    options.add(currentLtnLng)
                    withContext(Dispatchers.Main) {
                        val line = googleMap.addPolyline(options)
                        line.endCap = RoundCap()
                    }

                }

            }
            if (result.size > 1) {
                val current = result[result.size - 1]
                if (!current[0].isNaN() && !current[1].isNaN()) {
                    val currentLtnLng = LatLng(current[0], current[1])
                    withContext(Dispatchers.Main) {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(currentLtnLng)
                                .draggable(false)
                                .flat(false)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        createStoreMarker(
                                            activity,
                                            "",
                                            R.drawable.ic_map_loc_end
                                        )
                                    )
                                )
                        )
                    }

                }


            }

            emit(builder.build())

        }
    }

    suspend fun plotLocationModalGoogleMaps(
        activity: Activity,
        googleMap: GoogleMap,
        result: ArrayList<DoubleArray>
    ): Flow<LatLngBounds> {
        return flow {
            val startingLatLng = result[0]
            val builder = LatLngBounds.Builder()
            if (!startingLatLng[0].isNaN() && !startingLatLng[1].isNaN()) {
                val startingLtnLng = LatLng(startingLatLng[0], startingLatLng[1])
                withContext(Dispatchers.Main) {
                    googleMap.addMarker(
                        MarkerOptions().position(startingLtnLng)
                            .draggable(false)
                            .flat(false)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    createStoreMarker(
                                        activity,
                                        "",
                                        R.drawable.ic_map_loc_start
                                    )
                                )
                            )
                    )
                }

                builder.include(startingLtnLng)
            }
            for (i in 1 until result.size) {

                val current = result[i]
                val last = result[i - 1]
                var currentLtnLng: LatLng? = null
                var lastLtnLng: LatLng? = null
                if (!current[0].isNaN() && !current[1].isNaN()) {
                    currentLtnLng = LatLng(current[0], current[1])
                    builder.include(currentLtnLng)
                }

                if (!last[0].isNaN() && !last[1].isNaN()) {
                    lastLtnLng = LatLng(last[0], last[1])
                }

                if (currentLtnLng != null && lastLtnLng != null) {
                    val color = "#ffca99ff"
                    val options =
                        PolylineOptions()
                            .width(10f)
                            .color(Color.parseColor(color))
                            .geodesic(true)

                    options.add(lastLtnLng)
                    options.add(currentLtnLng)
                    withContext(Dispatchers.Main) {
                        val line = googleMap.addPolyline(options)
                        line.endCap = RoundCap()
                    }

                }

            }
            if (result.size > 1) {
                val current = result[result.size - 1]
                if (!current[0].isNaN() && !current[1].isNaN()) {
                    val currentLtnLng = LatLng(current[0], current[1])
                    withContext(Dispatchers.Main) {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(currentLtnLng)
                                .draggable(false)
                                .flat(false)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        createStoreMarker(
                                            activity,
                                            "",
                                            R.drawable.ic_map_loc_end
                                        )
                                    )
                                )
                        )
                    }

                }


            }

            emit(builder.build())

        }
    }


    private fun createStoreMarker(activity: Activity, text: String, drawable: Int): Bitmap {
        val markerLayout: View = activity.layoutInflater.inflate(R.layout.layout_marker, null)
        val markerImage: ImageView = markerLayout.findViewById(R.id.marker_image) as ImageView
        val markerRating = markerLayout.findViewById(R.id.marker_text) as TextView
        markerImage.setImageResource(drawable)
        markerRating.text = text
        markerLayout.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerLayout.layout(0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            markerLayout.measuredWidth,
            markerLayout.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerLayout.draw(canvas)
        return bitmap
    }
}