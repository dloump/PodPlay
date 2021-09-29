package com.raywenderlich.podplay.repository

import com.raywenderlich.podplay.service.ItunesService

//defining the primary constructor for ItunesRepo to require an existing
//instance of ItunesService interface (This is an example of Dependency Injection principle)
class ItunesRepo(private val itunesService: ItunesService) {
    //method taking User's search term as parameter
    suspend fun searchByTerm(term: String) =
        itunesService.searchPodcastByTerm(term) //calling method & passing in search term
    //returning Retrofit Response object of PodcastResponse
}