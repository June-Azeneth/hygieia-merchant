package com.example.hygieiamerchant.pages.announcement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Announcement
import com.example.hygieiamerchant.utils.Commons

class AnnouncementAdapter(
    private var announcementList: ArrayList<Announcement>,
    private val onItemClickListener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(item: Announcement) {
            // Default implementation
        }
    }
) :
    RecyclerView.Adapter<AnnouncementAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: Announcement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.announcement_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = announcementList[position]
        val context = holder.itemView.context

        holder.title.text = currentItem.title
        holder.body.text = currentItem.body
        holder.date.text = Commons().dateFormatMMMDDYYYYDate(currentItem.date)

        holder.item.setOnClickListener {
            onItemClickListener.onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val body: TextView = itemView.findViewById(R.id.body)
        val date: TextView = itemView.findViewById(R.id.date)
        val item: CardView = itemView.findViewById(R.id.item)
    }

    fun setData(newRewardsList: List<Announcement>) {
        val oldRewardList = ArrayList(announcementList) // Create a copy of the old list
        announcementList.clear() // Clear the old list
        announcementList.addAll(newRewardsList) // Update the list with the new data

        val diffUtil =
            AnnouncementDiffUtil(
                oldRewardList,
                announcementList
            ) // Pass the old and new lists to the DiffUtil
        val diffResults =
            DiffUtil.calculateDiff(diffUtil) // Calculate the diff between the old and new lists
        diffResults.dispatchUpdatesTo(this) // Dispatch the updates to the adapter
    }
}