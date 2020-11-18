package com.example.myapplication

import adapter.ParkingListAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import location.LocationServiceUpdate
import model.Parking
import network.UpdateParking
import permissions.PermissionsClass


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ParkingListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var network: UpdateParking
    private lateinit var location: LocationServiceUpdate
    private lateinit var sharedPref: SharedPreferences

    //Variable estática que controla si se debe pedir más veces el permiso de los servicios de localización de Google
    companion object {
        var globalVar = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        //Comprobamos que tenemos el permiso de localizacion para los filtros de distancia de los parking
        val permissions = PermissionsClass()
        permissions.askPermission(this, this, supportFragmentManager)


        val lista = ArrayList<Parking>()
        // Se añade un item falso de consultar datos como espera
        lista.add(
            Parking(
                "0",
                getString(R.string.loading),
                "-",
                "-",
                "-",
                "-",
                "-",
                "-",
                false
            )
        )
        viewAdapter = ParkingListAdapter(lista, applicationContext)


        //Actualizamos la posición
        val sharedPref2=this.getSharedPreferences("Posicion", Context.MODE_PRIVATE)
        val chosenLocation=sharedPref2.getString("Posicion","error")
        //Si es error, tenemos que coger localización actual
        if(chosenLocation.equals("error")){
            location = LocationServiceUpdate(this, viewAdapter, this)
            location.updateLocation()
        }else{
            //Si hay, siempre habrá dos valores
            val codes= chosenLocation?.split(",")
            val codesLatitude = codes?.get(0)?.replace(Regex("[^\\d.-]+"),"")
            val codesLongitude = codes?.get(1)?.replace(Regex("[^\\d.-]+"),"")
            val chooseLocation= Location(LocationManager.GPS_PROVIDER)
            chooseLocation.latitude= codesLatitude?.toDouble()!!
            chooseLocation.longitude=codesLongitude?.toDouble()!!
            viewAdapter.setUpdateLocation(chooseLocation)
        }


        //Obtenemos las preferencias donde esta almacenado el enlace del que obtener la lista de parkings
        sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        val url: String? = sharedPref.getString("Enlace", getString(R.string.no_link))

        viewManager = LinearLayoutManager(this)
        network = UpdateParking()
        network.actualizar(url, lista, this, viewAdapter)


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
        val tabLayout = findViewById<View>(R.id.tabLayout) as TabLayout
        val refresh: ImageButton = findViewById(R.id.refresh)
        //Se coge de nuevo aquí el enlace en caso de que la navegación sea hacia atrás y no hacia arriba
        refresh.setOnClickListener {
            if (tabLayout.selectedTabPosition == 1) {
                showFavs(lista, 1)
            } else {
                showList(lista)
            }
            val toast1 = Toast.makeText(
                applicationContext,
                getString(R.string.refresh_message), Toast.LENGTH_LONG
            )

            toast1.show()

        }

        val navDrawer: DrawerLayout = findViewById(R.id.drawer_layout)


        //Botón para filtrar
        val filter: Button = findViewById(R.id.submit_button)
        filter.setOnClickListener {
            filtrar()
            navDrawer.closeDrawer(GravityCompat.START)
        }

        val openFilter: ImageButton = findViewById(R.id.filter)
        openFilter.setOnClickListener {
            if (navDrawer.isDrawerOpen(GravityCompat.START)) {
                navDrawer.closeDrawer(GravityCompat.START)
            } else {
                navDrawer.openDrawer(GravityCompat.START)
            }
        }

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            //Cambiar a la pestaña correspondiente
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                //Pestaña favoritos
                if (position == 1) {
                    viewAdapter.setCurrentTab(1)
                    showFavs(lista, 0)
                } else {
                    viewAdapter.setCurrentTab(0)
                    showList(lista)
                }
                //Reiniciamos los filtros entre pestañas
                viewAdapter.setfilterList(lista)
                restartFilters()
            }

            //No hace falta dar comportamiento especial a los Tab
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        val distance: EditText = findViewById(R.id.distance_filter_value)
        //Se usa un when por si la aplicación escalara y hubiera más permisos que controlar
        when (requestCode) {
            0 -> {
                // Si se cancela la petición el resultado estará vacío
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    //Permiso concedido, filtro de distancia activado

                    distance.isEnabled = true
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
            1 -> {
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
    private fun filtrar() {
        val precio: EditText = findViewById(R.id.price_filter_value)
        val precio2: EditText = findViewById(R.id.price_filter_value2)
        var precioFilter = precio.text.toString()
        val precio2Filter = precio2.text.toString()
        if (precioFilter.isEmpty()) {
            precioFilter = if (precio2Filter.isEmpty()) {
                "-"
            }else{
                precio2Filter+"d"
            }
        }
        val distancia: EditText = findViewById(R.id.distance_filter_value)
        var distanciaFilter = distancia.text.toString()
        if (distanciaFilter.isEmpty()) {
            distanciaFilter = "-"
        }
        val checkOpen: CheckBox = findViewById(R.id.checkBox_open)
        val checkFree: CheckBox = findViewById(R.id.checkBox_free)
        viewAdapter.filter.filter(precioFilter + "," + distanciaFilter + "," + checkOpen.isChecked + "," + checkFree.isChecked)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val distance: EditText = findViewById(R.id.distance_filter_value)
        if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            distance.isEnabled = true
            distance.setBackgroundResource(R.drawable.bottom_border)
            location.updateLocation()
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == 2) {
            distance.isEnabled = false
            distance.setBackgroundResource(R.drawable.bottom_border_red)
            globalVar = false

        }

    }

    /**Método que sirve para actualizar la pantalla cuando accedemos o actualizamos a la pestaña de favoritos
     *
     * @param list lista de todos los parking mostrados actualmente. Devuelve los favoritos al finalizar
     * @param mode 0 si es para mostrar los favoritos, 1 si es para actualizar la lista tras darle al botón*/
    fun showFavs(list: ArrayList<Parking>, mode: Int) {
        sharedPref = this.getSharedPreferences("Favoritos", Context.MODE_PRIVATE)
        val favs = sharedPref.getString("Favoritos", "")
        val favsSplitted = favs!!.split(",")
        val arraySplitted: ArrayList<String>
        arraySplitted = if (favs.isEmpty()) {
            ArrayList()
        } else {
            ArrayList(favsSplitted)
        }
        viewAdapter.displayFavorites(arraySplitted, list)
        //Mostrar
        if (mode == 0) {
            viewAdapter.notifyDataSetChanged()
            //Actualizar
        } else {
            sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
            val url = sharedPref.getString("Enlace", getString(R.string.no_link))
            network.getFavoriteListUpdated(this, url, viewAdapter)

        }
    }

    /**Método que sirve para mostrar la lista con todos los parking
     *
     * @param list lista donde se devuelven los parking*/
    fun showList(list: ArrayList<Parking>) {
        list.clear()
        list.add(
            Parking(
                "0",
                getString(R.string.loading),
                "-",
                "-",
                "-",
                "-",
                "-",
                "-",
                false
            )
        )
        viewAdapter.notifyDataSetChanged()
        sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        val url = sharedPref.getString("Enlace", getString(R.string.no_link))
        network.actualizar(url, list, this, viewAdapter)

    }

    /**Método que reiniciar los filtros.Se usa al cambiar de pestañas y resetear las listas*/
    fun restartFilters() {
        val precio: EditText = findViewById(R.id.price_filter_value)
        precio.setText("")
        val distancia: EditText = findViewById(R.id.distance_filter_value)
        distancia.setText("")
        val checkOpen: CheckBox = findViewById(R.id.checkBox_open)
        checkOpen.isChecked = false
        val checkFree: CheckBox = findViewById(R.id.checkBox_free)
        checkFree.isChecked = false
    }


}

