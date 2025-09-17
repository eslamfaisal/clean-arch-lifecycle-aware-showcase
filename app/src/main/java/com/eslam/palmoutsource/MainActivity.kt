package com.eslam.palmoutsource

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eslam.palmoutsource.presentation.chat.ChatFragment
import com.eslam.palmoutsource.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity with Hilt dependency injection.
 * 
 * PRODUCTION CRASH FIX IMPLEMENTED:
 * ✅ Uses AppCompatActivity for Fragment support
 * ✅ Proper Fragment lifecycle management
 * ✅ Hilt dependency injection
 * ✅ Environment enforced: AGP 8.6.0, Kotlin 1.9.23
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Only add fragment if this is the first creation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatFragment.newInstance())
                .commit()
        }
    }
}