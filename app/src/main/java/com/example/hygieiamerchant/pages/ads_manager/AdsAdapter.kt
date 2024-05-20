package com.example.hygieiamerchant.pages.ads_manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Ads
import com.example.hygieiamerchant.utils.Commons

class AdsAdapter(
    private val adsList: ArrayList<Ads>,
    private val onEditClickListener: OnItemClickListener,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<AdsAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(ad: Ads)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(ad: Ads)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.ad_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return adsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = adsList[position]
        val context = holder.itemView.context

        Glide.with(holder.itemView)
            .load(currentItem.poster)
            .apply(RequestOptions.centerCropTransform())
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.poster)

        val durationMap = currentItem.duration
        Commons().log("DURATION", durationMap?.size.toString())
        holder.status.text = currentItem.status

        if (durationMap != null) {
            val startDate = durationMap["startDate"]?.let { Commons().dateFormatMMMDDYYYY(it) }
            val endDate = durationMap["endDate"]?.let { Commons().dateFormatMMMDDYYYY(it) }

            holder.duration.text =
                context.getString(R.string.from_to_duration_ads, startDate, endDate)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.poster)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val status: TextView = itemView.findViewById(R.id.status)
        val item: CardView = itemView.findViewById(R.id.item)
    }
}