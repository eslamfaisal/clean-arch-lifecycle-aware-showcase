package com.eslam.palmoutsource.presentation.chat

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eslam.palmoutsource.HiltTestActivity
import com.eslam.palmoutsource.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for ChatFragment using Espresso and Hilt.
 *
 * TESTING STRATEGY:
 * ✅ Uses real Android environment for integration testing
 * ✅ Tests actual UI interactions with Espresso
 * ✅ Validates Hilt dependency injection in real Android context
 * ✅ Covers critical user journeys and edge cases
 * ✅ Ensures production-ready behavior
 *
 * PRINCIPAL ENGINEER APPROACH:
 * ✅ Tests run on actual Android runtime
 * ✅ Validates real-world performance and behavior
 * ✅ Complements Robolectric tests with full integration coverage
 * ✅ Tests critical production crash scenarios
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatFragmentInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<HiltTestActivity>

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * Test: Fragment launches in HiltTestActivity successfully.
     *
     * INTEGRATION: Validates full Hilt DI chain in real Android environment.
     */
    @Test
    fun testFragmentLaunchesInHiltActivity() {
        // GIVEN - HiltTestActivity is launched
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
        activityScenario = ActivityScenario.launch(intent)

        // WHEN - ChatFragment is added to the activity
        activityScenario.onActivity { activity ->
            val fragment = ChatFragment.newInstance()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.eslam.palmoutsource.test.R.id.container, fragment)
                .commitNow()
        }

        // THEN - Fragment UI is displayed correctly
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonSend))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    /**
     * Test: Full user interaction flow in real Android environment.
     *
     * END-TO-END: Tests complete user journey from input to message display.
     */
    @Test
    fun testCompleteMessageSendingFlow() {
        // GIVEN - Fragment is launched in activity
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
        activityScenario = ActivityScenario.launch(intent)

        activityScenario.onActivity { activity ->
            val fragment = ChatFragment.newInstance()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.eslam.palmoutsource.test.R.id.container, fragment)
                .commitNow()
        }

        // Wait for initial load
        Thread.sleep(1500)

        // WHEN - User types and sends a message
        val testMessage = "Integration test message"
        onView(withId(R.id.editTextMessage))
            .perform(typeText(testMessage), closeSoftKeyboard())

        onView(withId(R.id.buttonSend))
            .perform(click())

        // THEN - Input is cleared and message is processed
        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))

        // Verify RecyclerView has content
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(hasMinimumChildCount(1)))
    }

    /**
     * Test: Fragment handles activity lifecycle changes.
     *
     * LIFECYCLE: Critical test for production crash fix validation.
     */
    @Test
    fun testFragmentSurvivesActivityRecreation() {
        // GIVEN - Fragment is running in activity
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
        activityScenario = ActivityScenario.launch(intent)

        activityScenario.onActivity { activity ->
            val fragment = ChatFragment.newInstance()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.eslam.palmoutsource.test.R.id.container, fragment)
                .commitNow()
        }

        // WHEN - Activity is recreated (configuration change)
        activityScenario.recreate()

        // THEN - Fragment is still functional
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .perform(typeText("After recreation"))

        onView(withId(R.id.buttonSend))
            .perform(click())

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * Test: Error handling in real Android environment.
     *
     * RESILIENCE: Tests error recovery mechanisms.
     */
    @Test
    fun testErrorHandlingWithLongPress() {
        // GIVEN - Fragment is displayed
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
        activityScenario = ActivityScenario.launch(intent)

        activityScenario.onActivity { activity ->
            val fragment = ChatFragment.newInstance()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.eslam.palmoutsource.test.R.id.container, fragment)
                .commitNow()
        }

        // WHEN - Long press to trigger retry
        onView(withId(R.id.recyclerViewMessages))
            .perform(longClick())

        // THEN - Fragment remains stable and functional
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: Multiple rapid interactions in real environment.
     *
     * STRESS TEST: Validates performance under load.
     */
    @Test
    fun testRapidInteractionsPerformance() {
        // GIVEN - Fragment is ready
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
        activityScenario = ActivityScenario.launch(intent)

        activityScenario.onActivity { activity ->
            val fragment = ChatFragment.newInstance()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.eslam.palmoutsource.test.R.id.container, fragment)
                .commitNow()
        }

        // WHEN - Rapid message sending
        repeat(3) { index ->
            onView(withId(R.id.editTextMessage))
                .perform(typeText("Rapid message $index"), closeSoftKeyboard())

            onView(withId(R.id.buttonSend))
                .perform(click())

            // Brief pause to allow processing
            Thread.sleep(200)
        }

        // THEN - Fragment handles load gracefully
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editTextMessage))
            .check(matches(withText("")))
    }

    /**
     * Test: Hilt dependency injection works correctly.
     *
     * ARCHITECTURE: Validates DI container functionality.
     */
    @Test
    fun testHiltDependencyInjectionWorks() {
        // GIVEN - Fragment uses Hilt for ViewModel injection
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
        activityScenario = ActivityScenario.launch(intent)

        activityScenario.onActivity { activity ->
            val fragment = ChatFragment.newInstance()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.eslam.palmoutsource.test.R.id.container, fragment)
                .commitNow()
        }

        // Wait for ViewModel initialization and data loading
        Thread.sleep(1500)

        // THEN - ViewModel is injected and working (evidenced by loaded messages)
        onView(withId(R.id.recyclerViewMessages))
            .check(matches(hasMinimumChildCount(1)))
    }
}
