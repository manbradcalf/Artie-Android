package com.bookyrself.bookyrself.base

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
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@LargeTest
@RunWith(AndroidJUnit4::class)
class NewTest : BaseTest() {

    @Test
    fun onboardingTest() {
        sleep(2000)
        onView(isRoot()).perform(ViewActions.swipeLeft())
        onView(isRoot()).perform(ViewActions.swipeLeft())
        onView(isRoot()).perform(ViewActions.swipeLeft())
        signInWithEmail("artie-tester+1@gmail.com", "testing123")
        BaseRobot().doOnView(withId(R.id.navigation_contacts), click())
        BaseRobot().assertOnView(withSubstring("Artie Fufkinn"), matches(isDisplayed()))
        BaseRobot().doOnView(withId(R.id.navigation_profile), click())

        // Not sure why this would fail to find "Sign Out" after clicking it
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withId(R.id.sign_out)).perform(click())
        sleep(3000)

        BaseRobot().doOnView(withId(R.id.navigation_event_invites_list), click())
        BaseRobot().assertOnView(withId(R.id.empty_state_text_header), matches(isDisplayed()))
        BaseRobot().doOnView(withId(R.id.navigation_contacts), click())
        BaseRobot().assertOnView(withId(R.id.empty_state_text_header), matches(isDisplayed()))
    }

    private fun signInWithEmail(email: String, password: String) {
        BaseRobot().doOnView(withId(R.id.navigation_profile), click())
        BaseRobot().doOnView(allOf(isDisplayed(), withId(R.id.empty_state_button)), click())
        BaseRobot().doOnView(withText("Sign in with email"), click())

        // Dismiss the dialog
        BaseRobot().device.pressBack()

        BaseRobot().doOnView(withId(R.id.email), typeText(email))
        BaseRobot().doOnView(withId(R.id.button_next), click())
        BaseRobot().doOnView(withId(R.id.password), typeText(password))
        closeSoftKeyboard()
        BaseRobot().doOnView(withId(R.id.button_done), click())
    }
}