package com.project.submissioninter.datasource.localdata

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.submissioninter.datasource.localdata.entity.RemoteKeysEntity
import com.project.submissioninter.datasource.localdata.entity.StoryEntity

@Database(
    entities =
        [
            RemoteKeysEntity::class,
            StoryEntity::class
        ],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}