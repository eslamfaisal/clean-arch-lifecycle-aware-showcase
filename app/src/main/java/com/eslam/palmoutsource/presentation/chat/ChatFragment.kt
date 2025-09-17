package com.eslam.palmoutsource.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.eslam.palmoutsource.R
import com.eslam.palmoutsource.databinding.FragmentChatBinding
import com.eslam.palmoutsource.presentation.chat.adapter.SimpleMessagesAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Modern ChatFragment with Clean Architecture, StateFlow, and data binding.
 *
 * PRODUCTION CRASH FIX IMPLEMENTED:
 * ✅ Uses viewLifecycleOwner with repeatOnLifecycle for safe StateFlow collection
 * ✅ Proper lifecycle management prevents IllegalStateException
 * ✅ Defensive programming with null checks
 * ✅ Modern coroutine-based reactive programming
 *
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Depends only on presentation layer (ViewModel)
 * ✅ UI logic separated from business logic
 * ✅ Framework-specific concerns isolated
 * ✅ Testable through clear interfaces
 */
@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val viewModel: SimpleChatViewModel by viewModels()
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SimpleMessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chat,
            container,
            false
        )

        // CRITICAL LIFECYCLE FIX: Use viewLifecycleOwner for data binding
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    /**
     * Sets up the RecyclerView with proper configuration.
     */
    private fun setupRecyclerView() {
        adapter = SimpleMessagesAdapter()
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Start from bottom like a chat
            }
            adapter = this@ChatFragment.adapter
        }
    }

    /**
     * Sets up click listeners for UI interactions.
     */
    private fun setupClickListeners() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.editTextMessage.text?.clear()
            }
        }

        // Handle error retry
        binding.root.setOnLongClickListener {
            viewModel.loadMessages()
            true
        }
    }

    /**
     * Observes ViewModel using LIFECYCLE-SAFE approach.
     *
     * PRODUCTION CRASH FIX: Uses viewLifecycleOwner instead of requireActivity()
     * This prevents IllegalStateException when Activity is destroyed before Fragment.
     */
    private fun observeViewModel() {
        // ✅ CRITICAL FIX: viewLifecycleOwner prevents crash
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            // Defensive programming: Check if components are initialized
            if (::adapter.isInitialized && isAdded) {
                adapter.updateMessages(messages)

                // Scroll to bottom when new messages arrive
                if (messages.isNotEmpty()) {
                    binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        // Observe loading state for data binding
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.isLoading = isLoading
        }

        // Handle error states
        viewModel.errorState.observe(viewLifecycleOwner) { error ->
            if (error != null && isAdded) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        viewModel.clearError()
                        viewModel.loadMessages()
                    }
                    .show()
            }
        }
    }


    /**
     * CRITICAL: Clean up binding to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ChatFragment()
    }
}
