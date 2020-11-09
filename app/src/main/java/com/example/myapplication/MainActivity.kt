package com.example.myapplication

import adapter.ParkingListAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import location.LocationServiceUpdate
import model.Parking
import network.UpdateParking
import permissions.PermissionsClass


class MainActivity : AppCompatActivity()  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ParkingListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var network: UpdateParking


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))


        //Comprobamos que tenemos el permiso de localizacion para los filtros de distancia de los parking
        val permissions= PermissionsClass()
        permissions.askPermission(this,this,supportFragmentManager)


        val lista =  ArrayList<Parking>()
        // Se añade un item falso de consultar datos como espera
        lista.add(Parking("0", getString(R.string.loading), "-", "-", "-", "-", "-", "-",false))
        viewAdapter = ParkingListAdapter(lista, applicationContext)


        //Actualizamos la posición
        val location = LocationServiceUpdate(this,viewAdapter,this)
        location.updateLocation()


        //Obtenemos las preferencias donde esta almacenado el enlace del que obtener la lista de parkings
        val sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        var url: String? = sharedPref.getString("Enlace", getString(R.string.no_link))

        viewManager = LinearLayoutManager(this)
        network= UpdateParking()
        network.actualizar(url,lista,this,viewAdapter)


        //Se gestiona la lista con el adaptador
        recyclerView = findViewById<RecyclerView>(R.id.listaParking).apply {
            // si el tamaño va a ser fijo y solo depende del numero de elementos poner a true para mejorar rendimiento
            setHasFixedSize(true)

            // aqui se especifica el layout manager
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            // aqui se especifica el adaptador
            adapter = viewAdapter

        }

        val refresh: ImageButton = findViewById(R.id.refresh)
        //Se coge de nuevo aquí el enlace en caso de que la navegación sea hacia atrás y no hacia arriba
        refresh.setOnClickListener {
            url = sharedPref.getString("Enlace", getString(R.string.no_link))
            network.actualizar(url,lista,this,viewAdapter)
            val toast1 = Toast.makeText(
                applicationContext,
                getString(R.string.refresh_message), Toast.LENGTH_LONG
            )

            toast1.show()

        }

        //Botón para filtrar
        val filter: Button= findViewById(R.id.submit_button)
        filter.setOnClickListener{
            filtrar()
        }



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val distance: EditText =findViewById(R.id.distance_filter_value)
        //Se usa un when por si la aplicación escalara y hubiera más permisos que controlar
        when (requestCode) {
            0 -> {
                // Si se cancela la petición el resultado estará vacío
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Permiso concedido, filtro de distancia activado

                    distance.isEnabled=true
                    distance.setBackgroundResource(R.drawable.bottom_border)

                } else {
                        //Se explica que se han hecho modificaciones en la aplicación por no tener el permiso y se desactiva el filtro
                        Snackbar.make(
                            findViewById(R.id.drawer_layout), R.string.disable_filter,
                            Snackbar.LENGTH_LONG
                        ).show()
                        distance.isEnabled = false
                        distance.setBackgroundResource(R.drawable.bottom_border_red)

                }
                return
            }
            //Este codigo se usa para saber que venimos del Dialog
            1 ->{
                distance.isEnabled = false
                distance.setBackgroundResource(R.drawable.bottom_border_red)
                return
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        filtrar()
    }

    /** Método que coge los datos de los filtros y los pasa al filter del adaptador */
    private fun filtrar(){
        val precio: EditText= findViewById(R.id.price_filter_value)
        var precioFilter=precio.text.toString()
        if(precioFilter.isEmpty()){
            precioFilter="-"
        }
        val distancia: EditText= findViewById(R.id.distance_filter_value)
        var distanciaFilter=distancia.text.toString()
        if(distanciaFilter.isEmpty()){
            distanciaFilter="-"
        }
        val checkOpen: CheckBox = findViewById(R.id.checkBox_open)
        val checkFree: CheckBox = findViewById(R.id.checkBox_free)
        viewAdapter.filter.filter(precioFilter + ","+ distanciaFilter+","+checkOpen.isChecked+","+checkFree.isChecked)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode==2){
            val location = LocationServiceUpdate(this,viewAdapter,this)
            location.updateLocation()
        }
    }

}
