package com.eslam.palmoutsource.presentation.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eslam.palmoutsource.utils.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI test to PROVE the existence of the crash in LegacyChatFragment.
 *
 * This test is designed to FAIL if the crash is ever fixed, ensuring that the legacy
 * code remains a reliable example of the bug.
 *
 * CRASH SCENARIO VALIDATED:
 * 1. Launch LegacyChatFragment, which uses `requireActivity()` for its observer.
 * 2. Recreate the fragment's view to simulate navigation or configuration changes.
 * 3. The test will catch the expected `IllegalStateException`.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LegacyChatFragmentCrashTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /**
     * This test proves that LegacyChatFragment crashes when its view is destroyed.
     * It launches the fragment and immediately recreates it, which triggers the
     * IllegalStateException because the LiveData observer is tied to the Activity's
     * lifecycle, not the Fragment's view lifecycle.
     *
     * The test PASSES if the expected exception is thrown.
     * The test FAILS if no exception is thrown (meaning the bug is gone).
     */
    @Test
    fun legacyChatFragment_crashes_whenViewIsDestroyed() {
        // Use assertThrows to confirm that the code inside the lambda crashes as expected.
        assertThrows(IllegalStateException::class.java) {
            // Launch the fragment that contains the buggy code.
            val scenario = launchFragmentInHiltContainer<LegacyChatFragment>()

            // Wait a moment for the fragment and its observers to be set up.
            Thread.sleep(1500)

            // This action destroys and recreates the fragment's view. Because the observer
            // is using `requireActivity()`, it will try to update a destroyed view,
            // causing the IllegalStateException.
            scenario.recreate()

            // Allow time for the crash to occur.
            Thread.sleep(1000)
        }
    }
}
