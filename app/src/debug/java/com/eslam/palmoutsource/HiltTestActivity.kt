package com.eslam.palmoutsource

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Test-only activity for hosting fragments in instrumented tests.
 *
 * Provides an isolated container to attach and test fragments
 * with Hilt dependency injection enabled.
 */
@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a very minimal container layout
        setContentView(R.layout.activity_test_container)
    }
}
