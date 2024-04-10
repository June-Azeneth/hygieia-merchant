package com.example.hygieiamerchant.pages.scanQR.screens

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Reward

class RedeemRewardAdapter(
    private val rewardList: ArrayList<Reward>,
    private val onItemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<RedeemRewardAdapter.MyViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    interface OnItemClickListener {
        fun onItemClick(item: Reward)
    }

    fun clearSelection() {
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.redeem_reward_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return rewardList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RedeemRewardAdapter.MyViewHolder, position: Int) {
        val currentItem = rewardList[position]
        val context = holder.itemView.context

        holder.name.text = currentItem.name
        holder.discount.text = "${currentItem.discount}%"
        holder.points.text = "${currentItem.pointsRequired}pts"

        val textColor = if (selectedPosition == position) {
            ContextCompat.getColor(context, android.R.color.white)
        } else {
            // Use your default text color here
            ContextCompat.getColor(context, R.color.text_color)
        }

        holder.name.setTextColor(textColor)
        holder.discount.setTextColor(textColor)
        holder.points.setTextColor(textColor)

        // Set background color based on the selected state
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.main_green))

        } else {
            holder.itemView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.item_holder))
        }

        holder.item.setOnClickListener {
            // Update selected position and notify data set changed to trigger UI update
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()

            // Perform other item click operations here
            onItemClickListener.onItemClick(currentItem)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.rewardName)
        val discount: TextView = itemView.findViewById(R.id.rewardDiscount)
        val points : TextView = itemView.findViewById(R.id.points)
        val item: ConstraintLayout = itemView.findViewById(R.id.item)
    }
}