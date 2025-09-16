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
 * Unit tests for SendMessageUseCase.
 * 
 * Tests Clean Architecture principles:
 * ✅ Business logic validation in use case
 * ✅ Single responsibility principle
 * ✅ Framework independent testing
 * ✅ Proper error handling
 */
class SendMessageUseCaseTest {

    @Mock
    private lateinit var mockChatRepository: ChatRepository

    private lateinit var useCase: SendMessageUseCase

    // Test data
    private val testMessage = ChatMessage(
        id = MessageId("test-id"),
        content = "Test message",
        timestamp = Instant.now(),
        sender = User.CURRENT_USER,
        messageType = MessageType.TEXT
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = SendMessageUseCase(mockChatRepository)
    }

    @Test
    fun `invoke should send valid message successfully`() = runTest {
        // Given
        val content = "Valid message"
        whenever(mockChatRepository.sendMessage(content)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(content)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testMessage, result.getOrNull())
        verify(mockChatRepository).sendMessage(content)
    }

    @Test
    fun `invoke should trim whitespace and send message`() = runTest {
        // Given
        val contentWithWhitespace = "  Valid message  "
        val trimmedContent = "Valid message"
        whenever(mockChatRepository.sendMessage(trimmedContent)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(contentWithWhitespace)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(trimmedContent)
    }

    @Test
    fun `invoke should reject empty message`() = runTest {
        // Given
        val emptyContent = ""

        // When
        val result = useCase(emptyContent)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is InvalidMessageException)
        assertEquals("Message cannot be empty", exception?.message)
        verifyNoInteractions(mockChatRepository)
    }

    @Test
    fun `invoke should reject blank message`() = runTest {
        // Given
        val blankContent = "   "

        // When
        val result = useCase(blankContent)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is InvalidMessageException)
        assertEquals("Message cannot be empty", exception?.message)
        verifyNoInteractions(mockChatRepository)
    }

    @Test
    fun `invoke should reject message that is too long`() = runTest {
        // Given
        val longContent = "a".repeat(1001) // MAX_MESSAGE_LENGTH is 1000

        // When
        val result = useCase(longContent)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is InvalidMessageException)
        assertEquals("Message too long (max 1000 characters)", exception?.message)
        verifyNoInteractions(mockChatRepository)
    }

    @Test
    fun `invoke should accept message at maximum length`() = runTest {
        // Given
        val maxLengthContent = "a".repeat(1000) // Exactly MAX_MESSAGE_LENGTH
        whenever(mockChatRepository.sendMessage(maxLengthContent)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(maxLengthContent)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(maxLengthContent)
    }

    @Test
    fun `invoke should handle repository failure`() = runTest {
        // Given
        val content = "Valid message"
        val repositoryException = RuntimeException("Network error")
        whenever(mockChatRepository.sendMessage(content)).thenReturn(Result.failure(repositoryException))

        // When
        val result = useCase(content)

        // Then
        assertTrue(result.isFailure)
        assertEquals(repositoryException, result.exceptionOrNull())
        verify(mockChatRepository).sendMessage(content)
    }

    @Test
    fun `invoke should handle newlines in message`() = runTest {
        // Given
        val contentWithNewlines = "Line 1\nLine 2\nLine 3"
        whenever(mockChatRepository.sendMessage(contentWithNewlines)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(contentWithNewlines)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(contentWithNewlines)
    }

    @Test
    fun `invoke should handle special characters in message`() = runTest {
        // Given
        val contentWithSpecialChars = "Hello! @#$%^&*()_+-={}[]|\\:;\"'<>?,./"
        whenever(mockChatRepository.sendMessage(contentWithSpecialChars)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(contentWithSpecialChars)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(contentWithSpecialChars)
    }

    @Test
    fun `invoke should handle unicode characters in message`() = runTest {
        // Given
        val contentWithUnicode = "Hello 👋 World 🌍 Emoji 😀"
        whenever(mockChatRepository.sendMessage(contentWithUnicode)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(contentWithUnicode)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(contentWithUnicode)
    }

    @Test
    fun `invoke should validate message length after trimming`() = runTest {
        // Given - Create content that becomes empty after trimming
        val onlyWhitespaceContent = "   \n\t   "

        // When
        val result = useCase(onlyWhitespaceContent)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is InvalidMessageException)
        assertEquals("Message cannot be empty", exception?.message)
        verifyNoInteractions(mockChatRepository)
    }

    @Test
    fun `invoke should validate trimmed message length for max length`() = runTest {
        // Given - Create content that exceeds max length after trimming
        val contentWithPadding = "   " + "a".repeat(1001) + "   "
        
        // When
        val result = useCase(contentWithPadding)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is InvalidMessageException)
        assertEquals("Message too long (max 1000 characters)", exception?.message)
        verifyNoInteractions(mockChatRepository)
    }

    @Test
    fun `invoke should accept single character message`() = runTest {
        // Given
        val singleCharContent = "a"
        whenever(mockChatRepository.sendMessage(singleCharContent)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(singleCharContent)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(singleCharContent)
    }

    @Test
    fun `invoke should handle tabs and other whitespace characters`() = runTest {
        // Given
        val contentWithTabs = "\t\tValid message\t\t"
        val trimmedContent = "Valid message"
        whenever(mockChatRepository.sendMessage(trimmedContent)).thenReturn(Result.success(testMessage))

        // When
        val result = useCase(contentWithTabs)

        // Then
        assertTrue(result.isSuccess)
        verify(mockChatRepository).sendMessage(trimmedContent)
    }

    @Test
    fun `invoke should call repository exactly once for valid message`() = runTest {
        // Given
        val content = "Valid message"
        whenever(mockChatRepository.sendMessage(content)).thenReturn(Result.success(testMessage))

        // When
        useCase(content)

        // Then
        verify(mockChatRepository, times(1)).sendMessage(content)
    }
}
