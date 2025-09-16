package com.eslam.palmoutsource.domain.usecase

import com.eslam.palmoutsource.domain.entity.ChatMessage
import com.eslam.palmoutsource.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing chat messages.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Single responsibility - only handles message retrieval
 * ✅ Depends on abstractions (repository interface)
 * ✅ Business logic encapsulation
 * ✅ Testable in isolation
 * 
 * Use cases represent the business operations the application can perform.
 */
class GetMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    /**
     * Executes the use case to get messages as a reactive stream.
     * 
     * @return Flow of message lists that updates when new messages arrive
     */
    operator fun invoke(): Flow<List<ChatMessage>> {
        return chatRepository.observeMessages()
    }
}
