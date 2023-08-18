package com.project.submissioninter.page.story

import com.project.submissioninter.databinding.ItemStoryBinding
import com.project.submissioninter.datasource.localdata.entity.StoryEntity

interface StoryCallback {
    fun onClickStory(story: StoryEntity, itemBinding: ItemStoryBinding)
}