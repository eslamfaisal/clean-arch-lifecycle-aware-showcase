package com.eslam.palmoutsource.data.repository

import com.eslam.palmoutsource.data.datasource.ChatLocalDataSource
import com.eslam.palmoutsource.data.datasource.ChatRemoteDataSource
import com.eslam.palmoutsource.data.mapper.ChatMessageMapper
import com.eslam.palmoutsource.domain.entity.ChatMessage
import com.eslam.palmoutsource.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChatRepository following Clean Architecture.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Implements domain interface (dependency inversion)
 * ✅ Coordinates between data sources
 * ✅ Maps between data and domain models
 * ✅ Handles offline-first strategy
 * 
 * PRODUCTION CONSIDERATIONS:
 * - Offline-first approach with local caching
 * - Error handling and retry logic
 * - Data synchronization between local and remote
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatLocalDataSource,
    private val mapper: ChatMessageMapper
) : ChatRepository {

    override fun observeMessages(): Flow<List<ChatMessage>> {
        return localDataSource.observeMessages()
            .map { dtoList ->
                dtoList.map { dto -> mapper.toDomain(dto) }
            }
    }

    override suspend fun sendMessage(content: String): Result<ChatMessage> {
        return try {
            // Create domain message
            val domainMessage = ChatMessage.createUserMessage(content, com.eslam.palmoutsource.domain.entity.User.CURRENT_USER)

            // Save to local first (offline-first approach)
            val localDto = mapper.toData(domainMessage)
            localDataSource.insertMessage(localDto)
            
            // Try to sync with remote
            try {
                val remoteDto = remoteDataSource.sendMessage(localDto)
                // Update local with remote response if different
                if (remoteDto.id != localDto.id) {
                    localDataSource.updateMessage(remoteDto)
                }
            } catch (e: Exception) {
                // Remote failed, but local save succeeded
                // Mark message as pending sync for later retry
                localDataSource.markAsPendingSync(localDto.id)
            }
            
            Result.success(domainMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadMessages(): Result<List<ChatMessage>> {
        return try {
            // Try remote first for fresh data
            try {
                val remoteMessages = remoteDataSource.getMessages()
                // Cache remote data locally
                localDataSource.insertMessages(remoteMessages)
            } catch (e: Exception) {
                // Remote failed, fall back to local cache
                // This is acceptable for offline-first approach
            }
            
            // Always return from local cache
            val localMessages = localDataSource.getMessages()
            val domainMessages = localMessages.map { dto -> mapper.toDomain(dto) }
            
            Result.success(domainMessages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearMessages(): Result<Unit> {
        return try {
            localDataSource.clearMessages()
            // Note: We don't clear remote messages as they might be needed for other devices
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
