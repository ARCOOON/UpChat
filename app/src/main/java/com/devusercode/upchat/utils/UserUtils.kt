package com.devusercode.upchat.utils

import android.util.Log
import com.devusercode.upchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.function.Consumer

enum class ErrorCodes(val message: String) {
    USER_NOT_FOUND("User not found"),
    SUCCESS("Success"),
    UNKNOWN_ERROR("Unknown error"),
    INIT("Initialization error"),
    CANCELLED("Operation cancelled"),
    NO_PROP_TO_SERIALIZE("No properties to serialize found on class"),
    NO_PROP_TO_SERIALIZE_FOR_FIELD("No properties to serialize for field"),
}

class UserUtils {
    companion object {
        private const val TAG = "UserUtils"
        private const val REF = "users"

        // Result class to encapsulate user-fetching results
        class Result(
            val user: User?,
            val error: Error?,
            val code: ErrorCodes
        ) {
            constructor(user: User, code: ErrorCodes) : this(user, null, code)
            constructor(error: Error, code: ErrorCodes) : this(null, error, code)

            val isSuccessful: Boolean
                get() = user != null && error == null && code == ErrorCodes.SUCCESS
        }

        /**
         * Fetch user by UID using callback.
         * @param uid User's unique ID.
         * @param onFinish Callback to handle the result.
         */
        fun getUserByUid(uid: String, onFinish: Consumer<Result>?) {
            val usersRef = FirebaseDatabase.getInstance().getReference(REF).child(uid)

            CoroutineScope(Dispatchers.IO).launch {
                val result = fetchUser(uid)
                withContext(Dispatchers.Main) {
                    onFinish?.accept(result)
                }
            }
        }

        /**
         * Fetch user by UID as a suspend function.
         * @param uid User's unique ID.
         * @return Result containing user or error.
         */
        suspend fun getUserByUidAsync(uid: String): Result {
            return fetchUser(uid)
        }

        /**
         * Update a specific field for the current user.
         * @param field The field to update.
         * @param value The new value.
         */
        fun update(field: String?, value: Any?) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null || field == null || value == null) {
                Log.e(TAG, "Invalid input or user is not authenticated")
                return
            }

            val uid = currentUser.uid
            val ref = FirebaseDatabase.getInstance().reference.child(REF).child(uid)

            ref.child(field).setValue(value)
                .addOnFailureListener { error -> Log.e(TAG, "Update failed: ${error.message}") }
        }

        // Internal helper function to fetch user
        private suspend fun fetchUser(uid: String): Result {
            val usersRef = FirebaseDatabase.getInstance().getReference(REF).child(uid)

            return try {
                val dataSnapshot = usersRef.get().await()
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Result(user, ErrorCodes.SUCCESS)
                    } else {
                        Result(Error("Failed to parse user data"), ErrorCodes.UNKNOWN_ERROR)
                    }
                } else {
                    Result(Error("User not found: $uid"), ErrorCodes.USER_NOT_FOUND)
                }
            } catch (exception: Exception) {
                Result(Error(exception.message ?: "Unknown error"), ErrorCodes.CANCELLED)
            }
        }
    }
}
