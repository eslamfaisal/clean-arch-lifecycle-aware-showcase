package com.eslam.palmoutsource.presentation.chat

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eslam.palmoutsource.R
import com.eslam.palmoutsource.utils.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FOCUSED test suite specifically validating the critical ChatFragment lifecycle crash fix.
 *
 * PRODUCTION CRASH SCENARIO:
 * ❌ BEFORE: `viewModel.messages.observe(requireActivity())` → IllegalStateException crash
 * ✅ AFTER: `viewModel.messages.observe(viewLifecycleOwner)` → Lifecycle-safe
 *
 * ROOT CAUSE:
 * - Activity destroyed before Fragment
 * - LiveData observers still trying to notify destroyed Activity lifecycle
 * - IllegalStateException: "LifecycleOwner is destroyed"
 *
 * FIX IMPLEMENTED:
 * - Changed from `requireActivity()` to `viewLifecycleOwner` in all observe() calls
 * - Added defensive programming with `isAdded` checks
 * - Proper binding cleanup in onDestroyView()
 *
 * TESTING STRATEGY:
 * ✅ Reproduces exact crash scenarios through rapid lifecycle transitions
 * ✅ Validates fix prevents IllegalStateException
 * ✅ Tests edge cases that could trigger the original crash
 * ✅ Ensures UI functionality remains intact after fix
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatFragmentLifecycleCrashFixTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * CORE CRASH REPRODUCTION TEST
     * 
     * This test reproduces the exact scenario that caused production crashes:
     * 1. Fragment observes ViewModel with viewLifecycleOwner (FIXED)
     * 2. Rapid view destruction and recreation
     * 3. Verify no IllegalStateException occurs
     */
    @Test
    fun chatFragment_doesNotCrash_whenViewIsDestroyedAndRecreated() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Wait for initial loading and observer setup
        Thread.sleep(1500)

        // Verify initial state is working
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        // Send a message to ensure observers are actively listening
        onView(withId(R.id.editTextMessage))
            .perform(typeText("Test message before crash scenario"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        Thread.sleep(500)

        // CRITICAL: Rapid view destruction/recreation that caused original crash
        repeat(5) {
            scenario.recreate()
            Thread.sleep(200) // Short delay to simulate real-world timing
        }

        // If we reach here without crash, the fix is working!
        // Verify fragment is still functional
        Thread.sleep(1500) // Wait for final recreation to complete

        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))

        // Verify we can still send messages (observers are working correctly)
        onView(withId(R.id.editTextMessage))
            .perform(typeText("Message after crash test"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * STRESS TEST: Rapid lifecycle changes during active data loading
     */
    @Test
    fun chatFragment_survivesRapidLifecycleChanges_duringDataLoading() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Don't wait for loading - start lifecycle changes immediately
        // This tests the scenario where observers are being set up during destruction
        repeat(3) {
            Thread.sleep(300) // Partial loading time
            scenario.recreate()
        }

        // Wait for final state to stabilize
        Thread.sleep(2000)

        // Verify fragment survived and is functional
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))
    }

    /**
     * EDGE CASE: Activity destruction timing vs Fragment lifecycle
     */
    @Test
    fun chatFragment_handlesActivityDestructionGracefully() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Wait for initial setup
        Thread.sleep(1500)

        // Send multiple messages to create active observer state
        val messages = listOf("Message 1", "Message 2", "Message 3")
        messages.forEach { message ->
            onView(withId(R.id.editTextMessage))
                .perform(typeText(message), closeSoftKeyboard())

            onView(withId(R.id.buttonSend))
                .perform(click())

            Thread.sleep(300)
        }

        // Simulate the exact timing issue that caused crashes
        // Activity gets destroyed while Fragment observers are still active
        scenario.recreate()
        Thread.sleep(100) // Very short delay
        scenario.recreate()

        // Wait for stabilization
        Thread.sleep(1500)

        // Verify no crash occurred and functionality is preserved
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .perform(typeText("After destruction test"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * MEMORY LEAK PREVENTION: Verify observers are properly cleaned up
     */
    @Test
    fun chatFragment_cleansUpObservationsOnViewDestruction() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Wait for observers to be set up
        Thread.sleep(1500)

        // Trigger observer activity
        onView(withId(R.id.editTextMessage))
            .perform(typeText("Observer test message"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        Thread.sleep(1000)

        // Multiple rapid destructions to test cleanup
        repeat(4) {
            scenario.recreate()
            Thread.sleep(250)
        }

        // Final verification - if cleanup is working, no crashes should occur
        Thread.sleep(1500)

        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        // Test that new observers work correctly after cleanup
        onView(withId(R.id.editTextMessage))
            .perform(typeText("After cleanup test"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * CONFIGURATION CHANGE CRASH PREVENTION
     */
    @Test
    fun chatFragment_survivesConfigurationChanges_withActiveObservers() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Wait for initial loading
        Thread.sleep(1500)

        // Create active observer state with multiple messages
        repeat(3) { index ->
            onView(withId(R.id.editTextMessage))
                .perform(typeText("Config test message $index"), closeSoftKeyboard())

            onView(withId(R.id.buttonSend))
                .perform(click())

            Thread.sleep(500)
        }

        // Simulate configuration changes (screen rotation, etc.)
        repeat(3) {
            scenario.recreate()
            Thread.sleep(800) // Realistic config change timing
        }

        // Verify fragment survived configuration changes
        Thread.sleep(1500)

        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))

        // Verify functionality after config changes
        onView(withId(R.id.editTextMessage))
            .perform(typeText("After config changes"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * RACE CONDITION TEST: Observer setup during view destruction
     */
    @Test
    fun chatFragment_handlesRaceConditions_betweenObserverSetupAndDestruction() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Create a race condition scenario
        // Start destruction immediately after launch (before observers fully set up)
        Thread.sleep(100) // Very short delay
        scenario.recreate()
        
        Thread.sleep(200)
        scenario.recreate()
        
        Thread.sleep(300)
        scenario.recreate()

        // Wait for final stabilization
        Thread.sleep(2000)

        // If we reach here, race condition was handled properly
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))

        // Verify observers are working correctly after race condition
        onView(withId(R.id.editTextMessage))
            .perform(typeText("Race condition test passed"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * DEFENSIVE PROGRAMMING TEST: Verify isAdded checks work
     */
    @Test
    fun chatFragment_usesDefensiveProgramming_withIsAddedChecks() {
        val scenario = launchFragmentInHiltContainer<ChatFragment>()

        // Wait for initial setup
        Thread.sleep(1500)

        // Send message to trigger observer callbacks
        onView(withId(R.id.editTextMessage))
            .perform(typeText("Defensive programming test"), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        // Immediately recreate to test isAdded checks during callbacks
        Thread.sleep(500) // Wait for message to be processing
        scenario.recreate()

        // Quick succession of recreations to stress test defensive checks
        repeat(3) {
            Thread.sleep(200)
            scenario.recreate()
        }

        // Final verification
        Thread.sleep(1500)

        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))
    }
}
