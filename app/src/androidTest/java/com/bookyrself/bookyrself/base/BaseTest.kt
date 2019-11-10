package com.bookyrself.bookyrself.base

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.bookyrself.bookyrself.views.activities.OnboardingActivity
import org.junit.Before
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseTest {
    @Before
    fun startActivityAndClearUser() {
        ActivityScenario.launch(OnboardingActivity::class.java)
    }
}