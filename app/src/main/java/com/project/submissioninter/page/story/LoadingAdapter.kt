package com.project.submissioninter.page.story

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.submissioninter.R
import com.project.submissioninter.databinding.SubviewLoadingRecyclerviewBinding

class LoadingAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingAdapter.LoadingViewHolder>() {
    override fun onBindViewHolder(
        holder: LoadingAdapter.LoadingViewHolder,
        loadState: LoadState
    ) {
        holder.bind(holder.itemView.context, loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingAdapter.LoadingViewHolder {
        val binding = SubviewLoadingRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return LoadingViewHolder(binding, retry)
    }

    inner class LoadingViewHolder(
        private val binding: SubviewLoadingRecyclerviewBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnRetry.setOnClickListener { retry.invoke() }
        }

        fun bind(context: Context, loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.tvErrorMessage.text = context.getString(R.string.error_loading_message)
            }

            binding.apply {
                binding.ltLoading.isVisible = loadState is LoadState.Loading
                binding.btnRetry.isVisible = loadState is LoadState.Error
                binding.tvErrorMessage.isVisible = loadState is LoadState.Error
            }
        }
    }
}