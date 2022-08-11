package com.realityexpander.bikingapp.ui

import android.annotation.SuppressLint
import android.content.Context
import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.common.TrackingUtility
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import kotlinx.android.synthetic.main.marker_view.view.tvAvgSpeed
import kotlinx.android.synthetic.main.marker_view.view.tvDate
import kotlinx.android.synthetic.main.marker_view.view.tvDistance
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pop-up window, when we click on a bar in the bar chart
 */
@SuppressLint("ViewConstructor")
class CustomMarkerView(
    val rides: List<Ride>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if(e == null) {
            return
        }
        val curRideId = e.x.toInt()
        val ride = rides[curRideId]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = ride.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        "${ride.avgSpeedInKMH}km/h".also {
            tvAvgSpeed.text = it
        }

        "${ride.distanceInMeters / 1000f}km".also {
            tvDistance.text = it
        }

        tvDuration.text =
            TrackingUtility.getFormattedStopWatchTime(
                ride.timeInMillis
            )

        "${ride.caloriesBurned}kcal".also {
            tvCaloriesBurned.text = it
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }
}