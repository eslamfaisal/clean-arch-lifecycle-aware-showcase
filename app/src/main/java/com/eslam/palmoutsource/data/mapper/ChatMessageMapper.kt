package com.eslam.palmoutsource.data.mapper

import com.eslam.palmoutsource.data.model.ChatMessageDto
import com.eslam.palmoutsource.domain.entity.*
import java.time.Instant
import javax.inject.Inject

/**
 * Mapper between data and domain layers.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Isolates domain from data layer changes
 * ✅ Handles type conversions
 * ✅ Maintains data integrity
 * ✅ Testable transformation logic
 * 
 * This is crucial for maintaining layer independence.
 */
class ChatMessageMapper @Inject constructor() {
    
    /**
     * Converts data layer DTO to domain entity.
     */
    fun toDomain(dto: ChatMessageDto): ChatMessage {
        return ChatMessage(
            id = MessageId(dto.id),
            content = dto.content,
            timestamp = Instant.ofEpochMilli(dto.timestamp),
            sender = mapUser(dto),
            messageType = mapMessageType(dto.messageType)
        )
    }
    
    /**
     * Converts domain entity to data layer DTO.
     */
    fun toData(domain: ChatMessage): ChatMessageDto {
        return ChatMessageDto(
            id = domain.id.value,
            content = domain.content,
            timestamp = domain.timestamp.toEpochMilli(),
            senderId = domain.sender.id,
            senderName = domain.sender.name,
            senderType = mapUserType(domain.sender.type),
            messageType = mapMessageType(domain.messageType),
            isPendingSync = false
        )
    }
    
    /**
     * Maps data layer user info to domain User entity.
     */
    private fun mapUser(dto: ChatMessageDto): User {
        val userType = when (dto.senderType) {
            ChatMessageDto.SENDER_TYPE_CURRENT -> UserType.CURRENT
            ChatMessageDto.SENDER_TYPE_SYSTEM -> UserType.SYSTEM
            ChatMessageDto.SENDER_TYPE_OTHER -> UserType.OTHER
            else -> UserType.OTHER
        }
        
        return User(
            id = dto.senderId,
            name = dto.senderName,
            type = userType
        )
    }
    
    /**
     * Maps domain UserType to data layer string.
     */
    private fun mapUserType(userType: UserType): String {
        return when (userType) {
            UserType.CURRENT -> ChatMessageDto.SENDER_TYPE_CURRENT
            UserType.SYSTEM -> ChatMessageDto.SENDER_TYPE_SYSTEM
            UserType.OTHER -> ChatMessageDto.SENDER_TYPE_OTHER
        }
    }
    
    /**
     * Maps data layer message type string to domain MessageType.
     */
    private fun mapMessageType(messageType: String): MessageType {
        return when (messageType) {
            ChatMessageDto.MESSAGE_TYPE_TEXT -> MessageType.TEXT
            ChatMessageDto.MESSAGE_TYPE_SYSTEM -> MessageType.SYSTEM
            ChatMessageDto.MESSAGE_TYPE_ERROR -> MessageType.ERROR
            else -> MessageType.TEXT
        }
    }
    
    /**
     * Maps domain MessageType to data layer string.
     */
    private fun mapMessageType(messageType: MessageType): String {
        return when (messageType) {
            MessageType.TEXT -> ChatMessageDto.MESSAGE_TYPE_TEXT
            MessageType.SYSTEM -> ChatMessageDto.MESSAGE_TYPE_SYSTEM
            MessageType.ERROR -> ChatMessageDto.MESSAGE_TYPE_ERROR
        }
    }
}
