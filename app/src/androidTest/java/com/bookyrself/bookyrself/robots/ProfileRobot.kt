package com.bookyrself.bookyrself.robots

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.bookyrself.bookyrself.R
import kotlinx.android.synthetic.main.fragment_profile.view.*

object ProfileRobot : BaseRobot() {
    val userNameField = withId(R.id.username_profile_fragment)
}