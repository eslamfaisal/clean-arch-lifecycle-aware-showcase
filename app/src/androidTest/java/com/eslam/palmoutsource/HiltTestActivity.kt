package com.eslam.palmoutsource

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.testing.EmptyFragmentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle theme extras like EmptyFragmentActivity
        val themeId = intent.getIntExtra(EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, 0)
        if (themeId != 0) {
            setTheme(themeId)
        }
        super.onCreate(savedInstanceState)
    }
}
