package com.raywenderlich.podplay.service

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesService {
    //adding retrofit annotation & calling the path of the endpoint
    @GET("/search?media=podcast")
    //telling Retrofit that parameter should be added as query term in path
    //defined by @GET annotation
    suspend fun searchPodcastByTerm(@Query("term") term: String):
            Response<PodcastResponse>
    //defining companion object
    companion object {
        //creating application-wide ItunesService instance
        val instance: ItunesService by lazy {
            //creating a retrofit builder object,
            //specifying baseUrl & addConverterFactory options
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            //calling to create the ItunesService instance
            retrofit.create(ItunesService::class.java)
        }
    }
}