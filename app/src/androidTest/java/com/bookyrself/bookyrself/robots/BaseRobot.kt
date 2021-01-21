package com.bookyrself.bookyrself.robots

import android.util.Log
import android.view.View
import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matcher
import java.lang.Thread.sleep

open class BaseRobot {
    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    fun doOnView(viewMatcher: Matcher<View>, vararg actions: ViewAction) {
        actions.forEach {
            Log.i(TAG, "Trying to do $it on $viewMatcher")
            waitFor { onView(viewMatcher).perform(it) }
        }
    }

    fun doOnView(viewMatcher: Matcher<View>, vararg actions: ViewAction, waitMillis: Int) {
        actions.forEach {
            Log.i(TAG, "Waiting $waitMillis milliseconds to do $it on $viewMatcher")
            waitFor({ onView(viewMatcher).perform(it) }, waitMillis)
        }
    }

    fun assertOnView(viewMatcher: Matcher<View>, vararg assertions: ViewAssertion) {
        assertions.forEach {
            Log.i(TAG, "Trying to assert $it on $viewMatcher")
            waitFor { onView(viewMatcher).check(it) }
        }
    }

    fun assertOnView(
            viewMatcher: Matcher<View>,
            vararg assertions: ViewAssertion,
            waitMillis: Int
    ) {
        assertions.forEach {
            Log.i(TAG, "Waiting $waitMillis milliseconds to assert $it on $viewMatcher")
            waitFor({ onView(viewMatcher).check(it) }, waitMillis)
        }
    }

    fun doOnAdapterView(
            dataInteraction: DataInteraction,
            vararg actions: ViewAction
    ) {
        actions.forEach {
            waitFor { dataInteraction.perform(it) }
        }
    }

    fun doOnAdapterViewAtPosition(
            dataInteraction: DataInteraction,
            position: Int,
            vararg actions: ViewAction
    ) {
        actions.forEach {
            waitFor { dataInteraction.atPosition(position).perform(it) }
        }
    }

    /**
     * Perform action of implicitly waiting for a certain function to succeed
     * @param func The block to try until success
     */
    fun waitFor(func: () -> Unit) {
        waitFor(func, waitMillis = 5000)
    }

    /**
     * Perform action of implicitly waiting for a certain function to succeed
     * @param func The block to try until success
     * @param waitMillis The amount of time to wait until a successful execution
     */
    fun waitFor(
            func: () -> Unit,
            waitMillis: Int
    ) {
        val maxTries = waitMillis / WAIT_MILLIS_PER_TRY
        var tries = 0

        for (i in 0..maxTries) {
            try {
                // Track the amount of times we've tried
                tries++

                func()
                return
            } catch (e: Throwable) {
                if (tries == maxTries) {
                    throw e
                }
                sleep(WAIT_MILLIS_PER_TRY.toLong())
            }
        }
        throw Exception("Error doing $func")
    }

    companion object {
        const val TAG = "BaseRobot"
        const val WAIT_MILLIS_PER_TRY = 100
    }
}