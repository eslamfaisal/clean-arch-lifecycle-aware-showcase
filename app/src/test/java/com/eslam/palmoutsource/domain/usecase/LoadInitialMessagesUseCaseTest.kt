package com.eslam.palmoutsource.domain.usecase

import com.eslam.palmoutsource.domain.entity.*
import com.eslam.palmoutsource.domain.repository.ChatRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.Instant

/**
 * Unit tests for LoadInitialMessagesUseCase.
 * 
 * Tests Clean Architecture principles:
 * ✅ Business logic for initial loading
 * ✅ Error handling at domain level
 * ✅ Framework independent
 * ✅ Proper exception wrapping
 */
class LoadInitialMessagesUseCaseTest {

    @Mock
    private lateinit var mockChatRepository: ChatRepository

    private lateinit var useCase: LoadInitialMessagesUseCase

    // Test data
    private val testMessage1 = ChatMessage(
        id = MessageId("1"),
        content = "First message",
        timestamp = Instant.now().minusSeconds(120),
        sender = User.CURRENT_USER,
        messageType = MessageType.TEXT
    )

    private val testMessage2 = ChatMessage(
        id = MessageId("2"),
        content = "Second message",
        timestamp = Instant.now().minusSeconds(60),
        sender = User.OTHER,
        messageType = MessageType.TEXT
    )

    private val testSystemMessage = ChatMessage(
        id = MessageId("3"),
        content = "Welcome to the chat",
        timestamp = Instant.now(),
        sender = User.SYSTEM,
        messageType = MessageType.SYSTEM
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadInitialMessagesUseCase(mockChatRepository)
    }

    @Test
    fun `invoke should return messages from repository successfully`() = runTest {
        // Given
        val expectedMessages = listOf(testMessage1, testMessage2, testSystemMessage)
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(expectedMessages))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMessages, result.getOrNull())
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should return empty list when no messages exist`() = runTest {
        // Given
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(emptyList()))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        assertTrue(messages!!.isEmpty())
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should handle single message correctly`() = runTest {
        // Given
        val singleMessage = listOf(testMessage1)
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(singleMessage))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        assertEquals(1, messages!!.size)
        assertEquals(testMessage1, messages[0])
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should return repository failure directly`() = runTest {
        // Given
        val repositoryException = RuntimeException("Network error")
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.failure(repositoryException))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertEquals(repositoryException, exception)
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should handle repository throwing exception and wrap in ChatLoadException`() = runTest {
        // Given
        val repositoryException = RuntimeException("Unexpected error")
        whenever(mockChatRepository.loadMessages()).thenThrow(repositoryException)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is ChatLoadException)
        assertEquals("Failed to load initial messages", exception?.message)
        assertEquals(repositoryException, exception?.cause)
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should preserve message order from repository`() = runTest {
        // Given
        val orderedMessages = listOf(testMessage1, testMessage2, testSystemMessage)
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(orderedMessages))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        assertEquals(orderedMessages, messages)
        assertEquals("First message", messages!![0].content)
        assertEquals("Second message", messages[1].content)
        assertEquals("Welcome to the chat", messages[2].content)
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should handle different message types correctly`() = runTest {
        // Given
        val mixedMessages = listOf(
            testMessage1, // TEXT
            testSystemMessage, // SYSTEM
            testMessage2 // TEXT
        )
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(mixedMessages))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        assertEquals(3, messages!!.size)
        assertEquals(MessageType.TEXT, messages[0].messageType)
        assertEquals(MessageType.SYSTEM, messages[1].messageType)
        assertEquals(MessageType.TEXT, messages[2].messageType)
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should handle different sender types correctly`() = runTest {
        // Given
        val messagesFromDifferentSenders = listOf(
            testMessage1, // CURRENT_USER
            testMessage2, // OTHER
            testSystemMessage // SYSTEM
        )
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(messagesFromDifferentSenders))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        assertEquals(3, messages!!.size)
        assertEquals(UserType.CURRENT, messages[0].sender.type)
        assertEquals(UserType.OTHER, messages[1].sender.type)
        assertEquals(UserType.SYSTEM, messages[2].sender.type)
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should call repository exactly once`() = runTest {
        // Given
        val messages = listOf(testMessage1)
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(messages))

        // When
        useCase()

        // Then
        verify(mockChatRepository, times(1)).loadMessages()
    }

    @Test
    fun `invoke should not modify messages from repository`() = runTest {
        // Given
        val originalMessages = listOf(testMessage1, testMessage2)
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(originalMessages))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        
        // Verify that the messages are exactly the same (no transformation)
        assertEquals(originalMessages.size, messages!!.size)
        originalMessages.forEachIndexed { index, originalMessage ->
            val resultMessage = messages[index]
            assertEquals(originalMessage.id, resultMessage.id)
            assertEquals(originalMessage.content, resultMessage.content)
            assertEquals(originalMessage.timestamp, resultMessage.timestamp)
            assertEquals(originalMessage.sender, resultMessage.sender)
            assertEquals(originalMessage.messageType, resultMessage.messageType)
        }
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `invoke should handle large number of messages`() = runTest {
        // Given
        val largeMessageList = (1..100).map { index ->
            ChatMessage(
                id = MessageId(index.toString()),
                content = "Message $index",
                timestamp = Instant.now().minusSeconds(index.toLong()),
                sender = if (index % 2 == 0) User.CURRENT_USER else User.OTHER,
                messageType = MessageType.TEXT
            )
        }
        whenever(mockChatRepository.loadMessages()).thenReturn(Result.success(largeMessageList))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val messages = result.getOrNull()
        assertNotNull(messages)
        assertEquals(100, messages!!.size)
        assertEquals("Message 1", messages[0].content)
        assertEquals("Message 100", messages[99].content)
        verify(mockChatRepository).loadMessages()
    }

    @Test
    fun `ChatLoadException should have correct properties`() {
        // Given
        val message = "Test error message"
        val cause = RuntimeException("Original cause")

        // When
        val exception = ChatLoadException(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `ChatLoadException should work without cause`() {
        // Given
        val message = "Test error message"

        // When
        val exception = ChatLoadException(message)

        // Then
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }
}
