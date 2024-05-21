package com.example.hygieiamerchant.pages.ads_manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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

        val startDate = currentItem.startDate?.let { Commons().dateFormatMMMDDYYYY(it) }
        val endDate = currentItem.endDate?.let { Commons().dateFormatMMMDDYYYY(it) }

        when (currentItem.status) {
            "Upcoming" -> holder.status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.accent_orange
                )
            )
            "Ongoing" -> holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
            "Paused" -> holder.status.setTextColor(ContextCompat.getColor(context, R.color.blue))
            else -> holder.status.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
        holder.status.text = currentItem.status
        holder.title.text = currentItem.title

        holder.duration.text =
            context.getString(R.string.from_to_duration_ads, startDate, endDate)

        holder.item.setOnClickListener {
            onEditClickListener.onEditClick(currentItem)
        }
        holder.delete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(currentItem)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.poster)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val status: TextView = itemView.findViewById(R.id.status)
        val title: TextView = itemView.findViewById(R.id.title)
        val item: CardView = itemView.findViewById(R.id.item)
        val delete : AppCompatButton = itemView.findViewById(R.id.delete)
    }
}