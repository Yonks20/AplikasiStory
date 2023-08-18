package com.project.submissioninter.datasource.remotedata.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("loginResult")
    val loginResult: LoginResultResponse? = null,
)

data class LoginResultResponse(

    @field:SerializedName("token")
    val token: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("userId")
    val userId: String? = null,
)