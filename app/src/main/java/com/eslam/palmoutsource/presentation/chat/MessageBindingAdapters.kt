package com.eslam.palmoutsource.presentation.chat

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.example.app.R

/**
 * Custom binding adapters for message UI components.
 *
 * These adapters handle complex data binding scenarios that can't be
 * expressed directly in XML, such as conditional margin settings.
 */
object MessageBindingAdapters {

    /**
     * Sets margins based on whether the message is from the current user.
     *
     * @param view The view to apply margins to
     * @param isFromCurrentUser Whether the message is from the current user
     */
    @JvmStatic
    @BindingAdapter("messageMargins")
    fun setMessageMargins(view: View, isFromCurrentUser: Boolean) {
        val context = view.context
        val smallMargin = context.resources.getDimensionPixelSize(
            R.dimen.message_margin_small
        )
        val largeMargin = context.resources.getDimensionPixelSize(
            R.dimen.message_margin_large
        )

        val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
        layoutParams?.let { params ->
            if (isFromCurrentUser) {
                // Current user messages: large margin on left, small on right
                params.leftMargin = largeMargin
                params.rightMargin = smallMargin
            } else {
                // Other user messages: small margin on left, large on right  
                params.leftMargin = smallMargin
                params.rightMargin = largeMargin
            }
            view.layoutParams = params
        }
    }

    /**
     * Sets the background drawable based on message sender.
     *
     * @param view The TextView to apply background to
     * @param isFromCurrentUser Whether the message is from current user
     */
    @JvmStatic
    @BindingAdapter("messageBackground")
    fun setMessageBackground(view: View, isFromCurrentUser: Boolean) {
        val backgroundRes = if (isFromCurrentUser) {
            R.drawable.bg_message_sent
        } else {
            R.drawable.bg_message_received
        }
        view.setBackgroundResource(backgroundRes)
    }

    /**
     * Sets text color based on message sender.
     *
     * @param view The TextView to apply color to
     * @param isFromCurrentUser Whether the message is from current user
     */
    @JvmStatic
    @BindingAdapter("messageTextColor")
    fun setMessageTextColor(view: android.widget.TextView, isFromCurrentUser: Boolean) {
        val colorRes = if (isFromCurrentUser) {
            android.R.color.white
        } else {
            android.R.color.black
        }
        view.setTextColor(view.context.getColor(colorRes))
    }
}
