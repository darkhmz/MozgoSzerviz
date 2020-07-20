package com.android.test

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    val DEBUG = "debug"
    val PERMISSIONS_REQUEST = 1000

    val zoom = 10.0f
    var marker: Marker? = null

    lateinit var map: GoogleMap
    lateinit var client: FusedLocationProviderClient
    lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        spinner = findViewById(R.id.spinner_services)
        spinner.isEnabled = false

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
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST)
        }

        //Csak hogy legyen benne ilyen is
        //Megpróbáljuk lekérni a szolgáltatások listáját szerverről
        //Ha nem sikerül, akkor az alkalmazásban tárolt listával lesz feltöltve az adapter
        val stringRequest = StringRequest(
        Request.Method.GET, "https://tabletinvoice-production.appspot.com/getitems", Response.Listener<String> { response ->
            try {
                setAdapter(JSONArray(response))
            } catch (e: JSONException){
                setAdapter(null)
            }
        },
        Response.ErrorListener {
            setAdapter(null)
        })

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun setAdapter(items: JSONArray?){
        val array = if(items != null) Array(items.length()){ items.get(it).toString() } else resources.getStringArray(R.array.items)

        ArrayAdapter(this, android.R.layout.simple_spinner_item, array).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.isEnabled = true
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
        when(requestCode){
            PERMISSIONS_REQUEST -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getDeviceLocation()
                }
            }
        }
    }

}