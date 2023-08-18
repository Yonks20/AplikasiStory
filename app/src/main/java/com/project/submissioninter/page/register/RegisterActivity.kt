package com.project.submissioninter.page.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.project.submissioninter.R
import com.project.submissioninter.databinding.ActivityRegisterBinding
import com.project.submissioninter.page.addstory.MediaHelper.visibilityAnimate
import com.project.submissioninter.page.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var jobRegister: Job = Job()
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUserActions()

    }

    private fun setUserActions() {
        binding.apply {
            btnRegister.setOnClickListener {
                registerUser()
            }

            tvLogin.setOnClickListener {
                val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(loginIntent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this@RegisterActivity)
                        .toBundle()
                )

                finish()
            }
        }
    }

    private fun registerUser() {
        showLoading(isLoading = true)
        var isAllFieldValid = true

        if (binding.edtEmail.text.isNullOrBlank() || !binding.edtEmail.error.isNullOrEmpty())
            isAllFieldValid = false

        if (binding.edtFullname.text.isNullOrBlank() || !binding.edtFullname.error.isNullOrEmpty())
            isAllFieldValid = false

        if (binding.edtPassword.text.isNullOrBlank() || !binding.edtPassword.error.isNullOrEmpty())
            isAllFieldValid = false

        if (isAllFieldValid) {
            val email = binding.edtEmail.text.toString().trim()
            val name = binding.edtFullname.text.toString()
            val password = binding.edtPassword.text.toString()

            this.lifecycleScope.launchWhenResumed {
                if (jobRegister.isActive) jobRegister.cancel()

                jobRegister = launch {
                    registerViewModel.registerUser(name = name, email = email, password = password)
                        .collect { result ->
                            result.onSuccess {
                                showLoading(isLoading = false)
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.success_register),
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                val loginIntent =
                                    Intent(this@RegisterActivity, LoginActivity::class.java)
                                startActivity(loginIntent,
                                    ActivityOptionsCompat.makeSceneTransitionAnimation(this@RegisterActivity)
                                        .toBundle()
                                )

                                finish()
                            }

                            result.onFailure {
                                showLoading(isLoading = false)
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.error_register),
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

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            edtEmail.isEnabled = !isLoading
            edtPassword.isEnabled = !isLoading
            edtFullname.isEnabled = !isLoading

            layoutLoading.root.visibilityAnimate(isLoading)
        }
    }
}
