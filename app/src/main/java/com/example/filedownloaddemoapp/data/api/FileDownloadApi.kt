package com.example.filedownloaddemoapp.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface FileDownloadApi {
    @Streaming
    @GET("50MB.zip")
    suspend fun downloadZipFile(): ResponseBody
}
