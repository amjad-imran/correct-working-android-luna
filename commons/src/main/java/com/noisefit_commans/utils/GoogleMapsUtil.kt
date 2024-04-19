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
import com.noisefit_commans.models.LocationDataNetwork
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
        result: List<LocationDataNetwork>
    ): Flow<LatLngBounds> {
        return flow {
            val builder = LatLngBounds.Builder()
            val color = "#8ed3f1"
            val myPolylineOptionsInstance = PolylineOptions()
                .width(10f)
                .color(Color.parseColor(color))

            for (i in result.indices) {
                if (result[i].lat != null && result[i].long != null) {
                    val currentLtnLng = LatLng(result[i].lat!!, result[i].long!!)
                    if (i == 0) {
                        withContext(Dispatchers.Main) {
                            addMarker(
                                googleMap,
                                currentLtnLng,
                                activity,
                                R.drawable.ic_map_loc_start
                            )
                        }
                    } else if (i == (result.size - 1)) {
                        withContext(Dispatchers.Main) {
                            addMarker(
                                googleMap,
                                currentLtnLng,
                                activity,
                                R.drawable.ic_map_loc_end
                            )
                        }
                    }

                    myPolylineOptionsInstance.add(currentLtnLng)
                    builder.include(currentLtnLng)
                }
            }

            withContext(Dispatchers.Main) {
                googleMap.addPolyline(myPolylineOptionsInstance)
            }

            emit(builder.build())

        }
    }

    private fun addMarker(
        googleMap: GoogleMap,
        currentLtnLng: LatLng,
        activity: Activity,
        drawable: Int
    ) {
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
                            drawable
                        )
                    )
                )
        )
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