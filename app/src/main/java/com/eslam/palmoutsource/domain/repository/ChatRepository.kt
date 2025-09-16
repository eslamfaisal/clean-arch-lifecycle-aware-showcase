package com.eslam.palmoutsource.domain.repository

import com.eslam.palmoutsource.domain.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface - Clean Architecture contract.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Interface in domain layer
 * ✅ Implementation in data layer (dependency inversion)
 * ✅ Framework agnostic
 * ✅ Testable through mocking
 * 
 * This defines WHAT the app can do, not HOW it does it.
 */
interface ChatRepository {
    /**
     * Observes chat messages as a reactive stream.
     * Returns Flow for modern reactive programming.
     */
    fun observeMessages(): Flow<List<ChatMessage>>
    
    /**
     * Sends a new message.
     * @param content The message content to send
     * @return Result indicating success or failure
     */
    suspend fun sendMessage(content: String): Result<ChatMessage>
    
    /**
     * Loads initial messages from data source.
     * @return Result with list of messages or error
     */
    suspend fun loadMessages(): Result<List<ChatMessage>>
    
    /**
     * Clears all messages (useful for testing or reset functionality).
     */
    suspend fun clearMessages(): Result<Unit>
}
