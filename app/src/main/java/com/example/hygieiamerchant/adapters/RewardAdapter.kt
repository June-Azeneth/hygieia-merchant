package com.example.hygieiamerchant.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Requests
import com.example.hygieiamerchant.data_classes.Rewards
import com.google.android.material.imageview.ShapeableImageView

class RewardAdapter(
    private val rewardsList: ArrayList<Rewards>,
//    private val onItemClickListener: RequestsAdapter.OnItemClickListener,
//    private val onDeleteClickListener: RequestsAdapter.OnDeleteClickListener
) : RecyclerView.Adapter<RewardAdapter.MyViewHolder>() {

//    interface OnItemClickListener {
//        fun onItemClick(rewards: Rewards)
//
//    }
//
//    interface OnDeleteClickListener {
//        fun onDeleteClick(rewards: Rewards)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardAdapter.MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.reward_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return rewardsList.size
    }

    override fun onBindViewHolder(holder: RewardAdapter.MyViewHolder, position: Int) {
        val currentItem = rewardsList[position]

        Glide.with(holder.itemView)
            .load(currentItem.photo)
            .apply(RequestOptions.centerCropTransform())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.prodPhoto)

        holder.name.text = currentItem.name
        holder.discountRate.text = "${currentItem.discountRate.toString()}%"
        holder.ptsRequired.text = currentItem.pointsRequired.toString()
        holder.discountedPrice.text = "₱${currentItem.discountedPrice}"
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prodPhoto: ShapeableImageView = itemView.findViewById(R.id.photo)
        val name: TextView = itemView.findViewById(R.id.prod_name)
        val discountRate: TextView = itemView.findViewById(R.id.discount)
        val discountedPrice: TextView = itemView.findViewById(R.id.disc_price)
        val ptsRequired: TextView = itemView.findViewById(R.id.points_required)
    }
}