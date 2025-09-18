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
 * Legacy ChatFragment with the original production crash.
 *
 * PRODUCTION CRASH INTENTIONALLY PRESENT:
 * ❌ Uses requireActivity() for LiveData observation, causing IllegalStateException
 * ❌ Lacks proper lifecycle management for observers
 * ❌ This is the version that demonstrates the bug.
 */
@AndroidEntryPoint
class LegacyChatFragment : Fragment() {

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

        // Data binding lifecycle owner is still correct here, but the observer is not.
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SimpleMessagesAdapter()
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Start from bottom like a chat
            }
            adapter = this@LegacyChatFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.editTextMessage.text?.clear()
            }
        }

        binding.root.setOnLongClickListener {
            viewModel.loadMessages()
            true
        }
    }

    /**
     * Observes ViewModel using the BUGGY, LIFECYCLE-UNSAFE approach.
     *
     * ❌ PRODUCTION CRASH: Uses requireActivity() instead of viewLifecycleOwner
     * This causes an IllegalStateException when the Activity is destroyed before the Fragment's view.
     */
    private fun observeViewModel() {
        // ❌ BUG: Using requireActivity() will cause a crash.
        viewModel.messages.observe(requireActivity()) { messages ->
            if (::adapter.isInitialized && isAdded) {
                adapter.updateMessages(messages)

                if (messages.isNotEmpty()) {
                    binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.isLoading = isLoading
        }

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = LegacyChatFragment()
    }
}
