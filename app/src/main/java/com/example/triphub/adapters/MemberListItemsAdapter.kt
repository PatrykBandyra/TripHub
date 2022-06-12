package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ItemMemberBinding
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

class MemberListItemsAdapter(private val context: Context, private val items: ArrayList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    private inner class MyViewHolder(val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = items[position]

        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(holder.binding.ivMemberImage)

            holder.binding.tvMemberName.text = model.name
            holder.binding.tvMemberEmail.text = model.email

            if (model.selected) {
                holder.binding.ivSelectedMember.visibility = View.VISIBLE
            } else {
                holder.binding.ivSelectedMember.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UNSELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }
}