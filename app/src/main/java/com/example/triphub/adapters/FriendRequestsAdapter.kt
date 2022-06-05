package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.activities.FriendsActivity
import com.example.triphub.databinding.ItemFriendRequestBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

class FriendRequestsAdapter(private val context: Context, var items: MutableList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class MyViewHolder(val binding: ItemFriendRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemFriendRequestBinding.inflate(
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
        }
    }

    override fun getItemCount() = items.size

    fun removeAt(activity: FriendsActivity, user: User, adapterPosition: Int) {
        val userFriendRequestId: String = items[adapterPosition].id

        val userHashMap: HashMap<String, Any> = hashMapOf()
        user.friendRequests.remove(userFriendRequestId)
        userHashMap[Constants.Models.User.FRIEND_REQUESTS] = user.friendRequests

        UserFireStore().updateUser(
            activity,
            user.id,
            userHashMap,
            isFromAdapter = true,
            isFriendRequest = true
        )
    }

    fun notifyAcceptFriendRequest(activity: FriendsActivity, user: User, position: Int) {
        val userFriend: User = items[position]

        user.friendRequests.remove(userFriend.id)
        user.friendIds.add(userFriend.id)
        userFriend.friendIds.add(user.id)

        UserFireStore().addFriend(activity, user, userFriend)
    }
}