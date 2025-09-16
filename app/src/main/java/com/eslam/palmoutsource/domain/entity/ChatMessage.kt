package com.eslam.palmoutsource.domain.entity

import java.time.Instant

/**
 * Domain entity representing a chat message.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Pure Kotlin - no Android dependencies
 * ✅ Immutable data class
 * ✅ Business logic focused
 * ✅ Framework independent
 * 
 * This is the core business model that all layers depend on.
 */
data class ChatMessage(
    val id: MessageId,
    val content: String,
    val timestamp: Instant,
    val sender: User,
    val messageType: MessageType = MessageType.TEXT
) {
    companion object {
        fun createUserMessage(content: String, user: User): ChatMessage {
            return ChatMessage(
                id = MessageId.generate(),
                content = content,
                timestamp = Instant.now(),
                sender = user,
                messageType = MessageType.TEXT
            )
        }
        
        fun createSystemMessage(content: String): ChatMessage {
            return ChatMessage(
                id = MessageId.generate(),
                content = content,
                timestamp = Instant.now(),
                sender = User.SYSTEM,
                messageType = MessageType.SYSTEM
            )
        }
    }
}

/**
 * Value object for message identification
 */
@JvmInline
value class MessageId(val value: String) {
    companion object {
        fun generate(): MessageId = MessageId(System.currentTimeMillis().toString())
    }
}

/**
 * Domain entity representing a user
 */
data class User(
    val id: String,
    val name: String,
    val type: UserType
) {
    companion object {
        val CURRENT_USER = User("current", "You", UserType.CURRENT)
        val SYSTEM = User("system", "System", UserType.SYSTEM)
        val OTHER = User("other", "Other", UserType.OTHER)
    }
}

/**
 * Enum for user types
 */
enum class UserType {
    CURRENT,
    OTHER,
    SYSTEM
}

/**
 * Enum for message types
 */
enum class MessageType {
    TEXT,
    SYSTEM,
    ERROR
}
