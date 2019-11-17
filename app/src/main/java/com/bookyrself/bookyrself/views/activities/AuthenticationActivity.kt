package com.bookyrself.bookyrself.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bookyrself.bookyrself.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthenticationActivity : AppCompatActivity() {

    private var isNewUser = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        progressBar.visibility = View.GONE

        sign_in_text.setOnClickListener {
            isNewUser = false
            sign_up_button.text = "Sign In"
            textView.text = "Welcome back!"
            username_edit_text.visibility = View.GONE
        }

        sign_up_button.setOnClickListener {
            when {
                username_edit_text.length() == 0 -> {
                    username_edit_text.error = "Please Enter a username!"
                    return@setOnClickListener
                }

                isNewUser -> {
                    progressBar.visibility = View.VISIBLE
                    FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(email_edit_text.text.toString(), password_edit_text.text.toString())
                            .addOnCompleteListener {
                                val returnIntent = Intent()
                                if (it.isSuccessful && it.result != null) {
                                    returnIntent.putExtra("isNewUser", isNewUser)
                                    returnIntent.putExtra("username", username_edit_text.text)
                                    progressBar.visibility = View.GONE
                                    setResult(RC_SIGN_UP, returnIntent)
                                    finish()
                                } else {
                                    setResult(RC_ERROR, returnIntent)
                                    finish()
                                }
                            }
                }

                !isNewUser -> {
                    progressBar.visibility = View.VISIBLE
                    FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email_edit_text.text.toString(), password_edit_text.text.toString())
                            .addOnCompleteListener {
                                val returnIntent = Intent()
                                if (it.isSuccessful && it.result != null) {
                                    returnIntent.putExtra("isNewUser", isNewUser)
                                    progressBar.visibility = View.GONE
                                    setResult(RC_SIGN_IN, returnIntent)
                                    finish()
                                } else {
                                    progressBar.visibility = View.GONE
                                    setResult(RC_ERROR, returnIntent)
                                    finish()
                                }
                            }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RC_CANCELED, Intent())
    }

    // TODO: CONSOLIDATE these w fragment constants when profilefragment is refactored into MVVM
    companion object {
        const val RC_SIGN_UP = 123
        const val RC_SIGN_IN = 456
        const val RC_ERROR = 500
        const val RC_CANCELED = 111
    }
}