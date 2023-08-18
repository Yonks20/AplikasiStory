package com.project.submissioninter.page.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.project.submissioninter.databinding.ActivityLauncherBinding
import com.project.submissioninter.page.login.LoginActivity
import com.project.submissioninter.page.story.StoryListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalPagingApi
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherBinding
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findUserSession()
    }

    private fun findUserSession() {
        this.lifecycleScope.launchWhenCreated {
            launch {
                splashViewModel.getAuthenticationToken().collect { token ->
                    delay(3000)
                    if (token.isNullOrEmpty()) {
                        val loginIntent = Intent(this@LauncherActivity, LoginActivity::class.java)
                        startActivity(loginIntent)

                    } else {
                        val storyIntent =
                            Intent(this@LauncherActivity, StoryListActivity::class.java)
                        startActivity(storyIntent)
                    }

                    finish()
                }
            }
        }

    }
}