package com.project.submissioninter.page.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.project.submissioninter.datasource.StoryRepository
import com.project.submissioninter.datasource.UserRepository
import com.project.submissioninter.datasource.localdata.entity.StoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class StoryListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun getStories(token: String): LiveData<PagingData<StoryEntity>> =
        storyRepository.getStories(token = token).cachedIn(viewModelScope).asLiveData()

    fun getAuthenticationToken(): Flow<String?> = userRepository.getAuthenticationToken()

    fun removeAuthenticationToken() {
        viewModelScope.launch {
            userRepository.removeAuthenticationToken()
        }
    }

}