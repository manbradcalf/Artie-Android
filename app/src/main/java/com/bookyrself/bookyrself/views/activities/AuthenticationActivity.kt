package com.bookyrself.bookyrself.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.user.User
import com.bookyrself.bookyrself.viewmodels.AuthenticationActivityViewModel
import com.bookyrself.bookyrself.viewmodels.AuthenticationActivityViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthenticationActivity : AppCompatActivity() {

    private var isNewUser = true
    lateinit var model: AuthenticationActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        model = ViewModelProviders.of(this, AuthenticationActivityViewModelFactory(application))
                .get(AuthenticationActivityViewModel::class.java)

        model.userLiveData.observe(this) {
            val returnIntent = Intent()
            returnIntent.putExtra(IS_NEW_USER_KEY, isNewUser)
            returnIntent.putExtra(EMAIL_KEY, it.email)
            returnIntent.putExtra(USERNAME_KEY, username_edit_text.text)
            setResult(RESULT_OK, returnIntent)
            finish()
        }

        progressBar.visibility = View.GONE

        sign_in_text.setOnClickListener {
            if (isNewUser) {
                showSignInPage()
            } else {
                showRegisterPage()
            }
        }

        auth_button.setOnClickListener {
            when {
                isNewUser -> {
                    if (username_edit_text.length() != 0) {
                        progressBar.visibility = View.VISIBLE
                        FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email_edit_text.text.toString(), password_edit_text.text.toString())
                                .addOnCompleteListener {
                                    progressBar.visibility = View.GONE

                                    // Make and send new user to firebase
                                    val user = User()
                                    user.username = username_edit_text.text.toString()
                                    user.email = it.result?.user?.email
                                    model.createUser(user, it.result!!.user!!.uid)
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                    } else {
                        username_edit_text.error = "Please Enter a username!"
                        return@setOnClickListener
                    }
                }

                !isNewUser -> {
                    progressBar.visibility = View.VISIBLE
                    FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email_edit_text.text.toString(), password_edit_text.text.toString())
                            .addOnCompleteListener {
                                val returnIntent = Intent()
                                returnIntent.putExtra(IS_NEW_USER_KEY, isNewUser)
                                progressBar.visibility = View.GONE
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }
                            .addOnFailureListener {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                }
            }
        }
    }

    private fun showSignInPage() {
        isNewUser = false
        auth_button.text = "Sign In"
        textView.text = "Welcome back!"
        username_edit_text.visibility = View.GONE
        sign_in_text.text = "Not signed up? Click here to get started!"
    }

    private fun showRegisterPage() {
        isNewUser = true
        auth_button.text = "Sign Up"
        textView.text = "Join the fun!"
        username_edit_text.visibility = View.VISIBLE
        sign_in_text.text = "Already signed up? Click here to log in"
    }

    companion object {
        const val IS_NEW_USER_KEY = "isNewUser"
        const val EMAIL_KEY = "email"
        const val USERNAME_KEY = "username"
    }
}