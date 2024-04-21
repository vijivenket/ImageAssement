package com.example.assessment.network

import com.example.assessment.model.LoadImageItem
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("media-coverages?limit=100")
    suspend fun getImageList(): Response<List<LoadImageItem>?>
}