package com.android.test

import android.Manifest.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    val PERMISSIONS_REQUEST = 1000

    val zoom = 10.0f
    var locationPermissionOk: Boolean = false
    var marker: Marker? = null

    lateinit var map: GoogleMap
    lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val spinner: Spinner = findViewById(R.id.spinner_services)
        ArrayAdapter.createFromResource(this, R.array.items, android.R.layout.simple_spinner_item).also {
                adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
        }

        findViewById<LinearLayout>(R.id.button_next).setOnClickListener{
            if(marker != null){
                intent = Intent(this, SecondActivity::class.java).apply {
                    putExtra("item", spinner.selectedItem.toString())
                    putExtra("latitude", marker!!.position.latitude)
                    putExtra("longitude", marker!!.position.longitude)
                }
                startActivity(intent)
                finish()
            } else {
                AlertDialog.Builder(this).setMessage(R.string.label_error_marker).setPositiveButton(android.R.string.ok, null).create().show()
            }
        }

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)
        client = LocationServices.getFusedLocationProviderClient(this)

        if(ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationPermissionOk = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(){
        val lr = LocationRequest.create()
        lr.setNumUpdates(1)
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        client.requestLocationUpdates(lr, locationCallback, null)
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(res: LocationResult){
            for(l in res.locations){
                if(l != null){
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(l.latitude, l.longitude), zoom))
                    marker?.remove()
                    marker = map.addMarker(MarkerOptions().position(LatLng(l.latitude, l.longitude)))
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        this.map.setOnMapClickListener {
            marker?.remove()
            marker = map.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        locationPermissionOk = false
        when(requestCode){
            PERMISSIONS_REQUEST -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    locationPermissionOk = true
                    getDeviceLocation()
                }
            }
        }
    }

}