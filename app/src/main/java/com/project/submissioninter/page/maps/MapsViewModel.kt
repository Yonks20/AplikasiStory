package com.project.submissioninter.page.maps

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.project.submissioninter.datasource.StoryRepository
import com.project.submissioninter.datasource.UserRepository
import com.project.submissioninter.datasource.remotedata.response.StoriesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class MapsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) :
    ViewModel() {

    fun getStoriesWithLocation(token: String): Flow<Result<StoriesResponse>> =
        storyRepository.getStoriesWithLocation(token = token)

    fun getAuthenticationToken(): Flow<String?> = userRepository.getAuthenticationToken()
}