package com.example.triphub.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.activities.AddTripActivity
import com.example.triphub.activities.MainActivity
import com.example.triphub.databinding.ItemMyTripBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyTripsAdapter(private val context: Context, var items: MutableList<MyTrip>) :
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

    fun notifyEditItem(activity: MainActivity, position: Int) {
        val intent = Intent(context, AddTripActivity::class.java)
        intent.putExtra(Constants.Intent.TRIP, items[position])
        intent.putExtra(Constants.Intent.USER_DATA, activity.userData)
        activity.editTripLauncher.launch(intent)
    }

    fun removeAt(activity: MainActivity, adapterPosition: Int) {
        val trip: MyTrip = items[adapterPosition]
        if (trip.image.isNotEmpty()) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(trip.image)
            sRef.delete()
                .addOnSuccessListener {
                    Log.i("Firebase Image", "Trip picture ${trip.image} deleted")
                    MyTripFireStore().deleteTrip(activity,this, trip, adapterPosition)
                }
                .addOnFailureListener {
                    Log.i("Firebase Image", "Could not delete trip picture: ${trip.image}")
                    activity.onTripDeletionFailure()
                }
        } else {
            MyTripFireStore().deleteTrip(activity,this, trip, adapterPosition)
        }
    }

    fun onTripDeletionSuccess(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}