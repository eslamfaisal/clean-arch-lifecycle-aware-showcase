package com.eslam.palmoutsource.testing

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.EmptyFragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.eslam.palmoutsource.HiltTestActivity

inline fun <reified F : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    themeResId: Int,
    crossinline action: F.() -> Unit = {}
): Pair<ActivityScenario<HiltTestActivity>, F> {
    var fragmentRef: F? = null
    val activityScenario = ActivityScenario.launch(HiltTestActivity::class.java)
    activityScenario.onActivity { activity ->
        // Apply theme if provided
        if (themeResId != 0) {
            activity.setTheme(themeResId)
        }
        
        val fragment = activity.supportFragmentManager.fragmentFactory
            .instantiate(F::class.java.classLoader!!, F::class.java.name) as F
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, null)
            .commitNow()
        fragment.action()
        fragmentRef = fragment
    }
    return activityScenario to requireNotNull(fragmentRef)
}
