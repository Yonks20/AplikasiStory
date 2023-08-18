package com.project.submissioninter.datasource

import com.project.submissioninter.datasource.localdata.LocalDataSource
import com.project.submissioninter.datasource.remotedata.ApiServices
import com.project.submissioninter.datasource.remotedata.response.LoginResponse
import com.project.submissioninter.datasource.remotedata.response.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiServices: ApiServices,
    private val localDataSource: LocalDataSource
) {
    suspend fun userLogin(email: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            val response = apiServices.userLogin(
                email = email,
                password = password
            )

            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun userRegister(name: String, email: String, password: String): Flow<Result<RegisterResponse>> = flow {
        try {
            val response = apiServices.userRegister(
                name = name,
                email = email,
                password = password
            )

            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun saveAuthenticationToken(token: String){
        localDataSource.saveAuthenticationToken(token)
    }

    suspend fun removeAuthenticationToken(){
        localDataSource.removeAuthenticationToken()
    }

    fun getAuthenticationToken(): Flow<String?> = localDataSource.getAuthenticationToken()

}