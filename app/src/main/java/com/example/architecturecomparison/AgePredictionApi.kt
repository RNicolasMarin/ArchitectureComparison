package com.example.architecturecomparison

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AgePredictionApi {

    @GET("/")
    suspend fun getAgePrediction(@Query("name") name: String) : Response<AgePrediction>
}