package com.bookyrself.bookyrself.views.activities

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.profile.ProfileRepo
import com.bookyrself.bookyrself.data.profile.ProfileRepo.ProfileRepoResponse.Failure
import com.bookyrself.bookyrself.data.profile.ProfileRepo.ProfileRepoResponse.Success
import com.bookyrself.bookyrself.data.serverModels.EventDetail.Host
import com.bookyrself.bookyrself.data.serverModels.user.User
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class ProfileEditActivity : AppCompatActivity() {

    private var profileRepo: ProfileRepo? = null
    val user = User()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO: Move to viewmodel
        this.profileRepo = MainActivity.profileRepo
        setContentView(R.layout.activity_profile_edit)

        // Set any existing data
        profile_edit_bio.setText(intent.getStringExtra("Bio"))
        profile_edit_username.setText(intent.getStringExtra("Username"))
        profile_edit_url.setText(intent.getStringExtra("Url"))

        Places.initialize(applicationContext, resources.getString(R.string.google_api_key))

        // TODO: The following code is mostly duplicated from eventcreationactivity
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_profile_edit) as AutocompleteSupportFragment?

        // Specify the types of place data to return.
        autocompleteFragment!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setHint(intent.getStringExtra("Location"))
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES)
        val geocoder = Geocoder(this, Locale.getDefault())

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                try {
                    val addresses = geocoder.getFromLocation(place.latLng!!.latitude, place.latLng!!.longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        val cityState = addresses[0].locality + ", " + addresses[0].adminArea
                        val etPlace = autocompleteFragment.view?.findViewById(R.id.places_autocomplete_search_input) as EditText
                        etPlace.hint = cityState
                        user.citystate = cityState
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("ERROR SELECTING PLACE", "An error occurred: $status")
            }
        })

        if (intent.getStringExtra("Tags") != null) {
            intent.getStringExtra("Tags").let { profile_edit_tags.setText(it.replace("\\[|]|, $".toRegex(), "")) }
        }

        profile_edit_fab.setOnClickListener {
            val returnIntent = Intent()

            user.username = profile_edit_username.text.toString()
            user.bio = profile_edit_bio.text.toString()
            user.url = profile_edit_url.text.toString()

            val tagsString = profile_edit_tags.text.toString()
            val tagsList = Arrays.asList(*tagsString.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            user.tags = tagsList

            // Update the user
            //TODO: Move to viewmodel
            CoroutineScope(Dispatchers.IO).launch {
                when (profileRepo!!.updateProfileInfo(FirebaseAuth.getInstance().uid!!, user)) {
                    is Success -> {
                        val userEventsResponse = FirebaseServiceCoroutines
                                .instance
                                .getUsersEventInvites(FirebaseAuth.getInstance().uid!!)

                        if (userEventsResponse.isSuccessful) {
                            if (userEventsResponse.body() != null) {
                                // Update events I'm hosting
                                val host = Host()
                                host.userId = FirebaseAuth.getInstance().uid
                                host.username = user.username
                                host.url = user.url
                                host.citystate = user.citystate
                                userEventsResponse.body()!!.filter { it.value.isHost }.forEach {
                                    val updateEventHostResponse = FirebaseServiceCoroutines.instance.updateEventHost(host, it.key)
                                    if (updateEventHostResponse.errorBody() != null) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(applicationContext, "Oh no! Something went wrong updating your events", Toast.LENGTH_LONG).show()
                                            setResult(Activity.RESULT_OK)
                                            finish()
                                        }
                                    }
                                }
                            }
                        }
                        val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(profile_edit_username.text.toString())
                                .build()
                        FirebaseAuth.getInstance().currentUser!!.updateProfile(profileUpdate)

                        // Finish the activity with a success
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                    is Failure -> {
                        Toast.makeText(applicationContext, "Oh no! Something went wrong updating your profile", Toast.LENGTH_LONG).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}
