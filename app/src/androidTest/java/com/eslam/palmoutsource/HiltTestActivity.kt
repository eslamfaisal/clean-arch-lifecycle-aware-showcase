package com.eslam.palmoutsource

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Test activity for hosting fragments in instrumented tests.
 * 
 * This activity provides a container for testing fragments in isolation
 * while maintaining proper Hilt dependency injection.
 * 
 * TESTING ARCHITECTURE PRINCIPLES:
 * ✅ Minimal activity for fragment testing
 * ✅ Hilt-enabled for proper DI
 * ✅ Follows Android testing best practices
 * ✅ Compatible with both Espresso and Robolectric
 */
@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Container layout for hosting test fragments
        setContentView(com.eslam.palmoutsource.test.R.layout.activity_test_container)
    }
}
