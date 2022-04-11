package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ItemMyTripBinding
import com.example.triphub.models.MyTrip

class MyTripsAdapter(private val context: Context, private var items: ArrayList<MyTrip>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    private inner class MyViewHolder(val binding: ItemMyTripBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemMyTripBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myTrip: MyTrip = items[position]
        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(myTrip.image)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.binding.ivMyTripImage)
            holder.binding.tvName.text = myTrip.name
            holder.binding.tvCreatedBy.text =
                context.resources.getString(R.string.created_by, myTrip.creatorName)

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, myTrip)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    interface OnClickListener {
        fun onClick(position: Int, myTrip: MyTrip)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun addItems(myTrips: ArrayList<MyTrip>) {
        items.addAll(myTrips)
    }
}