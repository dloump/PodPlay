package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.BuildConfig
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.w3c.dom.Node
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService private constructor() {
    suspend fun getFeed(xmlFileURL: String): RssFeedResponse? {
        // 1
        val service: FeedService
        // 2
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        // 3
        val client = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            client.addInterceptor(interceptor)
        }
        client.build()
        // 4
        val retrofit = Retrofit.Builder()
            .baseUrl("${xmlFileURL.split("?")[0]}/")
            .build()
        service = retrofit.create(FeedService::class.java)
        // 5
        try {
            val result = service.getFeed(xmlFileURL)
            if (result.code() >= 400) {
                println("server error, ${result.code()}, ${result.errorBody()}")
                    return null
            } else {
                var rssFeedResponse : RssFeedResponse? = null
                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()
                withContext(Dispatchers.IO) {
                    val doc = dBuilder.parse(result.body()?.byteStream())
                    val rss = RssFeedResponse(episodes = mutableListOf())
                    domToRssFeedResponse(doc, rss)
                    println(rss)
                    rssFeedResponse = rss
                }
                return rssFeedResponse
            }
        } catch (t: Throwable) {
            println("error, ${t.localizedMessage}")
        }
        return null
    }
    companion object {
        val instance: RssFeedService by lazy {
            RssFeedService()
        }
    }
}

private fun domToRssFeedResponse(node: Node, rssFeedResponse:
RssFeedResponse) {
    //checking nodeType to make sure it’s an XML element
    if (node.nodeType == Node.ELEMENT_NODE) {
        //storing node’s name & parent name
        val nodeName = node.nodeName
        val parentName = node.parentNode.nodeName
        //if current node is child of channel node, extract top-level RSS feed
        //information from this node
        if (parentName == "channel") {
            //switching on nodeName, if node is episode item, adding
                //new empty EpisodeResponse object to episodes list
            when (nodeName) {
                "title" -> rssFeedResponse.title = node.textContent
                "description" -> rssFeedResponse.description =
                    node.textContent
                "itunes:summary" -> rssFeedResponse.summary =
                    node.textContent
                "item" -> rssFeedResponse.episodes?.
                add(RssFeedResponse.EpisodeResponse())
                "pubDate" -> rssFeedResponse.lastUpdated =
                    DateUtils.xmlDateToDate(node.textContent)
            }
        }
        //getting name of grandparent node
        val grandParentName = node.parentNode.parentNode?.nodeName ?: ""
        //if this node is a child of an item node, & item node is a child of
        //a channel node, then it is an episode element
        if (parentName == "item" && grandParentName == "channel") {
            //assigning currentItem to last episode in episodes list
            val currentItem = rssFeedResponse.episodes?.last()
            if (currentItem != null) {
                //switching on current node’s name, then based on node name,
                    //current episode item’s details are populated from node’s
                        //textContent property. If node is an enclosure, extracting
                            //url & type from node’s attributes & set them on currentItem
                when (nodeName) {
                    "title" -> currentItem.title = node.textContent
                    "description" -> currentItem.description =
                        node.textContent
                    "itunes:duration" -> currentItem.duration =
                        node.textContent
                    "guid" -> currentItem.guid = node.textContent
                    "pubDate" -> currentItem.pubDate = node.textContent
                    "link" -> currentItem.link = node.textContent
                    "enclosure" -> {
                        currentItem.url = node.attributes.getNamedItem("url")
                            .textContent
                        currentItem.type = node.attributes.getNamedItem("type")
                            .textContent
                    }
                }
            }
        }
    }
    //assigning nodeList to list of child nodes for current node
    val nodeList = node.childNodes
    for (i in 0 until nodeList.length) {
        val childNode = nodeList.item(i)
        //for each child node, calling domToRssFeedResponse(), passing in existing
        //rssFeedResponse object
        domToRssFeedResponse(childNode, rssFeedResponse)
    }
}

interface FeedService {
    @Headers(
        "Content-Type: application/xml; charset=utf-8",
        "Accept: application/xml"
    )
    @GET
    suspend fun getFeed(@Url xmlFileURL: String):
            Response<ResponseBody>
}