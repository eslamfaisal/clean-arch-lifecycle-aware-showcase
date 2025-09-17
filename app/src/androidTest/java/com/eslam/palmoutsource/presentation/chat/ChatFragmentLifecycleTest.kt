package com.eslam.palmoutsource.presentation.chat

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.eslam.palmoutsource.R
import com.eslam.palmoutsource.testing.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChatFragmentLifecycleTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before fun setup() { hiltRule.inject() }

    @Test
    fun chatFragment_survivesViewDestructionAndRecreation_withViewLifecycleOwner() {
        val (activityScenario, _) = launchFragmentInHiltContainer<ChatFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        // Simulate onDestroyView/onCreateView without destroying the Fragment instance
        activityScenario.onActivity { activity ->
            val frag = activity.supportFragmentManager.fragments
                .filterIsInstance<ChatFragment>().first()
            val fm = activity.supportFragmentManager
            fm.beginTransaction().detach(frag).commitNow()
            fm.beginTransaction().attach(frag).commitNow()
        }

        onView(withId(R.id.recyclerViewMessages)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextMessage)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonSend)).check(matches(isDisplayed()))
    }

    @Test
    fun chatFragment_handlesViewLifecycleCyclesGracefully() {
        val (activityScenario, _) = launchFragmentInHiltContainer<ChatFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.editTextMessage)).perform(typeText("Test message"))
        onView(withId(R.id.buttonSend)).perform(click())

        repeat(3) {
            activityScenario.onActivity { activity ->
                val frag = activity.supportFragmentManager.fragments
                    .filterIsInstance<ChatFragment>().first()
                val fm = activity.supportFragmentManager
                fm.beginTransaction().detach(frag).commitNow()
                fm.beginTransaction().attach(frag).commitNow()
            }
        }

        onView(withId(R.id.recyclerViewMessages)).check(matches(isDisplayed()))
    }

    @Test
    fun chatFragment_cleansUpObservationsOnViewDestruction() {
        val (activityScenario, _) = launchFragmentInHiltContainer<ChatFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        repeat(5) {
            activityScenario.onActivity { activity ->
                val frag = activity.supportFragmentManager.fragments
                    .filterIsInstance<ChatFragment>().first()
                val fm = activity.supportFragmentManager
                fm.beginTransaction().detach(frag).commitNow()
                fm.beginTransaction().attach(frag).commitNow()
            }
        }

        onView(withId(R.id.editTextMessage)).perform(typeText("Final test"))
        onView(withId(R.id.buttonSend)).perform(click())
    }

    @Test
    fun chatFragment_survivesConfigurationChanges() {
        val (activityScenario, _) = launchFragmentInHiltContainer<ChatFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        onView(withId(R.id.editTextMessage)).perform(typeText("Message before config change"))
        onView(withId(R.id.buttonSend)).perform(click())

        activityScenario.recreate()

        onView(withId(R.id.recyclerViewMessages)).check(matches(isDisplayed()))
        onView(withId(R.id.editTextMessage)).perform(typeText("Message after config change"))
        onView(withId(R.id.buttonSend)).perform(click())
    }

    @Test
    fun chatFragment_handlesRapidLifecycleTransitions() {
        val (activityScenario, _) = launchFragmentInHiltContainer<ChatFragment>(
            themeResId = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )

        repeat(10) {
            activityScenario.onActivity { activity ->
                val frag = activity.supportFragmentManager.fragments
                    .filterIsInstance<ChatFragment>().first()
                val fm = activity.supportFragmentManager
                fm.beginTransaction().detach(frag).commitNow()
                fm.beginTransaction().attach(frag).commitNow()
            }
        }

        onView(withId(R.id.recyclerViewMessages)).check(matches(isDisplayed()))
    }
}
