package com.bookyrself.bookyrself.views.fragments

import android.graphics.drawable.Drawable
import android.view.View
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.empty_state_template.*

open class BaseFragment : Fragment() {

    open fun showContent(show: Boolean) {}

    open fun showLoadingState(show: Boolean) {}

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
                val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build())
                // Authenticate
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false, true)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN)
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

    open fun presentError(message: String) {}

    open fun showSignedOutEmptyState() {}

    companion object {
        const val RC_SIGN_IN = 123
    }

}