package com.example.hygieiamerchant.pages.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.imageview.ShapeableImageView

class RewardsAdapter(
    private val rewardList: ArrayList<Reward>,
    private val onEditClickListener: OnItemClickListener,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<RewardsAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(item: Reward)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(item: Reward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.reward_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return rewardList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = rewardList[position]
        val context = holder.itemView.context

        Glide.with(holder.itemView)
            .load(currentItem.photo)
            .apply(RequestOptions.centerCropTransform())
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.prodPhoto)

        holder.name.text = currentItem.name
        holder.discountRate.text = context.getString(
            R.string.discountRate,
            "${Commons().formatDecimalNumber(currentItem.discount)}%"
        )
        holder.ptsRequired.text = Commons().formatDecimalNumber(currentItem.pointsRequired)
        holder.discountedPrice.text = context.getString(
            R.string.discountedPrice,
            Commons().formatDecimalNumber(currentItem.discountedPrice)
        )

        holder.deleteItem.setOnClickListener {
            onDeleteClickListener.onDeleteClick(currentItem)
        }

        holder.editItem.setOnClickListener {
            onEditClickListener.onEditClick(currentItem)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prodPhoto: ShapeableImageView = itemView.findViewById(R.id.photo)
        val name: TextView = itemView.findViewById(R.id.prod_name)
        val discountRate: TextView = itemView.findViewById(R.id.discount)
        val discountedPrice: TextView = itemView.findViewById(R.id.disc_price)
        val ptsRequired: TextView = itemView.findViewById(R.id.points_required)
        val deleteItem : AppCompatButton = itemView.findViewById(R.id.deleteItem)
        val editItem : AppCompatButton = itemView.findViewById(R.id.editItem)
    }
}