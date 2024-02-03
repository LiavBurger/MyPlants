package com.myplants

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.squareup.picasso.Picasso
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                Picasso.get().load(selectedImageUri).into(profileImageView)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        profileImageView = view.findViewById(R.id.profileImage)
        usernameEditText = view.findViewById(R.id.etUserName)

        view.findViewById<FloatingActionButton>(R.id.fabtnCamera).setOnClickListener {
            openGalleryForImage()
        }

        view.findViewById<Button>(R.id.btnSaveChanges).setOnClickListener {
            saveUserProfile()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            findNavController().popBackStack()
        }

        // Load existing user profile data
        loadUserProfile()

        return view
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveUserProfile() {
        val userName = usernameEditText.text.toString().trim()

        if (userName.isEmpty()) {
            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the username is already taken
        db.collection("users")
            .whereEqualTo("username", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Username is available, proceed with profile update
                    if (selectedImageUri != null) {
                        uploadImageAndUpdateProfile(userName)
                    } else {
                        updateProfileData(userName, null)
                    }
                } else {
                    // Check if the username found is not the current user's
                    var isUsernameTaken = false
                    for (document in documents) {
                        if (document.id != userId) {
                            isUsernameTaken = true
                            break
                        }
                    }

                    if (isUsernameTaken) {
                        Toast.makeText(context, "Username is already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        // Username is the same as the current user's, proceed with profile update
                        if (selectedImageUri != null) {
                            uploadImageAndUpdateProfile(userName)
                        } else {
                            updateProfileData(userName, null)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error checking username availability: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun uploadImageAndUpdateProfile(userName: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages/$userId")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    updateProfileData(userName, imageUrl)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error uploading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfileData(username: String, imageUrl: String?) {
        val userUpdateMap = hashMapOf<String, Any>("username" to username)
        imageUrl?.let { userUpdateMap["profileImageUrl"] = it }

        db.collection("users").document(userId)
            .update(userUpdateMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        // Fetch user data from Firestore and populate the fields
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userName = document.getString("username")
                val profileImageUrl = document.getString("imageUrl")
                usernameEditText.setText(userName)
                Picasso.get().load(profileImageUrl).into(profileImageView)
            }
        }.addOnFailureListener { e ->
            Log.e("ProfileFragment", "Error loading user profile", e)
        }
    }
}
