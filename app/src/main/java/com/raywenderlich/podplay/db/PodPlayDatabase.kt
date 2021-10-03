package com.raywenderlich.podplay.db

import android.content.Context
import androidx.room.*
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import kotlinx.coroutines.CoroutineScope
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return (date?.time)
    }
}

//defining PodPlayDatabase as abstract class that implements
//RoomDatabase interface. (@Database annotation is used to define this as a
//Room database with two tables: Podcast & Episode.)
@Database(entities = [Podcast::class, Episode::class], version = 1)
@TypeConverters(Converters::class)
abstract class PodPlayDatabase : RoomDatabase() {
    //defining abstract method podcastDao to return a PodcastDao object
    abstract fun podcastDao(): PodcastDao

    //defining companion object to hold single instance of PodPlayDatabase
    companion object {
        //single instance of PodPlayDatabase is defined & set to null,
        //& marking JVM backing field of annotated property as volatile
        @Volatile
        private var INSTANCE: PodPlayDatabase? = null
        //returning a single application-wide instance of PodPlayDatabase
        fun getInstance(context: Context, coroutineScope:
        CoroutineScope
        ): PodPlayDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            //If an instance of PodPlayDatabase hasn’t been created, it’s created.
            //using Room.databaseBuilder() to instantiate PodPlayDatabase object
            synchronized(this) {
                val instance =
                    Room.databaseBuilder(context.applicationContext,
                        PodPlayDatabase::class.java,
                        "PodPlayer")
                        .build()
                INSTANCE = instance
                //returning PodPlayDatabase object to caller
                return instance
            }
        }
    }
}