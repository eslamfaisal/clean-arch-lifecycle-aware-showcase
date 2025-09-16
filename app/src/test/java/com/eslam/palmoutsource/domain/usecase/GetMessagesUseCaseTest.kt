package com.eslam.palmoutsource.domain.usecase

import com.eslam.palmoutsource.domain.entity.*
import com.eslam.palmoutsource.domain.repository.ChatRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.Instant

/**
 * Unit tests for GetMessagesUseCase.
 * 
 * Tests Clean Architecture principles:
 * ✅ Use case contains business logic
 * ✅ Depends only on repository interface
 * ✅ Framework independent
 * ✅ Easily testable in isolation
 */
class GetMessagesUseCaseTest {

    @Mock
    private lateinit var mockChatRepository: ChatRepository

    private lateinit var useCase: GetMessagesUseCase

    // Test data
    private val testMessage1 = ChatMessage(
        id = MessageId("1"),
        content = "First message",
        timestamp = Instant.now().minusSeconds(60),
        sender = User.CURRENT_USER,
        messageType = MessageType.TEXT
    )

    private val testMessage2 = ChatMessage(
        id = MessageId("2"),
        content = "Second message",
        timestamp = Instant.now(),
        sender = User.OTHER,
        messageType = MessageType.TEXT
    )

    private val testSystemMessage = ChatMessage(
        id = MessageId("3"),
        content = "System notification",
        timestamp = Instant.now(),
        sender = User.SYSTEM,
        messageType = MessageType.SYSTEM
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetMessagesUseCase(mockChatRepository)
    }

    @Test
    fun `invoke should return messages from repository`() = runTest {
        // Given
        val expectedMessages = listOf(testMessage1, testMessage2)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(expectedMessages))

        // When
        val result = useCase().first()

        // Then
        assertEquals(expectedMessages, result)
        verify(mockChatRepository).observeMessages()
    }

    @Test
    fun `invoke should return empty list when no messages`() = runTest {
        // Given
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(emptyList()))

        // When
        val result = useCase().first()

        // Then
        assertTrue(result.isEmpty())
        verify(mockChatRepository).observeMessages()
    }

    @Test
    fun `invoke should handle different message types`() = runTest {
        // Given
        val mixedMessages = listOf(testMessage1, testSystemMessage, testMessage2)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(mixedMessages))

        // When
        val result = useCase().first()

        // Then
        assertEquals(3, result.size)
        assertEquals(MessageType.TEXT, result[0].messageType)
        assertEquals(MessageType.SYSTEM, result[1].messageType)
        assertEquals(MessageType.TEXT, result[2].messageType)
        verify(mockChatRepository).observeMessages()
    }

    @Test
    fun `invoke should handle different sender types`() = runTest {
        // Given
        val messagesFromDifferentSenders = listOf(
            testMessage1, // CURRENT_USER
            testMessage2, // OTHER
            testSystemMessage // SYSTEM
        )
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(messagesFromDifferentSenders))

        // When
        val result = useCase().first()

        // Then
        assertEquals(3, result.size)
        assertEquals(UserType.CURRENT, result[0].sender.type)
        assertEquals(UserType.OTHER, result[1].sender.type)
        assertEquals(UserType.SYSTEM, result[2].sender.type)
        verify(mockChatRepository).observeMessages()
    }

    @Test
    fun `invoke should preserve message order from repository`() = runTest {
        // Given
        val orderedMessages = listOf(testMessage1, testMessage2, testSystemMessage)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(orderedMessages))

        // When
        val result = useCase().first()

        // Then
        assertEquals(orderedMessages, result)
        assertEquals("First message", result[0].content)
        assertEquals("Second message", result[1].content)
        assertEquals("System notification", result[2].content)
        verify(mockChatRepository).observeMessages()
    }

    @Test
    fun `invoke should call repository only once per invocation`() = runTest {
        // Given
        val messages = listOf(testMessage1)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(messages))

        // When
        useCase().first()

        // Then
        verify(mockChatRepository, times(1)).observeMessages()
    }

    @Test
    fun `invoke should return Flow that can be collected multiple times`() = runTest {
        // Given
        val messages = listOf(testMessage1, testMessage2)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(messages))

        // When
        val result1 = useCase().first()
        val result2 = useCase().first()

        // Then
        assertEquals(result1, result2)
        assertEquals(messages, result1)
        assertEquals(messages, result2)
        // Repository should be called for each invocation
        verify(mockChatRepository, times(2)).observeMessages()
    }

    @Test
    fun `invoke should handle single message correctly`() = runTest {
        // Given
        val singleMessage = listOf(testMessage1)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(singleMessage))

        // When
        val result = useCase().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(testMessage1, result[0])
        verify(mockChatRepository).observeMessages()
    }

    @Test
    fun `invoke should not modify messages from repository`() = runTest {
        // Given
        val originalMessages = listOf(testMessage1, testMessage2)
        whenever(mockChatRepository.observeMessages()).thenReturn(flowOf(originalMessages))

        // When
        val result = useCase().first()

        // Then
        // Verify that the messages are exactly the same (no transformation)
        assertEquals(originalMessages.size, result.size)
        originalMessages.forEachIndexed { index, originalMessage ->
            val resultMessage = result[index]
            assertEquals(originalMessage.id, resultMessage.id)
            assertEquals(originalMessage.content, resultMessage.content)
            assertEquals(originalMessage.timestamp, resultMessage.timestamp)
            assertEquals(originalMessage.sender, resultMessage.sender)
            assertEquals(originalMessage.messageType, resultMessage.messageType)
        }
        verify(mockChatRepository).observeMessages()
    }
}
