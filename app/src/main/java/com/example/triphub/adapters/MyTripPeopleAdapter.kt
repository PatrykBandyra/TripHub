package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.activities.MyTripPeopleActivity
import com.example.triphub.databinding.ItemMyTripPersonBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User

class MyTripPeopleAdapter(private val context: Context, var items: ArrayList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class MyViewHolder(val binding: ItemMyTripPersonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemMyTripPersonBinding.inflate(
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
                .placeholder(R.drawable.ic_user_placeholder_black)
                .into(holder.binding.ivPersonImage)

            holder.binding.tvPersonName.text = model.name
            holder.binding.tvPersonEmail.text = model.email
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeAt(myTripPeopleActivity: MyTripPeopleActivity, adapterPosition: Int, trip: MyTrip) {
        val user: User = items[adapterPosition]
        if (user.id == UserFireStore().getCurrentUserId()) {
            myTripPeopleActivity.hideProgressDialog()
            myTripPeopleActivity.showErrorSnackBar(R.string.cannot_remove_yourself)
            notifyItemChanged(adapterPosition)
        } else {
            MyTripFireStore().removePersonFromTrip(
                myTripPeopleActivity,
                user,
                trip,
                adapterPosition
            )
        }
    }
}