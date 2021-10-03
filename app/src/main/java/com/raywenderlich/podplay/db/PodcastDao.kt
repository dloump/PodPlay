package com.raywenderlich.podplay.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast

//defining PodcastDao interface
@Dao
interface PodcastDao {
    //loading all podcasts from database & returning a LiveData object
    @Query("SELECT * FROM Podcast ORDER BY FeedTitle")
    fun loadPodcasts(): LiveData<List<Podcast>>
    //loading all episodes from database,
    //sorted by release date in descending order
    @Query("SELECT * FROM Episode WHERE podcastId = :podcastId ORDER BY releaseDate DESC")
            suspend fun loadEpisodes(podcastId: Long): List<Episode>
        //inserting a single podcast into database
        @Insert(onConflict = REPLACE)
        suspend fun insertPodcast(podcast: Podcast): Long
        //inserting a single episode into database
        @Insert(onConflict = REPLACE)
        suspend fun insertEpisode(episode: Episode): Long
}