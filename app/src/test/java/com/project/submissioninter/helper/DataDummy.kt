package com.project.submissioninter.helper

import com.project.submissioninter.datasource.localdata.entity.StoryEntity

object DataDummy {

    fun generateDummyListStory(): List<StoryEntity> {
        val items = arrayListOf<StoryEntity>()

        for(i in 0 until 10){
            val story = StoryEntity(
                id = "story_xx",
                photoUrl = "photo_url",
                createdAt = "2023-06-08T09:40:10.234Z",
                name = "John Doe",
                description = "Lorem Ipsum",
                lon = 10.502,
                lat = -10.512
            )
            items.add(story)
        }

        return items
    }
}