package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.databinding.ItemFriendRequestBinding
import com.example.triphub.databinding.ItemMyTripBinding
import com.example.triphub.models.User

class FriendRequestsAdapter(private val context: Context, var items: ArrayList<User>) :
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
        TODO("Not yet implemented")
    }

    override fun getItemCount() = items.size
}