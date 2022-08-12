package com.realityexpander.bikingapp.ui.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.ui.CustomMarkerView
import com.realityexpander.bikingapp.common.TrackingUtility
import com.realityexpander.bikingapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBarChart()
        subscribeToObservers()
    }

    private fun setupBarChart() {
        barChart.xAxis.apply {
            setDrawLabels(false)
            position = XAxis.XAxisPosition.BOTTOM
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            textSize = 12f
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            textSize = 12f
            setDrawGridLines(false)
        }
        barChart.apply {
            legend.isEnabled = false
            description.text = "Average Speed in MPH"
            description.textSize = 16f
            description.textColor = Color.BLACK
            description.typeface = Typeface.DEFAULT_BOLD
            animateX(1500)
        }
    }

    private fun subscribeToObservers() {
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            // in case DB is empty it will be null
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10 * 0.621371) / 10f
                val totalDistanceString = "$totalDistance mi"
                tvTotalDistance.text = totalDistanceString
            }
        })

        viewModel.totalTimeInMillis.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeInMillis = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeInMillis
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val roundedAvgSpeed = round(it * 10f * 0.621371) / 10f
                val totalAvgSpeed = "$roundedAvgSpeed mph"
                tvAverageSpeed.text = totalAvgSpeed
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCaloriesBurned = "$it kcal"
                tvTotalCalories.text = totalCaloriesBurned
            }
        })

        viewModel.ridesSortedByDate.observe(viewLifecycleOwner, Observer { rides ->
            rides?.let { rides ->
                val allAvgSpeeds = rides.indices.map { i ->
                    BarEntry(i.toFloat(), (rides[i].avgSpeedInKMH * 0.621371).toFloat())
                }

                val bardataSettings = BarDataSet(allAvgSpeeds, "Avg MPH")
                bardataSettings.apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                    valueTextSize = 16f
                }

                val lineData = BarData(bardataSettings)
                barChart.data = lineData
                val marker = CustomMarkerView(
                    rides, //.reversed(),
                    requireContext(),
                    R.layout.marker_view
                )
                barChart.marker = marker
                barChart.invalidate()
            }
        })
    }
}