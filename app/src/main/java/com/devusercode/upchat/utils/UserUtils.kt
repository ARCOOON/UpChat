package com.devusercode.upchat.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.devusercode.upchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

        class Result {
            var user: User?
            var error: Error?
            var code: ErrorCodes? = ErrorCodes.INIT

            constructor(user: User?, error: Error?, code: ErrorCodes?) {
                this.user = user
                this.error = error
                this.code = code
            }

            constructor(user: User, code: ErrorCodes) {
                this.user = user
                this.error = null
                this.code = code
            }

            constructor(user: User?) {
                this.user = user
                error = null
            }

            val isSuccessful: Boolean
                get() = error == null && user != null && code == ErrorCodes.SUCCESS
        }

        fun getUserByUid(uid: String, onFinish: Consumer<Result>?) {
            val usersRef = FirebaseDatabase.getInstance().getReference(REF).child(uid)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dataSnapshot = usersRef.get().await()

                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(User::class.java)

                        if (user != null) {
                            withContext(Dispatchers.Main) {
                                onFinish?.accept(Result(user, ErrorCodes.SUCCESS))
                            }
                        } else {
                            val error = Error("Unknown error while retrieving User ($uid)")
                            withContext(Dispatchers.Main) {
                                onFinish?.accept(Result(null, error, ErrorCodes.UNKNOWN_ERROR))
                            }
                        }
                    } else {
                        val error = Error("User not found ($uid)")
                        withContext(Dispatchers.Main) {
                            onFinish?.accept(Result(null, error, ErrorCodes.USER_NOT_FOUND))
                        }
                    }
                } catch (exception: Exception) {
                    val error = Error(exception.message ?: "Unknown error")
                    withContext(Dispatchers.Main) {
                        onFinish?.accept(Result(null, error, ErrorCodes.CANCELLED))
                    }
                }
            }
        }


        fun update(field: String?, value: Any?) {
            val user = FirebaseAuth.getInstance().currentUser ?: return

            val uid = user.uid
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)

            ref.child(field!!).setValue(value)
                .addOnFailureListener { error: Exception -> Log.e(TAG, error.message!!) }
        }
    }
}