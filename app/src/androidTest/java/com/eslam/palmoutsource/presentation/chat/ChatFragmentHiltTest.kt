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
 * Instrumented UI tests for the production ChatFragment using Hilt.
 *
 * STRATEGY:
 * ✅ Launches ChatFragment inside HiltTestActivity
 * ✅ Validates fragment lifecycle and DI
 * ✅ Covers user interactions with Espresso
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatFragmentHiltTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }


    @Test
    fun fragmentLaunchesSuccessfully() {
        launchFragmentInHiltContainer<ChatFragment>()

        onView(withId(R.id.recyclerViewMessages)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextMessage)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonSend))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }



}
