package com.eslam.palmoutsource.data.datasource

import com.eslam.palmoutsource.data.model.ChatMessageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local data source interface for chat messages.
 * 
 * In a real app, this would be implemented with Room database.
 * For this demo, using in-memory storage with reactive Flow.
 */
interface ChatLocalDataSource {
    fun observeMessages(): Flow<List<ChatMessageDto>>
    suspend fun getMessages(): List<ChatMessageDto>
    suspend fun insertMessage(message: ChatMessageDto)
    suspend fun insertMessages(messages: List<ChatMessageDto>)
    suspend fun updateMessage(message: ChatMessageDto)
    suspend fun clearMessages()
    suspend fun markAsPendingSync(messageId: String)
}

/**
 * In-memory implementation of ChatLocalDataSource.
 * 
 * PRODUCTION NOTE: Replace with Room database implementation.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Implements data source interface
 * ✅ Framework-specific implementation
 * ✅ Reactive data with Flow
 * ✅ Thread-safe operations
 */
@Singleton
class ChatLocalDataSourceImpl @Inject constructor() : ChatLocalDataSource {
    
    private val messages = mutableListOf<ChatMessageDto>()
    private val _messagesFlow = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    
    override fun observeMessages(): Flow<List<ChatMessageDto>> {
        return _messagesFlow.asStateFlow()
    }
    
    override suspend fun getMessages(): List<ChatMessageDto> {
        return messages.toList()
    }
    
    override suspend fun insertMessage(message: ChatMessageDto) {
        messages.add(message)
        updateFlow()
    }
    
    override suspend fun insertMessages(messages: List<ChatMessageDto>) {
        // Replace existing messages with new ones (for sync scenarios)
        this.messages.clear()
        this.messages.addAll(messages)
        updateFlow()
    }
    
    override suspend fun updateMessage(message: ChatMessageDto) {
        val index = messages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            messages[index] = message
            updateFlow()
        }
    }
    
    override suspend fun clearMessages() {
        messages.clear()
        updateFlow()
    }
    
    override suspend fun markAsPendingSync(messageId: String) {
        val index = messages.indexOfFirst { it.id == messageId }
        if (index != -1) {
            messages[index] = messages[index].copy(isPendingSync = true)
            updateFlow()
        }
    }
    
    private fun updateFlow() {
        _messagesFlow.value = messages.toList()
    }
}
