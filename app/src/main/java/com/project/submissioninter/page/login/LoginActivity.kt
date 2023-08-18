package com.project.submissioninter.page.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.project.submissioninter.R
import com.project.submissioninter.databinding.ActivityLoginBinding
import com.project.submissioninter.page.addstory.MediaHelper.visibilityAnimate
import com.project.submissioninter.page.register.RegisterActivity
import com.project.submissioninter.page.story.StoryListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var jobLogin: Job = Job()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUserActions()
    }

    private fun setUserActions(){
        binding.apply {
            btnLogin.setOnClickListener {
                userLogin()
            }
            tvRegister.setOnClickListener {
                val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(registerIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@LoginActivity).toBundle())

                finish()
            }
        }
    }

    private fun userLogin(){
        showLoading(isLoading = true)
        var isFormValid = true

        if (binding.edtEmail.text.isNullOrBlank() || !binding.edtEmail.error.isNullOrEmpty())
            isFormValid = false

        if (binding.edtPassword.text.isNullOrBlank() || !binding.edtPassword.error.isNullOrEmpty())
            isFormValid = false


        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString()

        if (jobLogin.isActive) jobLogin.cancel()

        if (isFormValid) {
            this.lifecycleScope.launchWhenResumed {
                jobLogin = launch {
                    loginViewModel.userLogin(email = email, password = password).collect { result ->
                        result.onSuccess { loginResponse ->
                            loginResponse.loginResult?.token?.let { token ->
                                loginViewModel.saveAuthenticationToken(token = token)

                                showLoading(isLoading = false)
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.success_login),
                                    Snackbar.LENGTH_SHORT
                                ).show()


                                //navigate to storylist
                                val storyListIntent = Intent(this@LoginActivity, StoryListActivity::class.java)
                                startActivity(storyListIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@LoginActivity).toBundle())

                                finish()
                            }
                        }

                        result.onFailure {
                            showLoading(isLoading = false)
                            Snackbar.make(
                                binding.root,
                                getString(R.string.error_login),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else {
            showLoading(isLoading = false)
            Snackbar.make(
                binding.root,
                getString(R.string.error_form_valid),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLoading(isLoading: Boolean){
        binding.apply {
            edtEmail.isEnabled = !isLoading
            edtPassword.isEnabled = !isLoading

            layoutLoading.root.visibilityAnimate(isLoading)
        }
    }
}

