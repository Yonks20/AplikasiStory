package com.project.submissioninter.datasource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.submissioninter.datasource.localdata.StoryDatabase
import com.project.submissioninter.datasource.localdata.entity.StoryEntity
import com.project.submissioninter.datasource.remotedata.ApiServices
import com.project.submissioninter.datasource.remotedata.StoryRemoteMediator
import com.project.submissioninter.datasource.remotedata.response.AddStoryResponse
import com.project.submissioninter.datasource.remotedata.response.StoriesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@ExperimentalPagingApi
class StoryRepository @Inject constructor(
    private val apiServices: ApiServices,
    private val storyDatabase: StoryDatabase
) {

    fun getStories(token: String): Flow<PagingData<StoryEntity>> {
        val bearerToken = generateBearerToken(token)
        return Pager(
            config = PagingConfig(pageSize = 15),
            remoteMediator = StoryRemoteMediator(
                database = storyDatabase,
                apiServices = apiServices,
                token = bearerToken
            ),
            pagingSourceFactory = { storyDatabase.storyDao().getStories() }
        ).flow
    }

    fun getStoriesWithLocation(token: String): Flow<Result<StoriesResponse>> = flow {
            try {
                val bearerToken = generateBearerToken(token)
                val response =
                    apiServices.getStories(
                        token = bearerToken,
                        page = null, size = 20,
                        location = 1
                    )

                emit(Result.success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.failure(e))
            }
    }

    suspend fun addStory(token: String,
                         file: MultipartBody.Part,
                         description: RequestBody,
                         lat: RequestBody?,
                         lon: RequestBody?
    ) : Flow<Result<AddStoryResponse>> = flow {
        try {
            val bearerToken = generateBearerToken(token)
            val response = apiServices.addStory(
                token = bearerToken,
                file = file,
                description = description,
                lat = lat,
                lon = lon
            )

            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    private fun generateBearerToken(token : String): String = "Bearer $token"
}