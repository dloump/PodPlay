package com.raywenderlich.podplay.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.databinding.FragmentPodcastDetailsBinding
import com.raywenderlich.podplay.viewmodel.PodcastViewModel

class PodcastDetailsFragment : Fragment() {

    private lateinit var episodeListAdapter: EpisodeListAdapter
    private val podcastViewModel: PodcastViewModel by
    activityViewModels()

    private lateinit var binding:
            FragmentPodcastDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //telling Android that this Fragment wants to add items to options menu
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container:
    ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding =
            FragmentPodcastDetailsBinding.inflate(inflater, container,
                false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState:
    Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner,
            { viewData ->
                if (viewData != null) {
                    binding.feedTitleTextView.text = viewData.feedTitle
                    binding.feedDescTextView.text = viewData.feedDesc
                    activity?.let { activity ->

                        Glide.with(activity).load(viewData.imageUrl).into(binding.feedImageView)
                    }
                    //allowing feed title to scroll if it gets too long for its container
                    binding.feedDescTextView.movementMethod =
                        ScrollingMovementMethod()
                    //setup code for episode list RecyclerView
                    binding.episodeRecyclerView.setHasFixedSize(true)
                    val layoutManager = LinearLayoutManager(activity)
                    binding.episodeRecyclerView.layoutManager =
                        layoutManager
                    val dividerItemDecoration = DividerItemDecoration(
                        binding.episodeRecyclerView.context,
                        layoutManager.orientation)

                    binding.episodeRecyclerView.addItemDecoration(dividerItemDecoration)
                    //creating EpisodelistAdapter with list of episodes in
                    //activePodcastViewData & assigning it to episodeRecyclerView
                    episodeListAdapter =
                        EpisodeListAdapter(viewData.episodes)
                    binding.episodeRecyclerView.adapter =
                        episodeListAdapter
                }
            })
    }

    //inflating menu_details options menu so its items
    //are added to podcast Activity menu
    override fun onCreateOptionsMenu(menu: Menu, inflater:
    MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    private fun updateControls() {
        val viewData = podcastViewModel.activePodcastViewData ?:
        return
        binding.feedTitleTextView.text = viewData.feedTitle
        binding.feedDescTextView.text = viewData.feedDesc
        activity?.let { activity ->

            Glide.with(activity).load(viewData.imageUrl).into(binding.feedImageView)
        }
    }

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

}