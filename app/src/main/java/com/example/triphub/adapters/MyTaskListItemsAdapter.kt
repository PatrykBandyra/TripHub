package com.example.triphub.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.activities.MyTripBoardActivity
import com.example.triphub.databinding.ItemMyTaskBinding
import com.example.triphub.models.MyTask
import java.util.*

class MyTaskListItemsAdapter(private val context: Context, val tasks: MutableList<MyTask>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mPositionDraggedFrom: Int = -1
    private var mPositionDraggedTo: Int = -1

    private inner class MyViewHolder(val binding: ItemMyTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemMyTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        binding.root.layoutParams = layoutParams
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val task = tasks[position]
        if (holder is MyViewHolder) {
            if (position == tasks.size - 1) {
                holder.binding.tvAddTaskList.visibility = View.VISIBLE
                holder.binding.llTaskItem.visibility = View.GONE
            } else {
                holder.binding.tvAddTaskList.visibility = View.GONE
                holder.binding.llTaskItem.visibility = View.VISIBLE
            }

            holder.binding.tvTaskListTitle.text = task.title
            holder.binding.tvAddTaskList.setOnClickListener {
                holder.binding.tvAddTaskList.visibility = View.GONE
                holder.binding.cvAddTaskListName.visibility = View.VISIBLE
            }

            holder.binding.ibCloseListName.setOnClickListener {
                holder.binding.tvAddTaskList.visibility = View.VISIBLE
                holder.binding.cvAddTaskListName.visibility = View.GONE
            }

            holder.binding.ibDoneListName.setOnClickListener {
                val listName = holder.binding.etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is MyTripBoardActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please enter list name", Toast.LENGTH_SHORT).show()
                }
            }

            holder.binding.ibEditListName.setOnClickListener {
                holder.binding.etEditTaskListName.setText(task.title)
                holder.binding.llTitleView.visibility = View.GONE
                holder.binding.cvEditTaskListName.visibility = View.VISIBLE
            }

            holder.binding.ibCloseEditableView.setOnClickListener {
                holder.binding.llTitleView.visibility = View.VISIBLE
                holder.binding.cvEditTaskListName.visibility = View.GONE
            }

            holder.binding.ibDoneEditListName.setOnClickListener {
                val listName: String = holder.binding.etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is MyTripBoardActivity) {
                        context.updateTaskList(position, listName, task)
                    }
                } else {
                    Toast.makeText(context, "Please enter list name", Toast.LENGTH_SHORT).show()
                }
            }

            holder.binding.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, task.title)
            }

            holder.binding.tvAddCard.setOnClickListener {
                holder.binding.tvAddCard.visibility = View.GONE
                holder.binding.cvAddCard.visibility = View.VISIBLE
            }

            holder.binding.ibCloseCardName.setOnClickListener {
                holder.binding.tvAddCard.visibility = View.VISIBLE
                holder.binding.cvAddCard.visibility = View.GONE
            }

            holder.binding.ibDoneCardName.setOnClickListener {
                val cardName: String = holder.binding.etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is MyTripBoardActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please enter card name", Toast.LENGTH_SHORT).show()
                }
            }

            holder.binding.rvCardList.layoutManager = LinearLayoutManager(context)
            holder.binding.rvCardList.setHasFixedSize(true)
            val adapter = MyTripCardListItemsAdapter(context, task.cards)
            holder.binding.rvCardList.adapter = adapter

            adapter.setOnClickListener(object : MyTripCardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {

                    if (context is MyTripBoardActivity) {
                        context.cardDetails(holder.adapterPosition, cardPosition = cardPosition)
                    }
                }
            })

            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            holder.binding.rvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition

                    Collections.swap(tasks[position].cards, draggedPosition, targetPosition)
                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {
                        (context as MyTripBoardActivity).updateCardsInTaskList(
                            position,
                            tasks[position].cards
                        )
                    }
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
            })
            helper.attachToRecyclerView(holder.binding.rvCardList)
        }
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()

            if (context is MyTripBoardActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()
    }

    override fun getItemCount(): Int = tasks.size

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}