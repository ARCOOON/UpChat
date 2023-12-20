package com.devusercode.upchat.utils

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class DatabaseUtil {
    companion object {
        private const val TAG = "DatabaseUtil"
        private val auth = FirebaseAuth.getInstance()
        private val storage = FirebaseStorage.getInstance()
        private val database = FirebaseDatabase.getInstance()

        fun uploadProfileImage(path: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
            val profileImages = FirebaseStorage.getInstance().getReference("profile_images")
            val imageRef = profileImages.child("${auth.currentUser?.uid}.png")

            imageRef.putFile(path).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { error ->
                    onFailure(error)
                }
            }.addOnFailureListener { error ->
                onFailure(error)
            }
        }

        fun deleteProfileImage(onFailure: (Exception) -> Unit) {
            val profileImages = FirebaseStorage.getInstance().getReference("profile_images")
            profileImages.child("${auth.currentUser?.uid}.png").delete()
                .addOnFailureListener { error -> onFailure(error) }
        }

        fun deleteUser(onFailure: (Exception) -> Unit) {
            val users: DatabaseReference = database.reference.child("users")

            users.child(auth.currentUser!!.uid).removeValue()
                .addOnFailureListener { error -> onFailure(error) }
        }

        fun uploadImage(uploadPath: String, imageUri: Uri, fileName: String? = null, onResult: (Any) -> Unit) {
            val imagesRef = storage.reference.child(uploadPath)
            val finalFileName = fileName ?: (UUID.randomUUID().toString() + ".png")
            val imageRef: StorageReference = imagesRef.child(finalFileName)

            imageRef.putFile(imageUri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onResult(uri.toString())
                }.addOnFailureListener {
                    onResult("Failed to get download URL")
                }
            }.addOnFailureListener { error ->
                Log.e(TAG, error.message!!)
                onResult("Failed to upload image")
            }
        }
    }
}