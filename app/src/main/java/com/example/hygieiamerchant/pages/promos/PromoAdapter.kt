package com.example.hygieiamerchant.pages.promos

import android.icu.text.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.hygieiamerchant.R
import com.example.hygieiamerchant.data_classes.Promo
import com.google.android.material.imageview.ShapeableImageView
import java.util.Date

class PromoAdapter(private val promoList: ArrayList<Promo>) :
    RecyclerView.Adapter<PromoAdapter.MyViewHolder>() {
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
        val start = currentItem.dateStart?.let { formatDateOnly(it) }
        val end = currentItem.dateEnd?.let { formatDateOnly(it) }

        when (currentItem.status) {
            "Upcoming" -> holder.status.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
            "Ongoing"-> holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
            "Paused"-> holder.status.setTextColor(ContextCompat.getColor(context, R.color.blue))
            else -> holder.status.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        Glide.with(holder.itemView)
            .load(currentItem.photo)
            .apply(RequestOptions.centerCropTransform())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.prodPhoto)

        holder.promoName.text = currentItem.promoName
        holder.product.text = context.getString(R.string.product_template, currentItem.product)
        holder.discount.text = context.getString(R.string.discount_template, formatDouble(currentItem.discountRate))
        holder.discPrice.text = context.getString(R.string.disc_price_template, formatDouble(currentItem.discountedPrice))
        holder.pointsReq.text = context.getString(R.string.points_req_template, formatDouble(currentItem.pointsRequired))
        holder.promoStart.text = context.getString(R.string.date_start_template, start)
        holder.promoEnd.text = context.getString(R.string.date_end_template, end)
        holder.status.text = currentItem.status.uppercase()
    }

    private fun formatDateOnly(date: Date): String {
        val format = DateFormat.getDateInstance(DateFormat.DEFAULT)
        return format.format(date)
    }

    private fun formatDouble(value: Double): String {
        return if (value % 1 == 0.0) {
            String.format("%.0f", value)
        } else {
            String.format("%.2f", value)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prodPhoto: ShapeableImageView = itemView.findViewById(R.id.photo)
        val promoName: TextView = itemView.findViewById(R.id.promo_name)
        val product: TextView = itemView.findViewById(R.id.product)
        val discount: TextView = itemView.findViewById(R.id.discount)
        val discPrice: TextView = itemView.findViewById(R.id.disc_price)
        val pointsReq: TextView = itemView.findViewById(R.id.points_required)
        val promoStart: TextView = itemView.findViewById(R.id.promoStart)
        val promoEnd: TextView = itemView.findViewById(R.id.promoEnd)
        val status: TextView = itemView.findViewById(R.id.status)
    }
}