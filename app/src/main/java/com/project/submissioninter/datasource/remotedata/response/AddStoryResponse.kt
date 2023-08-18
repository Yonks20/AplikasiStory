package com.project.submissioninter.datasource.remotedata.response

import com.google.gson.annotations.SerializedName

data class AddStoryResponse(
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,
)