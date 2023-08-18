@file:OptIn(ExperimentalPagingApi::class)

package com.project.submissioninter.page.story

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Window
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.submissioninter.R
import com.project.submissioninter.databinding.ActivityListStoryBinding
import com.project.submissioninter.databinding.ItemStoryBinding
import com.project.submissioninter.databinding.SubviewDialogConfirmationBinding
import com.project.submissioninter.datasource.localdata.entity.StoryEntity
import com.project.submissioninter.page.addstory.AddStoryActivity
import com.project.submissioninter.page.detail.DetailStoryActivity
import com.project.submissioninter.page.login.LoginActivity
import com.project.submissioninter.page.maps.MapsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import androidx.core.util.Pair
import com.project.submissioninter.page.addstory.MediaHelper.visibilityAnimate

@OptIn(ExperimentalPagingApi::class)
@AndroidEntryPoint
class StoryListActivity : AppCompatActivity(), StoryCallback {
    private lateinit var binding: ActivityListStoryBinding


    private val storyListViewModel: StoryListViewModel by viewModels()
    private lateinit var storyRecyclerView: RecyclerView

    private lateinit var storyListAdapter: StoryListAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setStories()
        setUserActions()
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun setStories() {
        this@StoryListActivity.lifecycleScope.launchWhenCreated {
            storyListViewModel.getAuthenticationToken().collect { token ->
                if (!token.isNullOrEmpty()) {
                    this@StoryListActivity.token = token
                    setSwipeRefresh()
                    setupStoryRecyclerView()
                    getStories()

                }
            }
        }
    }

    private fun showConfirmationDialog(message: String) {
        val dialogBinding = SubviewDialogConfirmationBinding.inflate(layoutInflater)

        val dialog = Dialog(this@StoryListActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.apply {
            tvDialogMessage.text = message

            btnYes.setOnClickListener {
                Dispatchers.Main.apply {
                    storyListViewModel.removeAuthenticationToken()
                    dialog.dismiss()


                    val loginIntent = Intent(this@StoryListActivity, LoginActivity::class.java)
                    startActivity(loginIntent)


                }
                this@StoryListActivity.finish()
            }

            btnNo.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun setupToolbar() {
        val appCompatActivity = this@StoryListActivity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbarStory)
        appCompatActivity.supportActionBar?.setDisplayShowTitleEnabled(false)

        val menuHost: MenuHost = this@StoryListActivity
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_storylist, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_logout -> {
                        showConfirmationDialog(getString(R.string.dialog_logout))
                        true
                    }

                    R.id.menu_item_maps -> {
                        val mapsIntent = Intent(this@StoryListActivity, MapsActivity::class.java)
                        startActivity(mapsIntent)


                        true
                    }

                    else -> false
                }
            }
        }, this@StoryListActivity, Lifecycle.State.RESUMED)
    }

    private fun setUserActions() {
        binding.fabCreateStory.setOnClickListener {
            //navigate to add story

            val addStoryIntent = Intent(this@StoryListActivity, AddStoryActivity::class.java)
            startActivity(addStoryIntent)
        }
    }

    private fun setSwipeRefresh() {
        binding.srlStory.setOnRefreshListener {
            getStories()
        }
    }

    override fun onResume() {
        super.onResume()
        getStories()
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun getStories() {
        storyListViewModel.getStories(token = token).observe(this@StoryListActivity) { result ->
            updateRecyclerViewStoryData(result)
        }
    }

    private fun setupStoryRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this@StoryListActivity)
        storyListAdapter = StoryListAdapter(this@StoryListActivity)

        storyListAdapter.addLoadStateListener { loadState ->
            if (
                (loadState.source.refresh is LoadState.NotLoading &&
                        loadState.append.endOfPaginationReached &&
                        storyListAdapter.itemCount < 1) ||
                (loadState.source.refresh is LoadState.Error)
            ) {
                showError(isError = true)
            } else {
                showError(isError = false)
            }

            binding.srlStory.isRefreshing = loadState.source.refresh is LoadState.Loading
        }

        try {
            storyRecyclerView = binding.rvStory
            storyRecyclerView.apply {
                adapter = storyListAdapter.withLoadStateFooter(
                    footer = LoadingAdapter {
                        storyListAdapter.retry()
                    }
                )
                layoutManager = linearLayoutManager
            }
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun showError(isError: Boolean) {
        binding.layoutError.root.visibilityAnimate(isError)
        binding.rvStory.visibilityAnimate(!isError)
    }

    private fun updateRecyclerViewStoryData(stories: PagingData<StoryEntity>) {
        val recyclerViewState = storyRecyclerView.layoutManager?.onSaveInstanceState()
        storyListAdapter.submitData(lifecycle, stories)
        storyRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }


    override fun onClickStory(story: StoryEntity, itemBinding: ItemStoryBinding) {
        itemBinding.apply {
            //navigate to detail
            val optionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@StoryListActivity,
                    Pair(ivStory, "image_story"),
                    Pair(tvName, "name"),
                    Pair(tvDescription, "description"),
                    Pair(tvDate, "date"),
                )

            val detailStoryIntent = Intent(this@StoryListActivity, DetailStoryActivity::class.java)
            detailStoryIntent.putExtra(STORY_KEY, story)
            startActivity(detailStoryIntent, optionsCompat.toBundle())


        }
    }

    companion object {
        const val STORY_KEY = "story_key"
    }
}