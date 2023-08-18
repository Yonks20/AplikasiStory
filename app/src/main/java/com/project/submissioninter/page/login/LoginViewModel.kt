package com.project.submissioninter.page.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.submissioninter.datasource.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    suspend fun userLogin(email: String, password: String) =
        userRepository.userLogin(
            email = email,
            password = password
        )

    fun saveAuthenticationToken(token: String) =
        viewModelScope.launch {
            userRepository.saveAuthenticationToken(token = token)
        }
}