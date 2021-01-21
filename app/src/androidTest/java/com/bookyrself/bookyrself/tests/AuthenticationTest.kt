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
import com.bookyrself.bookyrself.robots.ProfileRobot
import com.bookyrself.bookyrself.tests.BaseTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@LargeTest
@RunWith(AndroidJUnit4::class)
class AuthenticationTest : BaseTest() {

    @Test
    fun authenticationTest() {
        // Wait until the onboarding flow has loaded
        baseRobot.assertOnView(withText("Find Artists"), matches(isDisplayed()))
        onView(isRoot()).perform(ViewActions.swipeLeft())
        onView(isRoot()).perform(ViewActions.swipeLeft())
        onView(isRoot()).perform(ViewActions.swipeLeft())
        signInWithEmail("artie-tester@gmail.com", "qwerty12!")

        // TODO: Do more profile validation
        baseRobot.assertOnView(ProfileRobot.userNameField, matches(isDisplayed()))
        baseRobot.doOnView(withId(R.id.navigation_profile), click())

        // Not sure why this would fail to find "Sign Out" after clicking it
        openActionBarOverflowOrOptionsMenu(baseRobot.getContext())

        // TODO: Make this pretty (
        baseRobot.doOnView(withText(baseRobot.getContext().getString(R.string.sign_out)), click())

        // TODO: Can we get rid of this? (assertOnView profile fragment signed out)
        sleep(3000)

        // validate invites signed out empty state
        baseRobot.doOnView(withId(R.id.navigation_event_invites_list), click())
        baseRobot.assertOnView(
                // TODO: Lets move this into its own robot
                withText(baseRobot.getContext().getString(R.string.empty_state_event_invites_signed_out_subheader)),
                matches(isDisplayed()))

        // validate events calendar signed out empty state
        baseRobot.doOnView(withId(R.id.navigation_calendar), click())
        baseRobot.assertOnView(withText(baseRobot.getContext().getString(R.string.events_fragment_empty_state_signed_out_subheader)),
                matches(isDisplayed()))

        // validate contacts signed out empty state
        baseRobot.doOnView(withId(R.id.navigation_contacts), click())
        baseRobot.assertOnView(withText(baseRobot.getContext().getString(R.string.contacts_empty_state_signed_out_subheader)),
                matches(isDisplayed()))
    }

    //TODO: Move to Robot
    private fun signInWithEmail(email: String, password: String) {
        baseRobot.doOnView(withId(R.id.navigation_profile), click())
        baseRobot.doOnView(allOf(isDisplayed(), withId(R.id.empty_state_button)), click())
        baseRobot.doOnView(withText("Already signed up? Click here to log in"), click())

        baseRobot.doOnView(withId(R.id.email_edit_text), typeText(email))
        baseRobot.doOnView(withId(R.id.password_edit_text), typeText(password))
        closeSoftKeyboard()
        baseRobot.doOnView(withId(R.id.auth_button), click())
    }
}