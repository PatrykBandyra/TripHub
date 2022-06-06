package com.example.triphub.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.activities.MyTripBoardActivity
import com.example.triphub.databinding.ItemCardBinding
import com.example.triphub.models.MyCard
import com.example.triphub.models.SelectedMember

class MyTripCardListItemsAdapter(private val context: Context, private val cards: ArrayList<MyCard>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    private inner class MyViewHolder(val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(
            ItemCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = cards[position]

        if (holder is MyViewHolder) {

            if (model.labelColor.isNotEmpty()) {
                holder.binding.viewLabelColor.visibility = View.VISIBLE
                holder.binding.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.binding.viewLabelColor.visibility = View.GONE
            }

            holder.binding.tvCardName.text = model.name

            if ((context as MyTripBoardActivity).mAssignedMemberDetailsList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMember> = ArrayList()
                context.mAssignedMemberDetailsList.forEach { user ->
                    model.assignedTo.forEach { id ->
                        if (user.id == id) {
                            val selectedMembers = SelectedMember(user.id, user.image)
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                    } else {
                        holder.binding.rvCardSelectedMembersList.visibility = View.VISIBLE
                        holder.binding.rvCardSelectedMembersList.layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter =
                            MyTripCardMemberListItemsAdapter(context, selectedMembersList, false)
                        holder.binding.rvCardSelectedMembersList.adapter = adapter
                        adapter.setOnClickListener(object :
                            MyTripCardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(holder.adapterPosition)
                                }
                            }
                        })
                    }
                } else {
                    holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int = cards.size

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }
}