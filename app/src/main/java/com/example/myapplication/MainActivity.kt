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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
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
    private lateinit var binding: ActivityMainBinding


    //Variable estática que controla si se debe pedir más veces el permiso de los servicios de localización de Google
    companion object {
        var globalVar = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Usamos ViewBinding para relacionar las vistas, es mas seguro que DataBinding y findViewById y DataBinding solo es mejor si se usan expresiones
        //o vinculacion de datos bidireccionales
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)
        setSupportActionBar(binding.myToolbar)

        //Comprobamos que no se haya dicho ya que no a los permisos
        if(globalVar) {
            //Comprobamos que tenemos el permiso de localizacion para los filtros de distancia de los parking
            val permissions = PermissionsClass()
            permissions.askPermission(this, this, supportFragmentManager)
        }

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
        actualizarUbicacion(chosenLocation)

        //Obtenemos las preferencias donde esta almacenado el enlace del que obtener la lista de parkings
        sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        val url: String? = sharedPref.getString("Enlace", getString(R.string.no_link))


        network = UpdateParking()
        network.actualizar(url, lista, this, viewAdapter)

        viewManager = LinearLayoutManager(this)

        //Se gestiona la lista con el adaptador
        recyclerView = binding.listaParking.apply {
            // si el tamaño va a ser fijo y solo depende del numero de elementos poner a true para mejorar rendimiento
            setHasFixedSize(true)

            // aqui se especifica el layout manager
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            // aqui se especifica el adaptador
            adapter = viewAdapter

        }
        val tabLayout = binding.tabLayout
        val refresh: ImageButton = binding.refresh
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

        val navDrawer: DrawerLayout = binding.root


        //Botón para filtrar
        val filter: Button = binding.submitButton
        filter.setOnClickListener {
            filtrar()
            navDrawer.closeDrawer(GravityCompat.START)
        }

        val openFilter: ImageButton = binding.filter
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

        val spinner: Spinner = binding.priceType
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.price_type,
            R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.location_options -> {
                val intent = Intent(this, TravelDestinationActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        //Se usa un when por si la aplicación escalara y hubiera más permisos que controlar
        when (requestCode) {
            0 -> {
                // Si se cancela la petición el resultado estará vacío
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    //Permiso concedido, actualizamos ubicación
                    val sharedPref2=this.getSharedPreferences("Posicion", Context.MODE_PRIVATE)
                    val chosenLocation=sharedPref2.getString("Posicion","error")
                    //Si es error, tenemos que coger localización actual
                    actualizarUbicacion(chosenLocation)
                }
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
        val precio: EditText = binding.priceFilterValue
        val spinner : Spinner= binding.priceType
        val tipo: Int= spinner.selectedItemPosition
        var precioFilter = precio.text.toString()
        if (precioFilter.isEmpty()) {
            precioFilter = "-"

        }else{
            if(tipo==1){
                precioFilter += "d"
            }

        }
        val distancia: EditText = binding.distanceFilterValue
        var distanciaFilter = distancia.text.toString()
        if (distanciaFilter.isEmpty()) {
            distanciaFilter = "-"
        }
        val checkOpen: CheckBox = binding.checkBoxOpen
        val checkFree: CheckBox = binding.checkBoxFree
        viewAdapter.filter.filter(precioFilter + "," + distanciaFilter + "," + checkOpen.isChecked + "," + checkFree.isChecked)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val distance: EditText = binding.distanceFilterValue
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
    private fun showFavs(list: ArrayList<Parking>, mode: Int) {
        sharedPref = this.getSharedPreferences("Favoritos", Context.MODE_PRIVATE)
        val favs = sharedPref.getString("Favoritos", "")
        val favsSplitted = favs!!.split(",")
        val arraySplitted: ArrayList<String> = if (favs.isEmpty()) {
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
        val precio: EditText = binding.priceFilterValue
        precio.setText("")
        val distancia: EditText = binding.distanceFilterValue
        distancia.setText("")
        val checkOpen: CheckBox = binding.checkBoxOpen
        checkOpen.isChecked = false
        val checkFree: CheckBox = binding.checkBoxFree
        checkFree.isChecked = false
    }

    /**Método utilizado para actualizar la ubicación
     *
     * @param chosenLocation ubicación introducida a mano pasada a formato String. Si no existe, se le pasa la cadena "error"*/
    private fun actualizarUbicacion(chosenLocation: String?){
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
    }


}

