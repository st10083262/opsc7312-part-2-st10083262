package com.notemaster

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var uploadImageButton: MaterialButton
    private lateinit var darkModeSwitch: SwitchMaterial

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences for dark mode state
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        // Initialize UI elements
        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        profileImageView = findViewById(R.id.profileImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)

        // Fetch user details from Firestore
        fetchUserDetails()

        // Load dark mode state from preferences
        val isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkModeEnabled
        setDarkMode(isDarkModeEnabled)

        // Handle image upload button click
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        // Toggle for dark mode
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
            // Save the dark mode state in shared preferences
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
        }
    }

    // Fetch user details from Firestore
    private fun fetchUserDetails() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("fullName") ?: "No Name"
                    val email = document.getString("email") ?: "No Email"
                    userNameTextView.text = name
                    userEmailTextView.text = email

                    // Load profile image if available
                    storage.reference.child("profileImages/$uid").downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this).load(uri).into(profileImageView)
                    }.addOnFailureListener {
                        // Handle failure if image is not found
                        Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to load user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Open image picker to upload profile image
    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    // Handle the result from image picker and upload the image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data

            // Upload image to Firebase Storage
            val currentUser = auth.currentUser
            currentUser?.uid?.let { uid ->
                val ref = storage.reference.child("profileImages/$uid")
                ref.putFile(imageUri!!)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show()
                        // Load the uploaded image into the ImageView
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this).load(uri).into(profileImageView)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Function to apply dark mode
    private fun setDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Toast.makeText(this, "Dark mode enabled", Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Toast.makeText(this, "Dark mode disabled", Toast.LENGTH_SHORT).show()
        }
    }
}
