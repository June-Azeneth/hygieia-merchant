package com.example.hygieiamerchant.pages.promos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.utils.Commons
import com.google.android.material.imageview.ShapeableImageView

class PromoAdapter(
    private val promoList: ArrayList<Promo>,
    private val onEditClickListener: OnItemClickListener,
    private val onDeleteClickListener: OnDeleteClickListener
) :
    RecyclerView.Adapter<PromoAdapter.MyViewHolder>() {

    private val commons : Commons = Commons()

    interface OnItemClickListener {
        fun onEditClick(item: Promo)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(rewards: Promo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.promo_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return promoList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = promoList[position]
        val context = holder.itemView.context

        //format the dates before passing it to the designated TextViews
        val start = currentItem.dateStart?.let { commons.dateFormatMMMDDYYYY(it) }
        val end = currentItem.dateEnd?.let { commons.dateFormatMMMDDYYYY(it) }

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

        Glide.with(holder.itemView)
            .load(currentItem.photo)
            .apply(RequestOptions.centerCropTransform())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.prodPhoto)

        holder.promoName.text = currentItem.promoName
        holder.promoStart.text = context.getString(R.string.date_start_template, start)
        holder.promoEnd.text = context.getString(R.string.date_end_template, end)
        holder.status.text = currentItem.status.uppercase()
        holder.id.text = context.getString(R.string.prod_id, currentItem.id)

        holder.editItem.setOnClickListener {
            onEditClickListener.onEditClick(currentItem)
        }

        holder.deleteItem.setOnClickListener {
            onDeleteClickListener.onDeleteClick(currentItem)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prodPhoto: ShapeableImageView = itemView.findViewById(R.id.photo)
        val promoName: TextView = itemView.findViewById(R.id.promo_name)
        val promoStart: TextView = itemView.findViewById(R.id.promoStart)
        val promoEnd: TextView = itemView.findViewById(R.id.promoEnd)
        val status: TextView = itemView.findViewById(R.id.status)
        val id: TextView = itemView.findViewById(R.id.promo_id)
        val deleteItem : AppCompatButton = itemView.findViewById(R.id.deleteItem)
        val editItem : AppCompatButton = itemView.findViewById(R.id.editItem)
    }
}