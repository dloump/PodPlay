package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import java.util.*

class PodcastViewModel(application: Application) :
    AndroidViewModel(application) {
    var podcastRepo: PodcastRepo? = null
    var activePodcastViewData: PodcastViewData? = null
    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>
    )
    data class EpisodeViewData (
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = ""
    )

    private fun episodesToEpisodesView(episodes: List<Episode>):
            List<EpisodeViewData> {
        return episodes.map {
            EpisodeViewData(
                it.guid,
                it.title,
                it.description,
                it.mediaUrl,
                it.releaseDate,
                it.duration
            )
        }
    }

    private fun podcastToPodcastView(podcast: Podcast):
            PodcastViewData {
        return PodcastViewData(
            false,
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodesToEpisodesView(podcast.episodes)
        )
    }

    //takeing a PodcastSummaryViewData object & returning PodcastViewData or null
    fun getPodcast(podcastSummaryViewData:
                   SearchViewModel.PodcastSummaryViewData
    ): PodcastViewData? {
        //assigning local variables to podcastRepo &
        //podcastSummaryViewData.feedUrl. If either is null, method returns early
        val repo = podcastRepo ?: return null
        val feedUrl = podcastSummaryViewData.feedUrl ?: return null
        //calling getPodcast() from podcast repo with feed URL
        val podcast = repo.getPodcast(feedUrl)
        //checking podcast detail object to make sure it’s not null
        podcast?.let {
            //setting podcast title to podcast summary name
            it.feedTitle = podcastSummaryViewData.name ?: ""
            //setting podcast detail image to match podcast summary image URL if it’s not null
            it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
            //converting Podcast object to a PodcastViewData object & assigning it to
            //activePodcastViewData
            activePodcastViewData = podcastToPodcastView(it)
            //returning podcast view data
            return activePodcastViewData
        }
        //returning null if no podcast is retrieved
        return null
    }

}