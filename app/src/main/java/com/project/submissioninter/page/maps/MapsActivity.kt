package com.project.submissioninter.page.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.project.submissioninter.R
import com.project.submissioninter.databinding.ActivityMapsBinding
import com.project.submissioninter.databinding.ItemMapBinding
import com.project.submissioninter.datasource.localdata.entity.StoryEntity
import com.project.submissioninter.datasource.remotedata.response.StoryResponse
import com.project.submissioninter.page.addstory.MediaHelper.convertUrlToBitmap
import com.project.submissioninter.page.addstory.MediaHelper.getNameLocation
import com.project.submissioninter.page.detail.DetailStoryActivity
import com.project.submissioninter.page.story.StoryListActivity
import com.project.submissioninter.page.story.setLocaleDateFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@ExperimentalPagingApi
@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var token: String = ""
    private val mapsViewModel: MapsViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MapsActivity)

        this@MapsActivity.lifecycleScope.launchWhenCreated {
            mapsViewModel.getAuthenticationToken().collect { token ->
                if (!token.isNullOrEmpty()) {
                    this@MapsActivity.token = token
                    setStoryMarker()
                }
            }
        }
    }

    override fun getInfoWindow(marker: Marker): View {
        val bindingItemMapWindow = ItemMapBinding.inflate(LayoutInflater.from(this@MapsActivity))
        val data: StoryResponse = marker.tag as StoryResponse

        bindingItemMapWindow.tvLocation.text = getNameLocation(
            this@MapsActivity,
            marker.position.latitude,
            marker.position.longitude
        )
        bindingItemMapWindow.tvName.text = getString(
            R.string.ttl_detail_toolbar,
            data.name.lowercase().replaceFirstChar { it.titlecase() })
        bindingItemMapWindow.tvDescription.text = data.description
        bindingItemMapWindow.tvDate.setLocaleDateFormat(timestamp = data.createdAt)
        bindingItemMapWindow.ivStory.setImageBitmap(
            convertUrlToBitmap(
                this@MapsActivity,
                data.photoUrl
            )
        )
        return bindingItemMapWindow.root
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        getCurrentLocation()
        setupMapStyle(R.raw.style_map_aubgene)
        mMap.setInfoWindowAdapter(this)

        mMap.setOnInfoWindowClickListener { marker ->
            val data: StoryResponse = marker.tag as StoryResponse

            val detailStoryIntent = Intent(this@MapsActivity, DetailStoryActivity::class.java)
            detailStoryIntent.putExtra(
                StoryListActivity.STORY_KEY,
                StoryEntity(
                    id = data.id,
                    name = data.name,
                    description = data.description,
                    createdAt = data.createdAt,
                    photoUrl = data.photoUrl,
                    lon = data.lon,
                    lat = data.lat
                )
            )
            startActivity(
                detailStoryIntent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this@MapsActivity).toBundle()
            )

        }
    }

    private fun setupMapStyle(styleResource: Int) {
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, styleResource))
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
    }

    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    private fun setStoryMarker() {
        this@MapsActivity.lifecycleScope.launchWhenResumed {
            launch {
                mapsViewModel.getStoriesWithLocation(token).collect { result ->
                    result.onSuccess { response ->
                        response.stories.forEach { story ->

                            if (story.lat != null && story.lon != null) {
                                val latLng = LatLng(story.lat, story.lon)

                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                )?.tag = story
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this@MapsActivity.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f))
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.activate_location),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }




}