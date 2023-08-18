package com.project.submissioninter.page.detail

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.project.submissioninter.databinding.ActivityDetailStoryBinding
import com.project.submissioninter.datasource.localdata.entity.StoryEntity
import com.project.submissioninter.page.story.StoryListActivity.Companion.STORY_KEY
import com.project.submissioninter.page.story.setLocaleDateFormat

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val story = intent.getParcelableExtra<StoryEntity>(STORY_KEY)
        if (story != null) {
            setStory(story)
            setupToolbar()
        }
    }

    private fun setStory(story: StoryEntity) {
        binding.apply {
            tvName.text = story.name
            tvDescription.text = story.description
            tvDate.setLocaleDateFormat(story.createdAt)

            Glide
                .with(this@DetailStoryActivity)
                .load(story.photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        this@DetailStoryActivity.supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        this@DetailStoryActivity.supportStartPostponedEnterTransition()
                        return false
                    }
                })
                .into(ivStory)
        }
    }

    private fun setupToolbar() {
        val appCompatActivity = this@DetailStoryActivity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbarDetailStory)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompatActivity.supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbarDetailStory.setNavigationOnClickListener {
            finish()
        }
    }


}