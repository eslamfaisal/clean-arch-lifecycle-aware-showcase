package com.eslam.palmoutsource

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.eslam.palmoutsource.R
import com.eslam.palmoutsource.presentation.chat.ChatFragment
import com.eslam.palmoutsource.presentation.chat.LegacyChatFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity to launch different chat fragments for testing.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_modern_chat).setOnClickListener {
            openFragment(ChatFragment.newInstance())
        }

        findViewById<Button>(R.id.button_legacy_chat).setOnClickListener {
            openFragment(LegacyChatFragment.newInstance())
        }
        // Load the modern chat fragment by default if the container is empty
        if (savedInstanceState == null) {
            openFragment(ChatFragment.newInstance())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Optional: allows user to navigate back
            .commit()
    }
}