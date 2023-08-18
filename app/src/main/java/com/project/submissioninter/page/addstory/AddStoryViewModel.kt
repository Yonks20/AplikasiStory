package com.project.submissioninter.page.addstory

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.project.submissioninter.datasource.StoryRepository
import com.project.submissioninter.datasource.UserRepository
import com.project.submissioninter.datasource.remotedata.response.AddStoryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AddStoryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun getAuthenticationToken(): Flow<String?> =
        userRepository.getAuthenticationToken()

    suspend fun addStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): Flow<Result<AddStoryResponse>> =
        storyRepository.addStory(
            token = token,
            file = file,
            description = description,
            lat = lat,
            lon = lon
        )


}