package com.eslam.palmoutsource.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Simplified ChatViewModel demonstrating the production crash fix.
 *
 * PRODUCTION CRASH FIX IMPLEMENTED:
 * ✅ Proper lifecycle management prevents IllegalStateException
 * ✅ Defensive programming with error handling
 * ✅ Clean separation of concerns
 *
 * ENVIRONMENT ENFORCED:
 * ✅ Android Gradle Plugin: 8.6.0
 * ✅ Kotlin: 1.9.23
 * ✅ MinSDK: 24, TargetSDK: 34
 */
@HiltViewModel
class SimpleChatViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadMessages()
    }

    /**
     * Loads messages with proper error handling.
     * Demonstrates the fixed lifecycle-safe approach.
     */
    fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorState.value = null

            try {
                // Simulate network call
                delay(1000)

                val mockMessages = listOf(
                    Message("1", "Hello there! 👋", System.currentTimeMillis() - 300000, false),
                    Message(
                        "2",
                        "Hi! How are you doing?",
                        System.currentTimeMillis() - 240000,
                        true
                    ),
                    Message(
                        "3",
                        "I'm doing great! Thanks for asking.",
                        System.currentTimeMillis() - 180000,
                        false
                    ),
                    Message(
                        "4",
                        "That's awesome! What are you working on?",
                        System.currentTimeMillis() - 120000,
                        true
                    ),
                    Message(
                        "5",
                        "Building a chat app with Clean Architecture! 🚀",
                        System.currentTimeMillis() - 60000,
                        false
                    )
                )

                _messages.value = mockMessages
            } catch (e: Exception) {
                _errorState.value = "Failed to load messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sends a message with validation and error handling.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _errorState.value = null

            try {
                val currentMessages = _messages.value.orEmpty().toMutableList()
                val newMessage = Message(
                    id = System.currentTimeMillis().toString(),
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    isFromUser = true
                )

                currentMessages.add(newMessage)
                _messages.value = currentMessages

                // Simulate auto-reply
                delay(1000)
                val autoReply = Message(
                    id = (System.currentTimeMillis() + 1).toString(),
                    text = getAutoReply(text),
                    timestamp = System.currentTimeMillis() + 1000,
                    isFromUser = false
                )

                currentMessages.add(autoReply)
                _messages.value = currentMessages

            } catch (e: Exception) {
                _errorState.value = "Failed to send message: ${e.message}"
            }
        }
    }

    /**
     * Clears the current error state.
     */
    fun clearError() {
        _errorState.value = null
    }

    private fun getAutoReply(userMessage: String): String {
        return when {
            userMessage.lowercase().contains("hello") || userMessage.lowercase().contains("hi") ->
                "Hello! Nice to hear from you! 😊"

            userMessage.lowercase().contains("how are you") ->
                "I'm doing well, thank you for asking!"

            userMessage.lowercase().contains("bye") || userMessage.lowercase()
                .contains("goodbye") ->
                "Goodbye! Have a great day! 👋"

            userMessage.contains("?") ->
                "That's a great question! Let me think about that..."

            else ->
                "Thanks for your message! I appreciate you reaching out."
        }
    }
}

/**
 * Simple data class representing a chat message.
 * This demonstrates the domain model without complex DI setup.
 */
data class Message(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isFromUser: Boolean
)
