package com.realityexpander.bikingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.db.Ride
import com.realityexpander.bikingapp.common.TrackingUtility
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_ride.view.*
import java.text.SimpleDateFormat
import java.util.*

class RideAdapter : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Ride>() {
        override fun areItemsTheSame(oldItem: Ride, newItem: Ride): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ride, newItem: Ride): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    // Run differ asynchronously
    val differ = AsyncListDiffer(this, diffCallback)

    inner class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun submitList(list: List<Ride>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        return RideViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_ride,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = differ.currentList[position]
        // set item data
        holder.itemView.apply {
            Glide
                .with(this)
                .load(ride.img)
                .into(ivRideImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = ride.timestamp
            }
            val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            "${"%.2f".format(ride.avgSpeedInKMH * 0.621371)} mp/h".also {
                tvAvgSpeed.text = it
            }

            "${"%.2f".format(ride.distanceInMeters / 1000f * 0.621371)} mi".also {
                tvDistance.text = it
            }

            tvTime.text = TrackingUtility.getFormattedStopWatchTime(ride.timeInMillis)
            "${ride.caloriesBurned} kcal".also {
                tvCalories.text = it
            }
        }
    }
}