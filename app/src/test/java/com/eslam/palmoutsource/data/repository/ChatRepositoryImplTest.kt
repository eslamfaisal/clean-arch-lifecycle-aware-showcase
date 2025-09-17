package com.eslam.palmoutsource.data.repository

import com.eslam.palmoutsource.data.datasource.ChatLocalDataSource
import com.eslam.palmoutsource.data.datasource.ChatRemoteDataSource
import com.eslam.palmoutsource.data.datasource.NetworkException
import com.eslam.palmoutsource.data.mapper.ChatMessageMapper
import com.eslam.palmoutsource.data.model.ChatMessageDto
import com.eslam.palmoutsource.domain.entity.ChatMessage
import com.eslam.palmoutsource.domain.entity.MessageId
import com.eslam.palmoutsource.domain.entity.MessageType
import com.eslam.palmoutsource.domain.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant

/**
 * Unit tests for ChatRepositoryImpl.
 *
 * Tests follow Clean Architecture principles:
 * ✅ Test business logic in isolation
 * ✅ Mock external dependencies
 * ✅ Verify interactions with data sources
 * ✅ Test error handling scenarios
 */
class ChatRepositoryImplTest {

    @Mock
    private lateinit var mockRemoteDataSource: ChatRemoteDataSource

    @Mock
    private lateinit var mockLocalDataSource: ChatLocalDataSource

    // Use a real instance of the mapper instead of a mock
    private lateinit var mapper: ChatMessageMapper

    private lateinit var repository: ChatRepositoryImpl

    // Test data
    private val testMessageDto = ChatMessageDto(
        id = "test-id",
        content = "Test message",
        timestamp = System.currentTimeMillis(),
        senderId = "current",
        senderName = "You",
        senderType = ChatMessageDto.SENDER_TYPE_CURRENT,
        messageType = ChatMessageDto.MESSAGE_TYPE_TEXT,
        isPendingSync = false
    )

    private val testDomainMessage = ChatMessage(
        id = MessageId("test-id"),
        content = "Test message",
        timestamp = Instant.ofEpochMilli(testMessageDto.timestamp),
        sender = User.CURRENT_USER,
        messageType = MessageType.TEXT
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Instantiate the real mapper
        mapper = ChatMessageMapper()
        repository = ChatRepositoryImpl(
            remoteDataSource = mockRemoteDataSource,
            localDataSource = mockLocalDataSource,
            mapper = mapper // Pass the real instance
        )
    }

    @Test
    fun `observeMessages should return mapped domain messages from local data source`() = runTest {
        // Given
        val dtoList = listOf(testMessageDto)
        val expectedDomainList = listOf(testDomainMessage)

        whenever(mockLocalDataSource.observeMessages()).thenReturn(flowOf(dtoList))

        // When
        val result = repository.observeMessages().first()

        // Then
        assertEquals(expectedDomainList.size, result.size)
        assertEquals(expectedDomainList.first().id, result.first().id)
        assertEquals(expectedDomainList.first().content, result.first().content)
        verify(mockLocalDataSource).observeMessages()
    }

    @Test
    fun `observeMessages should handle empty list`() = runTest {
        // Given
        whenever(mockLocalDataSource.observeMessages()).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.observeMessages().first()

        // Then
        assertTrue(result.isEmpty())
        verify(mockLocalDataSource).observeMessages()
    }

    @Test
    fun `sendMessage should save to local first and sync with remote successfully`() = runTest {
        // Given
        val content = "Test message"
        val remoteDtoWithNewId = testMessageDto.copy(id = "remote-id")

        // The repository creates a message, maps it, then sends it.
        // We need to capture the DTO passed to the remote data source.
        val captor = argumentCaptor<ChatMessageDto>()
        whenever(mockRemoteDataSource.sendMessage(captor.capture())).thenReturn(remoteDtoWithNewId)

        // When
        val result = repository.sendMessage(content)

        // Then
        assertTrue(result.isSuccess)
        // Verify a message was inserted locally first
        verify(mockLocalDataSource).insertMessage(captor.firstValue)
        // Verify the same message was sent to remote
        verify(mockRemoteDataSource).sendMessage(captor.firstValue)
        // Verify the local message was updated with the new ID from remote
        verify(mockLocalDataSource).updateMessage(remoteDtoWithNewId)
        verify(mockLocalDataSource, never()).markAsPendingSync(any())
    }

    @Test
    fun `sendMessage should handle remote failure gracefully and mark as pending sync`() = runTest {
        // Given
        val content = "Test message"
        val captor = argumentCaptor<ChatMessageDto>()
        
        // Mock the remote data source to throw an exception
        whenever(mockRemoteDataSource.sendMessage(any())).thenAnswer { 
            throw NetworkException("Network error")
        }

        // When
        val result = repository.sendMessage(content)

        // Then
        assertTrue(result.isSuccess) // Sending is successful from the user's perspective
        verify(mockLocalDataSource).insertMessage(captor.capture())
        verify(mockRemoteDataSource).sendMessage(captor.firstValue)
        verify(mockLocalDataSource).markAsPendingSync(captor.firstValue.id)
        verify(mockLocalDataSource, never()).updateMessage(any())
    }

