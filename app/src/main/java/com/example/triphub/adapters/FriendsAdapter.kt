package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.activities.FriendsActivity
import com.example.triphub.databinding.ItemFriendBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

class FriendsAdapter(private val context: Context, var items: MutableList<User>) :
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
        val user: User = items[position]
        if (holder is MyViewHolder) {
            Glide.with(context)
                .load(user.image)
                .placeholder(R.drawable.ic_user_placeholder_black)
                .into(holder.binding.ivUserImage)
            holder.binding.tvName.text = user.name
            holder.binding.tvEmail.text = user.email

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, user)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun removeAt(activity: FriendsActivity, user: User, position: Int) {
        val userFriendId: String = items[position].id

        val userHashMap: HashMap<String, Any> = hashMapOf()
        user.friendIds.remove(userFriendId)
        userHashMap[Constants.Models.User.FRIEND_IDS] = user.friendIds

        UserFireStore().updateUser(activity, user.id, userHashMap, isFromAdapter = true)
    }

    interface OnClickListener {
        fun onClick(position: Int, friend: User)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}