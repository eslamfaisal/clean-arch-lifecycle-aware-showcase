package com.eslam.palmoutsource.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import com.eslam.palmoutsource.HiltTestActivity
/**
 * Launches a fragment inside HiltTestActivity for instrumented tests.
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    crossinline action: T.() -> Unit = {}
): ActivityScenario<HiltTestActivity> {
    val scenario = ActivityScenario.launch(HiltTestActivity::class.java)
    scenario.onActivity { activity ->
        val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager.beginTransaction()
            .replace(
                com.eslam.palmoutsource.R.id.test_fragment_container,
                fragment,
                T::class.java.simpleName
            )
            .commitNow()

        (fragment as T).action()
    }
    return scenario
}