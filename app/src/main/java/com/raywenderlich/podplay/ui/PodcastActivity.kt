package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.databinding.ActivityPodcastBinding
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.service.RssFeedService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastActivity : AppCompatActivity(),
    PodcastListAdapter.PodcastListAdapterListener {

    private val podcastViewModel by viewModels<PodcastViewModel>()
    private lateinit var searchMenuItem: MenuItem
    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var binding: ActivityPodcastBinding
    val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)

        GlobalScope.launch {
            val results = itunesRepo.searchByTerm("Android Developer")
            Log.i(TAG, "Results = ${results.body()}")
            }

        setupViewModels()
        updateControls()
        setupToolbar()
        handleIntent(intent)
        addBackStackListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //
        //inflating options menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        //creating search action menu item (found within the options menu), & search
        //view is taken from item’s actionView property
        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView
        //loading SearchManager object
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        //loading search configuration & assigning it to searchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        if (supportFragmentManager.backStackEntryCount > 0) {
            binding.podcastRecyclerView.visibility = View.INVISIBLE
        }
        if (binding.podcastRecyclerView.visibility ==
            View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }

        return true
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?:
            return
            performSearch(query)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel.iTunesRepo = ItunesRepo(service)
        podcastViewModel.podcastRepo = PodcastRepo(RssFeedService.instance)
    }

    private fun updateControls() {
        binding.podcastRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        binding.podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            binding.podcastRecyclerView.context,
            layoutManager.orientation)

        binding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(null, this, this)
        binding.podcastRecyclerView.adapter = podcastListAdapter
    }

    override fun onShowDetails(podcastSummaryViewData:
                               SearchViewModel.PodcastSummaryViewData) {
        podcastSummaryViewData.feedUrl?.let {
            showProgressBar()
            podcastViewModel.getPodcast(podcastSummaryViewData)
        }
    }

    private fun createSubscription() {
        podcastViewModel.podcastLiveData.observe(this, {
            hideProgressBar()
            if (it != null) {
                showDetailsFragment()
            } else {
                showError("Error loading feed")
            }
        })
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun performSearch(term: String) {
        showProgressBar()
        GlobalScope.launch {
            val results = searchViewModel.searchPodcasts(term)
            withContext(Dispatchers.Main) {
                hideProgressBar()
                binding.toolbar.title = term
                podcastListAdapter.setSearchData(results)
            }
        }
    }

    private fun createPodcastDetailsFragment():
            PodcastDetailsFragment {
        //checking if Fragment already exists
        var podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as
                PodcastDetailsFragment?
        //if no existing fragment, creating a new one using newInstance()
        //on Fragment’s companion object
        if (podcastDetailsFragment == null) {
            podcastDetailsFragment =
                PodcastDetailsFragment.newInstance()
        }
        //returning fragment object
        return podcastDetailsFragment
    }

    private fun showDetailsFragment() {
        //creating or retrieving details fragment from fragment manager
        val podcastDetailsFragment = createPodcastDetailsFragment()
        //adding fragment to supportFragmentManager.
        supportFragmentManager.beginTransaction().add(
            R.id.podcastDetailsContainer,
            podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment").commit()
        //hiding main podcast RecyclerView so only thing showing is detail Fragment
        binding.podcastRecyclerView.visibility = View.INVISIBLE
        //hiding searchMenuItem so that search icon is not shown on details screen
        searchMenuItem.isVisible = false
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button), null)
            .create()
            .show()
    }

    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }

}