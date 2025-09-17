package com.eslam.palmoutsource

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt instrumented tests.
 * 
 * This runner replaces the default Application with HiltTestApplication
 * to enable dependency injection in tests.
 * 
 * ARCHITECTURE PRINCIPLES:
 * ✅ Follows Hilt testing best practices
 * ✅ Enables proper DI in instrumented tests
 * ✅ Supports both Espresso and fragment testing
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
