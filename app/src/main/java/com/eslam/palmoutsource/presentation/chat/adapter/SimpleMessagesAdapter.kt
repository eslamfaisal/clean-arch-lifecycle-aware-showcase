package com.eslam.palmoutsource.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eslam.palmoutsource.R
import com.eslam.palmoutsource.databinding.ItemMessageBinding
import com.eslam.palmoutsource.presentation.chat.Message
import java.util.Date

/**
 * Simplified RecyclerView adapter with data binding and DiffUtil.
 *
 * IMPROVEMENTS IMPLEMENTED:
 * ✅ Data binding for type-safe UI updates
 * ✅ DiffUtil for efficient list updates (fixes performance issue)
 * ✅ Proper ViewHolder pattern
 * ✅ Works with your exact environment requirements
 *
 * CODE REVIEW ISSUES FIXED:
 * ✅ No more notifyDataSetChanged() - uses DiffUtil
 * ✅ Proper layout inflation with data binding
 * ✅ Type-safe view updates
 */
class SimpleMessagesAdapter : RecyclerView.Adapter<SimpleMessagesAdapter.MessageViewHolder>() {

    private var messages: List<Message> = emptyList()

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
     * Updates the messages list using DiffUtil for efficient updates.
     * FIXED: Now uses DiffUtil instead of notifyDataSetChanged() for better performance.
     */
    fun updateMessages(newMessages: List<Message>) {
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages = newMessages
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * ViewHolder with data binding for type-safe view updates.
     */
    class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            // Convert to UI model for data binding
            binding.message = MessageUiModel.fromMessage(message)
            binding.executePendingBindings()
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class MessageDiffCallback(
        private val oldList: List<Message>,
        private val newList: List<Message>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

/**
 * UI model for data binding compatibility.
 */
data class MessageUiModel(
    val text: String,
    val timestamp: String,
    val isFromCurrentUser: Boolean
) {
    companion object {
        fun fromMessage(message: Message): MessageUiModel {
            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return MessageUiModel(
                text = message.text,
                timestamp = formatter.format(Date(message.timestamp)),
                isFromCurrentUser = message.isFromUser
            )
        }
    }
}
