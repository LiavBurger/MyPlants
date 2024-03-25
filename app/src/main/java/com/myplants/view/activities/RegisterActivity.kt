package com.myplants.view.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myplants.R
import com.squareup.picasso.Picasso
import java.util.UUID

class RegisterActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var ivProfileImage: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private var selectedImageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ivProfileImage = findViewById(R.id.profileImage)
        etUsername = findViewById(R.id.etUserName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.et_re_enter_Password)

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            attemptRegistration()
        }

        findViewById<Button>(R.id.btnAddNewPlant).setOnClickListener {
            navigateToLogin()
        }



        // For choosing profile image
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                Picasso.get().load(selectedImageUri).into(ivProfileImage)
            }
        }

        findViewById<FloatingActionButton>(R.id.fabtnCamera).setOnClickListener {
            openGalleryForImage()
        }
    }

    private fun attemptRegistration() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val username = etUsername.text.toString().trim()

        if (!validateForm(username, email, password, confirmPassword)) return

        checkUsernameUniqueness(username) { isUnique ->
            if (isUnique) {
                registerUser(email, password, username)
            } else {
                displayMessage("Username already taken")
            }
        }
    }

    private fun validateForm(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            displayMessage("Please fill in all fields")
            return false
        }

        if (selectedImageUri == null) {
            displayMessage("Please select your profile picture")
            return false
        }

        if (password != confirmPassword) {
            displayMessage("Passwords do not match")
            etPassword.text.clear()
            etConfirmPassword.text.clear()
            return false
        }

        // Add any other validation logic here (email format, password length, etc.)

        return true
    }

    private fun checkUsernameUniqueness(username: String, callback: (Boolean) -> Unit) {
        firestore.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.isEmpty)
            }
            .addOnFailureListener { e ->
                displayMessage("Error checking username uniqueness: ${e.message}")
            }
    }

    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/${UUID.randomUUID()}")
                    storageRef.putFile(selectedImageUri!!)
                        .addOnSuccessListener { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                val profileImageUri = uri.toString()
                                storeUserData(username, email, profileImageUri)
                            }
                        }
                } else {
                    displayMessage("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun storeUserData(username: String, email: String, profileImageUrl: String) {
        val user = hashMapOf("username" to username, "email" to email, "imageUrl" to profileImageUrl)
        firestore.collection("users").document(auth.currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                displayMessage("Registration successful!")
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToMain()
                }, 1500)
            }
            .addOnFailureListener { e ->
                displayMessage("Failed to store user data: ${e.message}")
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayMessage(message: String) {
        val rootView: View = findViewById(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        pickImageLauncher.launch(intent)
    }
}
