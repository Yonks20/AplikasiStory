package com.project.submissioninter.di

import com.project.submissioninter.datasource.remotedata.ApiConfiguration
import com.project.submissioninter.datasource.remotedata.ApiServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun provideApiServices(): ApiServices = ApiConfiguration.getApiServices()
}