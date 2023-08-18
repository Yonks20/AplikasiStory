package com.project.submissioninter.page.splash

import androidx.lifecycle.ViewModel
import com.project.submissioninter.datasource.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {
    fun getAuthenticationToken(): Flow<String?> = userRepository.getAuthenticationToken()
}