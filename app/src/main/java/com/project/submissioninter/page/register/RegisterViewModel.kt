package com.project.submissioninter.page.register

import androidx.lifecycle.ViewModel
import com.project.submissioninter.datasource.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    suspend fun registerUser(name: String, email: String, password: String) =
        userRepository.userRegister(
            name = name,
            email = email,
            password = password
        )
}