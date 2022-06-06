package com.example.triphub.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.databinding.ItemLabelColorBinding


class LabelColorListItemsAdapter(
    private val context: Context,
    private val items: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickedListener: OnItemClickedListener? = null

    private inner class MyViewHolder(val binding: ItemLabelColorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemLabelColorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: String = items[position]
        if (holder is MyViewHolder) {

            holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))
            if (item == mSelectedColor) {
                holder.binding.ivSelectedColor.visibility = View.VISIBLE
            } else {
                holder.binding.ivSelectedColor.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onItemClickedListener != null) {
                    onItemClickedListener!!.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    interface OnItemClickedListener {
        fun onClick(position: Int, color: String)
    }
}