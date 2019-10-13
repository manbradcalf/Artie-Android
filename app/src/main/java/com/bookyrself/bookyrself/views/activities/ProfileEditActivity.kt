package com.bookyrself.bookyrself.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.Profile.ProfileRepo
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.Host
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_profile_edit.*
import java.util.*

class ProfileEditActivity : AppCompatActivity() {

    private var profileRepo: ProfileRepo? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO: Move to viewmodel
        this.profileRepo = MainActivity.profileRepo
        setContentView(R.layout.activity_profile_edit)

        // Set any existing data
        profile_edit_bio.setText(intent.getStringExtra("Bio"))
        profile_edit_location.setText(intent.getStringExtra("Location"))
        profile_edit_username.setText(intent.getStringExtra("Username"))
        profile_edit_url.setText(intent.getStringExtra("Url"))

        if (intent.getStringExtra("Tags") != null) {
            intent.getStringExtra("Tags").let { profile_edit_tags.setText(it.replace("\\[|]|, $".toRegex(), "")) }
        }

        profile_edit_fab.setOnClickListener {
            val user = User()
            val returnIntent = Intent()

            user.username = profile_edit_username.text.toString()
            user.bio = profile_edit_bio.text.toString()
            user.citystate = profile_edit_location.text.toString()
            user.url = profile_edit_url.text.toString()

            val tagsString = profile_edit_tags.text.toString()
            val tagsList = Arrays.asList(*tagsString.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            user.tags = tagsList

            // Update the user
            //TODO: Move to viewmodel
            profileRepo!!.updateProfileInfo(
                    FirebaseAuth.getInstance().uid!!, user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

                    // Get my event Invites
                    .flatMap<HashMap<String, EventInviteInfo>> {
                        FirebaseService.instance
                                .getUsersEventInvites(FirebaseAuth.getInstance().uid!!)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                    }
                    .firstOrError()
                    .toFlowable()
                    .flatMapIterable<Map.Entry<String, EventInviteInfo>> { it.entries }

                    // Only get events I'm hosting
                    .filter { stringEventInviteInfoEntry -> stringEventInviteInfoEntry.value.isHost }

                    // Update the events I'm hosting with the new data
                    .doOnNext { eventInviteInfoEntry ->

                        val host = Host()
                        host.userId = FirebaseAuth.getInstance().uid
                        host.username = user.username
                        host.url = user.url
                        host.citystate = user.citystate

                        FirebaseService.instance
                                .updateEventHost(host, eventInviteInfoEntry.key)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                    }

                    .subscribe({

                        // Update the firebase user
                        val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(profile_edit_username.text.toString())
                                .build()
                        FirebaseAuth.getInstance().currentUser!!.updateProfile(profileUpdate)

                        // Finish the activity with a success
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    },
                            { throwable ->

                                if (throwable is NoSuchElementException) {
                                    // User has no events to update, so update the FBUser and bail
                                    Log.e(this.localClassName, "User has no events to update")
                                    val profileUpdate = UserProfileChangeRequest.Builder()
                                            .setDisplayName(profile_edit_username.text.toString())
                                            .build()
                                    FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdate)

                                    // Bail
                                    setResult(Activity.RESULT_OK, returnIntent)
                                    finish()

                                } else {
                                    Toast.makeText(this, "Unable to update profile!", Toast.LENGTH_SHORT).show()
                                    Log.e("ProfileEditActivity: ", throwable.message, throwable)
                                }
                            })
        }
    }
}
