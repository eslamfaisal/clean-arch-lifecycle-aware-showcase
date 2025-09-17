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
    
    // Create simple intent with theme extras
    val startActivityIntent = Intent(ApplicationProvider.getApplicationContext(), HiltTestActivity::class.java).apply {
        if (themeResId != 0) {
            putExtra(EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, themeResId)
        }
    }
    
    val activityScenario = ActivityScenario.launch<HiltTestActivity>(startActivityIntent)
    activityScenario.onActivity { activity ->
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
