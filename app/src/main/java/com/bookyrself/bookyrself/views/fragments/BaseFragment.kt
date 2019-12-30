package com.bookyrself.bookyrself.views.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.fragment.app.Fragment
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.views.activities.AuthenticationActivity
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.empty_state_template.*

open class BaseFragment : Fragment() {

    fun showEmptyState(header: String, subHeader: String, image: Drawable?, buttonText: String? = null) {
        showContent(false)
        showLoadingState(false)
        empty_state_view?.visibility = View.VISIBLE
        empty_state_image?.visibility = View.VISIBLE
        empty_state_text_header?.visibility = View.VISIBLE
        empty_state_text_subheader?.visibility = View.VISIBLE

        empty_state_text_header?.text = header
        empty_state_text_subheader?.text = subHeader
        empty_state_image?.setImageDrawable(image)
        if (buttonText != null) {
            empty_state_button?.visibility = View.VISIBLE
            empty_state_button?.text = buttonText
            empty_state_button?.setOnClickListener {
                // Authenticate
                val intent = Intent(context,AuthenticationActivity::class.java)
                startActivityForResult(intent, RC_SIGN_IN)
            }
        } else {
            empty_state_button?.visibility = View.GONE
        }
    }

    fun hideEmptyState() {
        empty_state_button.visibility = View.GONE
        empty_state_image.visibility = View.GONE
        empty_state_text_header.visibility = View.GONE
        empty_state_text_subheader.visibility = View.GONE
    }

    fun showSignedOutEmptyState(message: String, img: Drawable?) {
        showEmptyState(
                getString(R.string.empty_state_signed_out_header),
                message,
                img,
                "Sign In!"
        )
    }

    open fun presentError(message: String) {
        showEmptyState(getString(R.string.error_header),
                message,
                activity!!.getDrawable(R.drawable.ic_error_empty_state))
    }

    open fun showContent(show: Boolean) {}

    open fun showLoadingState(show: Boolean) {}

    companion object {
        const val RC_SIGN_IN = 123
        const val RC_EVENT_CREATION = 456
    }
}