package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

internal class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var coordfinal: LatLng
    private lateinit var binding: ActivityMapsBinding
    private var lastMarker: Marker? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapsBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        coordfinal= LatLng(0.0, 0.0)
        val mySnackbar=Snackbar.make(
            binding.map, R.string.mark_position,
            Snackbar.LENGTH_INDEFINITE
        )
        val sbView: View = mySnackbar.view
        sbView.setOnClickListener {

            val intent = Intent()
            intent.putExtra("coord",coordfinal)
            setResult(Activity.RESULT_OK,intent)
            finish()

        }
        mySnackbar.show()
        lastMarker=null


        mMap.setOnMapClickListener { coord ->

            if(lastMarker!=null){
                lastMarker?.remove()
            }
            lastMarker= mMap.addMarker(
                MarkerOptions()
                .position(coord))
            coordfinal=coord
        }
    }


}