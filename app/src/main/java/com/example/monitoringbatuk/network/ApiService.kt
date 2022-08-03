package com.example.monitoringbatuk.network

import com.example.monitoringbatuk.model.RawResultResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

//    @GET("v1/api/83092/raw-data/79304402")
//    fun getRawResult(
//        @Header("x-api-key") apiKey: String,
//        @Header("Accept") json: String
//    ): Call<RawResultResponse>
//
    @GET("v1/api/82128/raw-data/80571655?limitPayloadValues=10")
    fun getRawResult(
        @Header("x-api-key") apiKey: String,
        @Header("Accept") json: String
    ): Call<RawResultResponse>

}