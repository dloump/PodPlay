package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.PodcastResponse
import com.raywenderlich.podplay.util.DateUtils

class SearchViewModel(application: Application) :
    AndroidViewModel(application) {

    var iTunesRepo: ItunesRepo? = null

    data class PodcastSummaryViewData(
        var name: String? = "",
        var lastUpdated: String? = "",
        var imageUrl: String? = "",
        var feedUrl: String? = "")

    private fun itunesPodcastToPodcastSummaryView(
        itunesPodcast: PodcastResponse.ItunesPodcast):
            PodcastSummaryViewData {
        return PodcastSummaryViewData(
            itunesPodcast.collectionCensoredName,
            DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
            itunesPodcast.artworkUrl30,
            itunesPodcast.feedUrl)
    }

    //first parameter is search term  (iTunes repo’s search method
    //runs asynchronously, so method needs suspend keyword)
    suspend fun searchPodcasts(term: String):
            List<PodcastSummaryViewData> {
        //iTunesRepo is used to perform search asynchronously
        val results = iTunesRepo?.searchByTerm(term)
        //checking if results are not null & call is successful
        if (results != null && results.isSuccessful) {
            //getting podcasts from body
            val podcasts = results.body()?.results
            //checking if podcasts list is not empty
            if (!podcasts.isNullOrEmpty()) {
                //mapping podcasts to PodcastSummaryViewData objects
                return podcasts.map { podcast ->
                    itunesPodcastToPodcastSummaryView(podcast)
                }
            }
        }
        //if results are null, returning an empty list
        return emptyList()
    }
}