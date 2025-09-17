package com.eslam.palmoutsource

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt-based instrumentation tests.
 * 
 * This runner ensures that HiltTestApplication is used instead of the regular
 * application class during test execution, which is required for Hilt dependency
 * injection to work properly in tests.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
