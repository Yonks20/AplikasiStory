package com.project.submissioninter.page.story

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.submissioninter.databinding.ItemStoryBinding
import com.project.submissioninter.datasource.localdata.entity.StoryEntity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoryListAdapter(private val callback: StoryCallback) :
    PagingDataAdapter<StoryEntity, StoryListAdapter.ViewHolder>(
        DiffCallback
    ) {

    inner class ViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, story: StoryEntity) {
            binding.apply {
                tvName.text = story.name.lowercase()
                    .replaceFirstChar { it.titlecase() }

                tvDescription.text = story.description
                ivStory.setImageFromUrl(context, url = story.photoUrl)
                tvDate.setLocaleDateFormat(story.createdAt)

                root.setOnClickListener {
                    callback.onClickStory(
                        story = story,
                        itemBinding = binding
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(holder.itemView.context, story)
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}

fun ImageView.setImageFromUrl(
    context: Context,
    url: String,
    placeholder: Drawable? = null,
    error: Drawable? = null
) {

    Glide.with(context)
        .load(url)
        .placeholder(placeholder)
        .error(error)
        .into(this)
}

fun TextView.setLocaleDateFormat(timestamp: String) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    val date = dateFormat.parse(timestamp) as Date

    val resultDateFormatted = DateFormat.getDateInstance(DateFormat.FULL).format(date)
    this.text = resultDateFormatted
}
