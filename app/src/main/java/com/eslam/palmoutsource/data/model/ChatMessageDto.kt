package com.eslam.palmoutsource.data.model

/**
 * Data Transfer Object for chat messages.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Separate from domain entities
 * ✅ Framework-specific (can include Room/Retrofit annotations)
 * ✅ Optimized for data layer concerns
 * ✅ Isolated from business logic
 * 
 * This represents how data is stored/transmitted, not business concepts.
 */
data class ChatMessageDto(
    val id: String,
    val content: String,
    val timestamp: Long, // Unix timestamp for easy serialization
    val senderId: String,
    val senderName: String,
    val senderType: String, // "current", "other", "system"
    val messageType: String = "text",
    val isPendingSync: Boolean = false // For offline-first functionality
) {
    companion object {
        const val SENDER_TYPE_CURRENT = "current"
        const val SENDER_TYPE_OTHER = "other"
        const val SENDER_TYPE_SYSTEM = "system"
        
        const val MESSAGE_TYPE_TEXT = "text"
        const val MESSAGE_TYPE_SYSTEM = "system"
        const val MESSAGE_TYPE_ERROR = "error"
    }
}
