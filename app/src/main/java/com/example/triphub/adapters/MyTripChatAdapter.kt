package com.example.triphub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ItemContainerReceivedMessageBinding
import com.example.triphub.databinding.ItemContainerSentMessageBinding
import com.example.triphub.models.ChatMessage
import com.example.triphub.models.MyTripChatMessage
import com.example.triphub.models.User
import java.text.SimpleDateFormat
import java.util.*

class MyTripChatAdapter(
    private val context: Context,
    val messages: MutableList<MyTripChatMessage>,
    private val receiverUsers: ArrayList<User>,
    private val user: User
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType {
        SENT {
            override fun getValue(): Int = 1
        },
        RECEIVED {
            override fun getValue(): Int = 2
        };

        abstract fun getValue(): Int
    }

    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: MyTripChatMessage) {
            binding.tvMessage.text = chatMessage.message
            val sdf = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.GERMANY)
            binding.tvDateTime.text = sdf.format(chatMessage.timestamp)
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: MyTripChatMessage) {
            binding.tvMessage.text = chatMessage.message
            val sdf = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.GERMANY)
            binding.tvDateTime.text = sdf.format(chatMessage.timestamp)

            val receiverUser: User? = receiverUsers.find { user ->
                user.id == chatMessage.senderId
            }
            Glide.with(context)
                .load(receiverUser!!.image)
                .placeholder(R.drawable.ic_user_placeholder_black)
                .into(binding.civSenderImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ViewType.SENT.getValue()) {
            SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ViewType.SENT.getValue()) {
            (holder as SentMessageViewHolder).setData(messages[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(messages[position])
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == user.id) {
            ViewType.SENT.getValue()
        } else {
            ViewType.RECEIVED.getValue()
        }
    }


}