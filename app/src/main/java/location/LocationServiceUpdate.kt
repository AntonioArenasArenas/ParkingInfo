package location

import adapter.ParkingListAdapter
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.Exception

/**
 * Clase que gestiona las peticiones periódicas de ubicación
 *
 * @param context Context del Activity donde se obtiene la ubicación
 * @param viewAdapter ParkingListAdapter donde actualizar la ubicación una vez obtenida
 * @param activity Activity donde se obtiene la ubicación. Se usa para mandar un Intent de obtener servicios si falta algo para la ubicación*/
class LocationServiceUpdate (private val context: Context, val viewAdapter: ParkingListAdapter, private val activity: Activity) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    /**Método que actualiza la localización del dispositivo.
     *
     * En el método se comprueba si hay permisos para la actualización y se coge la última que se haya guardado. Si no la hay, se coge una nueva. */
    fun updateLocation(){

        //Generamos un LocationService para gestionar la ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        val locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var currentLocation : Location? = null

        //Obtenemos la última posición conocida
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                currentLocation=location

            }

        //Se ha de comprobar que se tiene el permiso de localización

        val permissionGrante = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissionGrante == PackageManager.PERMISSION_GRANTED) {

            //Si no tenemos localización hacemos un update de la misma
            if (currentLocation == null) {
                createLocationRequest(locManager)

            }

        }
    }

    /**Método que configura la petición de localización
     *
     * Tras configurar los parámetros, se comprueba que dichos parámetros coincidan con los permisos del teléfono. Si coinciden, se consigue una nueva ubicación,
     * en caso contrario, se piden lo que falte para ello.
     *
     * @param locManager LocationManager para comprobar que la ubicación está conectada*/
    private fun createLocationRequest(locManager: LocationManager) {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { e: Exception ->


            if (e is ResolvableApiException && MainActivity.globalVar) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(activity, 2)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }

            }

        }

            .addOnSuccessListener {
                val permissionGrante= ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (LocationManagerCompat.isLocationEnabled(locManager) && permissionGrante==PackageManager.PERMISSION_GRANTED) {
                    val locationCallback = object : LocationCallback() {

                        override fun onLocationResult(locationResult: LocationResult?) {
                            locationResult ?: return
                            viewAdapter.updateLocation(locationResult.lastLocation)

                        }


                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                }else{
                    val toast3 = Toast.makeText(
                        context.applicationContext,
                        context.getString(R.string.turn_on_location), Toast.LENGTH_LONG
                    )

                    toast3.show()
                }
            }


    }

}