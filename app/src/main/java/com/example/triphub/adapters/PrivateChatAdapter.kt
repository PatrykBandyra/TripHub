package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ItemFriendBinding
import com.example.triphub.models.User

class PrivateChatAdapter(private val context: Context, var items: MutableList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    private inner class MyViewHolder(val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemFriendBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val friend: User = items[position]
        if (holder is MyViewHolder) {
            Glide.with(context)
                .load(friend.image)
                .placeholder(R.drawable.ic_user_placeholder_black)
                .into(holder.binding.ivUserImage)
            holder.binding.tvName.text = friend.name
            holder.binding.tvEmail.text = friend.email

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, friend)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    interface OnClickListener {
        fun onClick(position: Int, friend: User)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}