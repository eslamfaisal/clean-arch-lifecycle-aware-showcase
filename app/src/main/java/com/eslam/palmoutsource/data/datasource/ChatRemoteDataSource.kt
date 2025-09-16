package com.eslam.palmoutsource.data.datasource

import com.eslam.palmoutsource.data.model.ChatMessageDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source interface for chat messages.
 * 
 * In a real app, this would be implemented with Retrofit API calls.
 */
interface ChatRemoteDataSource {
    suspend fun getMessages(): List<ChatMessageDto>
    suspend fun sendMessage(message: ChatMessageDto): ChatMessageDto
}

/**
 * Mock implementation of ChatRemoteDataSource.
 * 
 * PRODUCTION NOTE: Replace with Retrofit implementation.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Implements data source interface
 * ✅ Simulates network behavior
 * ✅ Error handling for network failures
 * ✅ Framework-specific concerns isolated
 */
@Singleton
class ChatRemoteDataSourceImpl @Inject constructor() : ChatRemoteDataSource {
    
    private val mockMessages = mutableListOf<ChatMessageDto>()
    
    init {
        // Initialize with some mock data
        mockMessages.addAll(createMockMessages())
    }
    
    override suspend fun getMessages(): List<ChatMessageDto> {
        // Simulate network delay
        delay(1000)
        
        // Simulate occasional network failure
        if (Math.random() < 0.1) { // 10% chance of failure
            throw NetworkException("Failed to fetch messages from server")
        }
        
        return mockMessages.toList()
    }
    
    override suspend fun sendMessage(message: ChatMessageDto): ChatMessageDto {
        // Simulate network delay
        delay(500)
        
        // Simulate occasional network failure
        if (Math.random() < 0.05) { // 5% chance of failure
            throw NetworkException("Failed to send message to server")
        }
        
        // Add to mock server storage
        mockMessages.add(message)
        
        // Simulate auto-reply for demo
        if (mockMessages.size % 2 == 0) {
            delay(1000)
            val autoReply = createAutoReply(message.content)
            mockMessages.add(autoReply)
        }
        
        return message
    }
    
    private fun createMockMessages(): List<ChatMessageDto> {
        val baseTime = System.currentTimeMillis()
        return listOf(
            ChatMessageDto(
                id = "1",
                content = "Hello there! 👋",
                timestamp = baseTime - 300000,
                senderId = "other",
                senderName = "Assistant",
                senderType = ChatMessageDto.SENDER_TYPE_OTHER
            ),
            ChatMessageDto(
                id = "2",
                content = "Hi! How are you doing?",
                timestamp = baseTime - 240000,
                senderId = "current",
                senderName = "You",
                senderType = ChatMessageDto.SENDER_TYPE_CURRENT
            ),
            ChatMessageDto(
                id = "3",
                content = "I'm doing great! Thanks for asking. How can I help you today?",
                timestamp = baseTime - 180000,
                senderId = "other",
                senderName = "Assistant",
                senderType = ChatMessageDto.SENDER_TYPE_OTHER
            )
        )
    }
    
    private fun createAutoReply(userMessage: String): ChatMessageDto {
        val replyContent = when {
            userMessage.lowercase().contains("hello") || userMessage.lowercase().contains("hi") -> 
                "Hello! Nice to hear from you! 😊"
            userMessage.lowercase().contains("how are you") -> 
                "I'm doing well, thank you for asking!"
            userMessage.lowercase().contains("bye") || userMessage.lowercase().contains("goodbye") -> 
                "Goodbye! Have a great day! 👋"
            userMessage.contains("?") -> 
                "That's a great question! Let me think about that..."
            else -> 
                "Thanks for your message! I appreciate you reaching out."
        }
        
        return ChatMessageDto(
            id = (System.currentTimeMillis() + 1).toString(),
            content = replyContent,
            timestamp = System.currentTimeMillis() + 1000,
            senderId = "other",
            senderName = "Assistant",
            senderType = ChatMessageDto.SENDER_TYPE_OTHER
        )
    }
}

/**
 * Network exception for remote data source failures
 */
class NetworkException(message: String) : Exception(message)
