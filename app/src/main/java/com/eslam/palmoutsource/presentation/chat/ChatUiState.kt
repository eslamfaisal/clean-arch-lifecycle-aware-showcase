package com.eslam.palmoutsource.presentation.chat

import com.eslam.palmoutsource.domain.entity.ChatMessage

/**
 * UI State for the Chat screen following modern Android architecture.
 *
 * CLEAN ARCHITECTURE + MVVM PRINCIPLES:
 * ✅ Immutable state representation
 * ✅ Sealed class for type safety
 * ✅ Single source of truth
 * ✅ Framework agnostic (can be used in Compose or View system)
 *
 * This represents what the UI should display at any given moment.
 */
sealed class ChatUiState {
    /**
     * Initial state when chat is starting up
     */
    object Loading : ChatUiState()

    /**
     * Chat is loaded and ready for interaction
     */
    data class Success(
        val messages: List<ChatMessage>,
        val isRefreshing: Boolean = false,
        val isSendingMessage: Boolean = false
    ) : ChatUiState()

    /**
     * Error state with retry capability
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : ChatUiState()
}

/**
 * UI Events that the user can trigger.
 *
 * MVVM PRINCIPLES:
 * ✅ Represents user intentions
 * ✅ Decouples UI from business logic
 * ✅ Testable user interactions
 * ✅ Clear action semantics
 */
sealed class ChatUiEvent {
    /**
     * User wants to send a message
     */
    data class SendMessage(val content: String) : ChatUiEvent()

    /**
     * User wants to refresh the chat
     */
    object RefreshMessages : ChatUiEvent()

    /**
     * User wants to retry after an error
     */
    object RetryLastAction : ChatUiEvent()

    /**
     * User dismissed the error message
     */
    object DismissError : ChatUiEvent()
}

/**
 * UI Effects for one-time events (like showing snackbars, navigation).
 *
 * These are consumed once and don't persist in the state.
 */
sealed class ChatUiEffect {
    /**
     * Show a temporary message to the user
     */
    data class ShowSnackbar(val message: String) : ChatUiEffect()

    /**
     * Scroll to the bottom of the messages list
     */
    object ScrollToBottom : ChatUiEffect()

    /**
     * Clear the message input field
     */
    object ClearInput : ChatUiEffect()
}
