package com.bookyrself.bookyrself.tests

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.robots.BaseRobot
import com.bookyrself.bookyrself.tests.BaseTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@LargeTest
@RunWith(AndroidJUnit4::class)
class OnboardingTest : BaseTest() {

    @Test
    fun onboardingTest() {
        // Wait until the onboarding flow has loaded
        BaseRobot().assertOnView(withText("Find Artists"), matches(isDisplayed()))
        onView(isRoot()).perform(ViewActions.swipeLeft())
        onView(isRoot()).perform(ViewActions.swipeLeft())
        onView(isRoot()).perform(ViewActions.swipeLeft())
        signInWithEmail("artie-tester@gmail.com", "qwerty12!")
        BaseRobot().assertOnView(withSubstring("ArtieTester"), matches(isDisplayed()))
        BaseRobot().doOnView(withId(R.id.navigation_profile), click())

        // Not sure why this would fail to find "Sign Out" after clicking it
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        BaseRobot().doOnView(withId(R.id.sign_out), click())
        sleep(3000)

        BaseRobot().doOnView(withId(R.id.navigation_event_invites_list), click())
        BaseRobot().assertOnView(withId(R.id.empty_state_text_header), matches(isDisplayed()))
        BaseRobot().doOnView(withId(R.id.navigation_contacts), click())
        BaseRobot().assertOnView(withId(R.id.empty_state_text_header), matches(isDisplayed()))
    }

    private fun signInWithEmail(email: String, password: String) {
        BaseRobot().doOnView(withId(R.id.navigation_profile), click())
        BaseRobot().doOnView(allOf(isDisplayed(), withId(R.id.empty_state_button)), click())
        BaseRobot().doOnView(withText("Already signed up? Click here to log in"), click())

        BaseRobot().doOnView(withId(R.id.email_edit_text), typeText(email))
        BaseRobot().doOnView(withId(R.id.password_edit_text), typeText(password))
        closeSoftKeyboard()
        BaseRobot().doOnView(withId(R.id.auth_button), click())
    }
}