package com.eslam.palmoutsource.domain.usecase

import com.eslam.palmoutsource.domain.entity.ChatMessage
import com.eslam.palmoutsource.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for loading initial messages when the chat is first opened.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Separate concern from real-time message observation
 * ✅ Business logic for initial loading
 * ✅ Error handling at domain level
 * ✅ Framework independent
 */
class LoadInitialMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    
    /**
     * Loads initial messages for the chat.
     * 
     * This is typically called once when the chat screen is opened,
     * while GetMessagesUseCase provides ongoing updates.
     * 
     * @return Result with initial messages or error
     */
    suspend operator fun invoke(): Result<List<ChatMessage>> {
        return try {
            chatRepository.loadMessages()
        } catch (e: Exception) {
            Result.failure(ChatLoadException("Failed to load initial messages", e))
        }
    }
}

/**
 * Domain exception for chat loading failures
 */
class ChatLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
