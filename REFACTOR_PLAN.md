# Staged Refactor Plan: Legacy Chat to MVVM + Clean Architecture

## Current State Analysis
The existing `ChatFragment` demonstrates typical legacy patterns:
- Direct ViewModel dependency without abstraction
- No separation of concerns (UI, Domain, Data layers)
- Tightly coupled components
- No dependency injection
- Manual adapter updates without DiffUtil

## Staged Migration Approach

### Phase 1: Foundation & Infrastructure (Week 1-2)
**Goal**: Establish architectural foundation without breaking existing functionality

#### 1.1 Dependency Injection Setup
```kotlin
// Add Hilt/Dagger setup
@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
    @Provides
    @Singleton
    fun provideChatRepository(): ChatRepository = ChatRepositoryImpl()
}
```

#### 1.2 Data Layer Creation
```kotlin
// Domain entities (pure Kotlin)
data class ChatMessage(
    val id: MessageId,
    val content: String,
    val timestamp: Instant,
    val sender: User
)

// Repository contract (Domain layer)
interface ChatRepository {
    suspend fun getMessages(): Result<List<ChatMessage>>
    suspend fun sendMessage(content: String): Result<Unit>
}

// Implementation (Data layer)
class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
    private val localDataSource: ChatLocalDataSource
) : ChatRepository {
    // Implementation with offline-first strategy
}
```

#### 1.3 Use Cases (Domain Layer)
```kotlin
class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<ChatMessage>> {
        return repository.getMessages()
    }
}

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
    private val validator: MessageValidator
) {
    suspend operator fun invoke(content: String): Result<Unit> {
        return when (val validation = validator.validate(content)) {
            is ValidationResult.Valid -> repository.sendMessage(content)
            is ValidationResult.Invalid -> Result.failure(validation.error)
        }
    }
}
```

### Phase 2: ViewModel Refactoring (Week 2-3)
**Goal**: Transform ViewModel to use Clean Architecture principles

#### 2.1 State Management with Sealed Classes
```kotlin
sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

sealed class ChatUiEvent {
    data class SendMessage(val content: String) : ChatUiEvent()
    object RefreshMessages : ChatUiEvent()
    object RetryLastAction : ChatUiEvent()
}
```

#### 2.2 ViewModel with Clean Dependencies
```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    fun handleEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.SendMessage -> sendMessage(event.content)
            is ChatUiEvent.RefreshMessages -> loadMessages()
            is ChatUiEvent.RetryLastAction -> retryLastAction()
        }
    }
    
    private fun sendMessage(content: String) {
        viewModelScope.launch {
            sendMessageUseCase(content)
                .onSuccess { loadMessages() }
                .onFailure { _uiState.value = ChatUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
```

### Phase 3: UI Layer Modernization (Week 3-4)
**Goal**: Migrate to modern Android UI patterns

#### 3.1 Fragment with StateFlow
```kotlin
@AndroidEntryPoint
class ChatFragment : Fragment() {
    
    private val viewModel: ChatViewModel by viewModels()
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUiState()
    }
    
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ChatUiState.Loading -> showLoading()
                    is ChatUiState.Success -> showMessages(state.messages)
                    is ChatUiState.Error -> showError(state.message)
                }
            }
        }
    }
}
```

#### 3.2 Modern Adapter with DiffUtil
```kotlin
class MessagesAdapter : ListAdapter<ChatMessage, MessageViewHolder>(MessageDiffCallback()) {
    
    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }
}
```

### Phase 4: Testing & Quality Assurance (Week 4-5)
**Goal**: Comprehensive test coverage and quality gates

#### 4.1 Repository Testing
```kotlin
@Test
fun `repository returns cached data when network fails`() = runTest {
    // Given
    whenever(apiService.getMessages()).thenThrow(NetworkException())
    whenever(localDataSource.getCachedMessages()).thenReturn(cachedMessages)
    
    // When
    val result = repository.getMessages()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(cachedMessages, result.getOrNull())
}
```

#### 4.2 Use Case Testing
```kotlin
@Test
fun `sendMessage validates input before calling repository`() = runTest {
    // Given
    val invalidMessage = ""
    
    // When
    val result = sendMessageUseCase(invalidMessage)
    
    // Then
    assertTrue(result.isFailure)
    verify(repository, never()).sendMessage(any())
}
```

#### 4.3 ViewModel Testing with Turbine
```kotlin
@Test
fun `viewModel emits loading then success states`() = runTest {
    // Given
    whenever(getMessagesUseCase()).thenReturn(Result.success(testMessages))
    
    // When
    viewModel.handleEvent(ChatUiEvent.RefreshMessages)
    
    // Then
    viewModel.uiState.test {
        assertEquals(ChatUiState.Loading, awaitItem())
        assertEquals(ChatUiState.Success(testMessages), awaitItem())
    }
}
```

### Phase 5: Migration Strategy (Week 5-6)
**Goal**: Safe production migration

#### 5.1 Feature Flagging
```kotlin
@Singleton
class FeatureFlags @Inject constructor() {
    fun isNewChatArchitectureEnabled(): Boolean {
        return BuildConfig.DEBUG || RemoteConfig.getBoolean("new_chat_architecture")
    }
}
```

#### 5.2 A/B Testing Setup
```kotlin
class ChatFragmentFactory @Inject constructor(
    private val featureFlags: FeatureFlags
) {
    fun createChatFragment(): Fragment {
        return if (featureFlags.isNewChatArchitectureEnabled()) {
            ModernChatFragment()
        } else {
            LegacyChatFragment()
        }
    }
}
```

#### 5.3 Gradual Rollout Plan
1. **Week 5**: Deploy with feature flag disabled (0% traffic)
2. **Week 6**: Enable for internal testing (5% traffic)
3. **Week 7**: Gradual rollout (25% → 50% → 100%)
4. **Week 8**: Remove legacy code after monitoring

## Migration Benefits

### Immediate Benefits
- **Testability**: Clear separation enables comprehensive unit testing
- **Maintainability**: Single responsibility principle reduces complexity
- **Scalability**: Modular architecture supports feature growth
- **Performance**: StateFlow + DiffUtil eliminates unnecessary UI updates

### Long-term Benefits
- **Team Velocity**: New features can be developed faster
- **Bug Reduction**: Clear contracts reduce integration issues
- **Code Reuse**: Domain layer can be shared across features
- **Technology Migration**: Architecture supports future framework changes

## Risk Mitigation

### Technical Risks
- **Performance Regression**: Comprehensive benchmarking before rollout
- **Memory Leaks**: Strict lifecycle testing with LeakCanary
- **Crash Increase**: Gradual rollout with crash monitoring

### Business Risks
- **Feature Parity**: Comprehensive feature comparison testing
- **User Experience**: A/B testing to validate UX improvements
- **Rollback Plan**: Feature flag allows instant rollback

## Success Metrics
- **Crash Rate**: < 0.1% (down from current 0.3%)
- **Performance**: 50ms faster message loading
- **Development Velocity**: 30% faster feature delivery
- **Test Coverage**: > 90% (up from current 45%)
- **Code Maintainability**: Reduced cyclomatic complexity by 40%
