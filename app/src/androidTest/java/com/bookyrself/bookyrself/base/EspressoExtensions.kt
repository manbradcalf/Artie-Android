package com.bookyrself.bookyrself.base

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class EspressoExtensions {
    companion object {
        /**
         * Perform action of waiting for a certain view within a single root view
         * @param matcher Generic Matcher used to find our view
         */
        fun searchFor(matcher: Matcher<View>): ViewAction {
            return object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isRoot()
                }

                override fun getDescription(): String {
                    return "searching for view $matcher in the root view"
                }

                override fun perform(uiController: UiController, view: View) {

                    var tries = 0
                    val childViews: Iterable<View> = TreeIterables.breadthFirstViewTraversal(view)

                    // Look for the match in the tree of childviews
                    childViews.forEach {
                        tries++
                        if (matcher.matches(it)) {
                            // found the view
                            return
                        }
                    }

                    throw NoMatchingViewException.Builder()
                            .withRootView(view)
                            .withViewMatcher(matcher)
                            .build()


                }
            }
        }

        /**
         * Search for a [targetViewId] inside of a childview at [position] in [recyclerViewId]
         * @param position the position of the childView to check in search of targetView. EX: R.id.carContent
         * @param recyclerViewId the id of the RecyclerView we are matching in
         * @param targetViewId the id of the view we wish to match on. EX: R.id.yearAndMakeTextField. If -1,
         * no id has been provided, so we assume we want the childview at @position in the RecyclerView
         */
        fun atPositionInRecyclerView(
                position: Int,
                recyclerViewId: Int,
                targetViewId: Int = -1
        ): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description) {
                    description.appendText(
                            "Matching target view $targetViewId in RecyclerView $recyclerViewId at " +
                                    "position $position"
                    )
                }

                override fun matchesSafely(view: View): Boolean {
                    var childView: View? = null
                    val recyclerView = view.rootView.findViewById(recyclerViewId) as RecyclerView

                    if (recyclerView.id == recyclerViewId) {
                        childView =
                                recyclerView.findViewHolderForAdapterPosition(position)?.itemView
                    }
                    if (childView == null) {
                        throw NoMatchingViewException.Builder()
                                .withRootView(recyclerView)
                                .withViewMatcher(withId(targetViewId))
                                .build()
                    }

                    // The view to match on is the child view if no target view was specified
                    return if (targetViewId == -1) {
                        view === childView
                    }

                    // We have a targetViewId, so find it in the childView/CarContent
                    else {
                        val targetView = childView.findViewById<View>(targetViewId)
                        view === targetView
                    }
                }
            }
        }

//        fun textViewNumberInRange(
//                minVal: Int,
//                maxVal: Int,
//                rangeSortType: RangeSortType
//        ): Matcher<View> {
//            return object : TypeSafeMatcher<View>() {
//                override fun describeTo(description: Description) {
//                    description.appendText(
//                            "Ensuring number in text view is between $minVal and $maxVal"
//                    )
//                }
//
//                override fun matchesSafely(item: View?): Boolean {
//                    item as TextView
//                    val formatter = rangeSortType.searchResultsFormatter
//                    val valueToCheck = formatter(item.text as String)
//                    return valueToCheck in minVal..maxVal
//                }
//            }
//        }

        fun swipeUpHalfway(): ViewAction {
            return GeneralSwipeAction(
                    Swipe.FAST,
                    GeneralLocation.CENTER,
                    GeneralLocation.TOP_CENTER,
                    Press.FINGER
            )
        }

        fun swipeLeftHalfway(): ViewAction {
            return GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.CENTER,
                    GeneralLocation.CENTER_LEFT,
                    Press.FINGER
            )
        }
    }
}