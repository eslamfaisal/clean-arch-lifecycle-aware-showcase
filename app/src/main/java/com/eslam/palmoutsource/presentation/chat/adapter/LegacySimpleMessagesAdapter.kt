package com.eslam.palmoutsource.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.eslam.palmoutsource.R
import com.eslam.palmoutsource.databinding.ItemMessageBinding
import com.eslam.palmoutsource.presentation.chat.Message

/**
 * Legacy RecyclerView adapter that accepts messages in the constructor.
 *
 * This adapter demonstrates an older, less efficient way of handling list updates.
 * - It takes the list of messages directly in its constructor.
 * - It uses `notifyDataSetChanged()` for updates, which is inefficient.
 */
class LegacySimpleMessagesAdapter(
    private var messages: List<Message>
) : RecyclerView.Adapter<LegacySimpleMessagesAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding: ItemMessageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_message,
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Inefficiently updates the entire list.
     */
    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged() // Inefficient update
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.message = MessageUiModel.fromMessage(message)
            binding.executePendingBindings()
        }
    }
}
