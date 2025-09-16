package com.eslam.palmoutsource

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt dependency injection setup.
 * 
 * This enables Hilt to generate the necessary dependency injection components
 * throughout the application lifecycle.
 */
@HiltAndroidApp
class PalmTaskApplication : Application()
