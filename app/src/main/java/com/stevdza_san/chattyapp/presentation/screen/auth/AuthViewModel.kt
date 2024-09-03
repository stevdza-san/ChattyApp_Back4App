package com.stevdza_san.chattyapp.presentation.screen.auth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.parse.ParseUser

class AuthViewModel : ViewModel() {
    private var _myUsername: MutableState<String> = mutableStateOf("")
    val myUsername: State<String> = _myUsername

    private var _myPassword: MutableState<String> = mutableStateOf("")
    val myPassword: State<String> = _myPassword

    fun setUsername(text: String) {
        _myUsername.value = text
    }

    fun setPassword(text: String) {
        _myPassword.value = text
    }

    fun onSignInClick(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (myUsername.value.isNotEmpty() && myPassword.value.isNotEmpty()) {
            val user = ParseUser().apply {
                username = myUsername.value
                setPassword(myPassword.value)
            }
            user.signUpInBackground { exception ->
                if (exception == null) {
                    loginTheUser(
                        onSuccess = onSuccess,
                        onError = onError
                    )
                } else {
                    ParseUser.logOut()
                    if (exception.message != null) {
                        if (exception.message!!.contains("Account already exists for this username.")) {
                            loginTheUser(
                                onSuccess = onSuccess,
                                onError = onError
                            )
                        } else {
                            onError(exception.message ?: "Fatal error.")
                        }
                    }
                }
            }
        } else {
            onError("Fields are empty.")
        }
    }

    private fun loginTheUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        ParseUser.logInInBackground(
            myUsername.value,
            myPassword.value
        ) { parseUser, parseException ->
            if (parseUser != null) {
                onSuccess()
            } else {
                ParseUser.logOut()
                onError(parseException.message ?: "Fatal error.")
            }
        }
    }
}