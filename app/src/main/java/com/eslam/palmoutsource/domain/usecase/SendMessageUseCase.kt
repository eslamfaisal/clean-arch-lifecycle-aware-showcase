package com.eslam.palmoutsource.domain.usecase

import com.eslam.palmoutsource.domain.entity.ChatMessage
import com.eslam.palmoutsource.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for sending chat messages.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Business logic validation
 * ✅ Single responsibility
 * ✅ Framework independent
 * ✅ Easily testable
 * 
 * Encapsulates the business rules for sending messages.
 */
class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    
    /**
     * Executes the use case to send a message.
     * 
     * @param content The message content to send
     * @return Result indicating success with the sent message or failure with error
     */
    suspend operator fun invoke(content: String): Result<ChatMessage> {
        // Business rule validation
        if (content.isBlank()) {
            return Result.failure(InvalidMessageException("Message cannot be empty"))
        }
        
        if (content.length > MAX_MESSAGE_LENGTH) {
            return Result.failure(InvalidMessageException("Message too long (max $MAX_MESSAGE_LENGTH characters)"))
        }
        
        // Delegate to repository for actual sending
        return chatRepository.sendMessage(content.trim())
    }
    
    companion object {
        private const val MAX_MESSAGE_LENGTH = 1000
    }
}

/**
 * Domain exception for invalid messages
 */
class InvalidMessageException(message: String) : Exception(message)
