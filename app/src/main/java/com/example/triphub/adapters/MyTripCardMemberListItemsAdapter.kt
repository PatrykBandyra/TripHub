package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ItemCardSelectedMemberBinding
import com.example.triphub.models.SelectedMember

class MyTripCardMemberListItemsAdapter(
    private val context: Context,
    private val items: ArrayList<SelectedMember>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    private inner class MyViewHolder(val binding: ItemCardSelectedMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemCardSelectedMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = items[position]

        if (holder is MyViewHolder) {
            if (position == items.size - 1 && assignMembers) {
                holder.binding.ivAddMember.visibility = View.VISIBLE
                holder.binding.ivSelectedMemberImage.visibility = View.GONE
            } else {
                holder.binding.ivAddMember.visibility = View.GONE
                holder.binding.ivSelectedMemberImage.visibility = View.VISIBLE

                Glide.with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(holder.binding.ivSelectedMemberImage)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }
}