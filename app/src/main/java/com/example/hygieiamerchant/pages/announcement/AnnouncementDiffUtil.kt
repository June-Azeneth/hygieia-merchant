package com.example.hygieiamerchant.pages.announcement

import androidx.recyclerview.widget.DiffUtil
import com.example.hygieiamerchant.data_classes.Announcement

class AnnouncementDiffUtil(
    private val oldList: ArrayList<Announcement>,
    private val newList: ArrayList<Announcement>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return  oldList[oldItemPosition] == newList[newItemPosition]
    }
}