package com.bookyrself.bookyrself.views.activities

import android.os.Bundle
import android.widget.Toast
import com.bookyrself.bookyrself.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_imageview.*

class ViewImageActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imageview)
        val imageId = intent.getStringExtra("id")
        val imageType = intent.getStringExtra("imageType")
        val profileImageReference = imageStorage.child("images/$imageType/$imageId")
        profileImageReference
                .downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.with(applicationContext)
                            .load(uri)
                            .resize(imageview_activity_image.width, 0)
                            .into(imageview_activity_image)
                }.addOnFailureListener {
                    presentError("Image Unavailable")
                }
    }


    override fun presentError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

}