    @Test
    fun `sendMessage should return failure when local save fails`() = runTest {
        // Given
        val content = "Test message"
        val exception = RuntimeException("Local storage error")
        whenever(mockLocalDataSource.insertMessage(any())).thenThrow(exception)

        // When
        val result = repository.sendMessage(content)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockLocalDataSource).insertMessage(any())
        verifyNoInteractions(mockRemoteDataSource)
    }

    @Test
    fun `sendMessage should not update local when remote returns same id`() = runTest {
        // Given
        val content = "Test message"
        val captor = argumentCaptor<ChatMessageDto>()
        // Remote returns the exact same object that was sent
        whenever(mockRemoteDataSource.sendMessage(captor.capture())).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        // When
        val result = repository.sendMessage(content)

        // Then
        assertTrue(result.isSuccess)
        verify(mockLocalDataSource).insertMessage(captor.firstValue)
        verify(mockRemoteDataSource).sendMessage(captor.firstValue)
        verify(mockLocalDataSource, never()).updateMessage(any())
    }

    @Test
    fun `loadMessages should fetch from remote and cache locally`() = runTest {
        // Given
        val remoteDtoList = listOf(testMessageDto)
        val expectedDomainList = listOf(testDomainMessage)

        whenever(mockRemoteDataSource.getMessages()).thenReturn(remoteDtoList)
        whenever(mockLocalDataSource.getMessages()).thenReturn(remoteDtoList)

        // When
        val result = repository.loadMessages()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDomainList.size, result.getOrNull()?.size)
        assertEquals(expectedDomainList.first().id, result.getOrNull()?.first()?.id)
        verify(mockRemoteDataSource).getMessages()
        verify(mockLocalDataSource).insertMessages(remoteDtoList)
        verify(mockLocalDataSource).getMessages()
    }

    @Test
    fun `loadMessages should fallback to local when remote fails`() = runTest {
        // Given
        val localDtoList = listOf(testMessageDto)
        val expectedDomainList = listOf(testDomainMessage)

        whenever(mockRemoteDataSource.getMessages()).thenAnswer { 
            throw NetworkException("Network error")
        }
        whenever(mockLocalDataSource.getMessages()).thenReturn(localDtoList)

        // When
        val result = repository.loadMessages()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDomainList.first().id, result.getOrNull()?.first()?.id)
        verify(mockRemoteDataSource).getMessages()
        verify(mockLocalDataSource, never()).insertMessages(any())
        verify(mockLocalDataSource).getMessages()
    }

    @Test
    fun `loadMessages should return failure when both remote and local fail`() = runTest {
        // Given
        val exception = RuntimeException("Storage error")

        whenever(mockRemoteDataSource.getMessages()).thenAnswer { 
            throw NetworkException("Network error")
        }
        whenever(mockLocalDataSource.getMessages()).thenThrow(exception)

        // When
        val result = repository.loadMessages()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRemoteDataSource).getMessages()
        verify(mockLocalDataSource).getMessages()
    }

    @Test
    fun `clearMessages should clear local messages successfully`() = runTest {
        // When
        val result = repository.clearMessages()

        // Then
        assertTrue(result.isSuccess)
        verify(mockLocalDataSource).clearMessages()
        verifyNoInteractions(mockRemoteDataSource)
    }

    @Test
    fun `clearMessages should return failure when local clear fails`() = runTest {
        // Given
        val exception = RuntimeException("Clear failed")
        whenever(mockLocalDataSource.clearMessages()).thenThrow(exception)

        // When
        val result = repository.clearMessages()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockLocalDataSource).clearMessages()
    }

    // The following tests are now implicitly covered by other tests,
    // as we are using the real mapper. You could remove them or keep them
    // as explicit checks on the input to the data layer.
    @Test
    fun `sendMessage should create and map a user message with correct properties`() = runTest {
        // Given
        val content = "Test message"
        val captor = argumentCaptor<ChatMessageDto>()

        // When
        repository.sendMessage(content)

        // Then
        verify(mockLocalDataSource).insertMessage(captor.capture())
        val mappedDto = captor.firstValue

        assertEquals(content, mappedDto.content)
        assertEquals(ChatMessageDto.SENDER_TYPE_CURRENT, mappedDto.senderType)
        assertEquals(ChatMessageDto.MESSAGE_TYPE_TEXT, mappedDto.messageType)
    }

    @Test
    fun `sendMessage should trim whitespace from content before mapping`() = runTest {
        // Given
        val content = "  Test message  "
        val trimmedContent = "Test message"
        val captor = argumentCaptor<ChatMessageDto>()

        // When
        repository.sendMessage(content)

        // Then
        verify(mockLocalDataSource).insertMessage(captor.capture())
        val mappedDto = captor.firstValue

        assertEquals(trimmedContent, mappedDto.content)
    }
}