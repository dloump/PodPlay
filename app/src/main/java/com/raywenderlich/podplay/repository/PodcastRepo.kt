package com.raywenderlich.podplay.repository

import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.RssFeedResponse
import com.raywenderlich.podplay.service.RssFeedService
import com.raywenderlich.podplay.util.DateUtils

class PodcastRepo(private var feedService: RssFeedService) {
    suspend fun getPodcast(feedUrl: String): Podcast? {
        var podcast: Podcast? = null
        val feedResponse = feedService.getFeed(feedUrl)
        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

    private fun rssItemsToEpisodes(
        episodeResponses: List<RssFeedResponse.EpisodeResponse>
    ): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                null,
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

    private fun rssResponseToPodcast(
        feedUrl: String, imageUrl: String, rssResponse:
        RssFeedResponse
    ): Podcast? {
        //assigning list of episodes to items if it’s not null, otherwise,
        //method returns null
        val items = rssResponse.episodes ?: return null
        //if description is empty, description property is set to response
        //summary, otherwise, it’s set to response description
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        //creating a new Podcast object using response data & returning it to caller
        return Podcast(null, feedUrl, rssResponse.title, description,
            imageUrl, rssResponse.lastUpdated,
            episodes = rssItemsToEpisodes(items))
    }

}