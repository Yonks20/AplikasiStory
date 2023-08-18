package com.project.submissioninter.page.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.project.submissioninter.R
import com.project.submissioninter.databinding.ActivityAddStoryBinding
import com.project.submissioninter.page.addstory.MediaHelper.getNameLocation
import com.project.submissioninter.page.addstory.MediaHelper.reduceFileImage
import com.project.submissioninter.page.addstory.MediaHelper.convertUriToFile
import com.project.submissioninter.page.addstory.MediaHelper.visibilityAnimate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@ExperimentalPagingApi
@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding

    private lateinit var currentPhotoPath: String
    private var getFile: File? = null
    private var token: String = ""

    private val addStoryViewModel: AddStoryViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null

    private val launcherCameraIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val file = File(currentPhotoPath).also { getFile = it }
                val os: OutputStream

                val bitmap = BitmapFactory.decodeFile(getFile?.path)
                val exif = ExifInterface(currentPhotoPath)
                val orientation: Int = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )

                val rotatedBitmap: Bitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> TransformationUtils.rotateImage(
                        bitmap,
                        90
                    )

                    ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(
                        bitmap,
                        180
                    )

                    ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(
                        bitmap,
                        270
                    )

                    ExifInterface.ORIENTATION_NORMAL -> bitmap
                    else -> bitmap
                }
                try {
                    Dispatchers.Main.apply {
                        os = FileOutputStream(file)
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                        os.flush()
                        os.close()
                    }
                    getFile = file
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.ivStory.setImageBitmap(rotatedBitmap)
            }
        }

    private val launcherGalleryIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImg: Uri = result.data?.data as Uri
                convertUriToFile(selectedImg, this@AddStoryActivity).also { getFile = it }

                binding.ivStory.setImageURI(selectedImg)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getUserToken()
        setupToolbar()
        setUserActions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@AddStoryActivity)
        binding.swLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLocation()
                binding.tvLocation.visibility = View.VISIBLE
            } else {
                this.location = null
                binding.tvLocation.text = ""
                binding.tvLocation.visibility = View.GONE
            }
        }


    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this@AddStoryActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this.location = location
                    binding.tvLocation.text = getNameLocation(
                        context = this@AddStoryActivity,
                        lat = location.latitude,
                        lon = location.longitude
                    )
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        getString(R.string.activate_location),
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.swLocation.isChecked = false
                }
            }
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getLocation()
            }

            else -> {
                Snackbar
                    .make(
                        binding.root,
                        getString(R.string.activate_location),
                        Snackbar.LENGTH_SHORT
                    )
                    .setActionTextColor(
                        ContextCompat.getColor(
                            this@AddStoryActivity,
                            R.color.white
                        )
                    )
                    .setAction("Change Settings") {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                            val uri =
                                Uri.fromParts("package", this@AddStoryActivity.packageName, null)
                            intent.data = uri

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    .show()

                binding.swLocation.isChecked = false
            }
        }
    }

    private fun getUserToken() {
        this.lifecycleScope.launchWhenCreated {
            addStoryViewModel.getAuthenticationToken().collect { token ->
                if (!token.isNullOrEmpty()) {
                    this@AddStoryActivity.token = token
                }
            }
        }
    }

    private fun setupToolbar() {
        val appCompatActivity = this@AddStoryActivity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.tblAddStory)
        appCompatActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.ttl_add_story)
        }

        binding.tblAddStory.setNavigationOnClickListener {
            finish()
        }

        val menuHost: MenuHost = this@AddStoryActivity

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_addstory, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_save -> {
                        addStory()
                        true
                    }

                    else -> false
                }
            }
        }, this@AddStoryActivity, Lifecycle.State.RESUMED)
    }

    private fun setUserActions() {
        binding.btnCamera.setOnClickListener { startCameraIntent() }
        binding.btnGallery.setOnClickListener { startGalleryIntent() }
    }

    private fun startCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        MediaHelper.newTempFile(this@AddStoryActivity).also {
            val photoUri =
                FileProvider.getUriForFile(this@AddStoryActivity, "com.project.submissioninter", it)

            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            launcherCameraIntent.launch(intent)
        }
    }

    private fun addStory() {
        var isAllFieldValid = true
        showLoading(isLoading = true)

        if (binding.edtStory.text.isNullOrBlank()) {
            binding.edtStory.error = getString(R.string.fld_story_error)
            isAllFieldValid = false
        }

        if (getFile == null) {
            isAllFieldValid = false
        }

        if (isAllFieldValid) {
            val file = reduceFileImage(getFile as File)
            val description =
                binding.edtStory.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            var lat: RequestBody? = null
            var lon: RequestBody? = null

            if (location != null) {
                lat = location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                lon = location?.longitude.toString().toRequestBody("text/plain".toMediaType())
            }

            this@AddStoryActivity.lifecycleScope.launchWhenStarted {
                launch {
                    addStoryViewModel.addStory(
                        token = token,
                        file = imageMultipart,
                        description = description,
                        lat = lat,
                        lon = lon
                    ).collect { response ->
                        response.onSuccess {
                            showLoading(isLoading = false)
                            Snackbar.make(
                                binding.root,
                                getString(R.string.success_upload_story),
                                Snackbar.LENGTH_SHORT
                            ).show()

                            //backtoprevious
                            finish()
                        }

                        response.onFailure {
                            showLoading(isLoading = false)
                            Snackbar.make(
                                binding.root,
                                getString(R.string.error_upload_story),
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

    private fun startGalleryIntent() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.dialog_choose_image))
        launcherGalleryIntent.launch(chooser)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            btnGallery.isEnabled = !isLoading
            btnCamera.isEnabled = !isLoading
            edtStory.isEnabled = !isLoading

            layoutLoading.root.visibilityAnimate(isLoading)
        }
    }


}