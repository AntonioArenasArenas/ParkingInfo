package com.example.myapplication

import adapter.ParkingListAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import model.Parking
import network.UpdateParking



class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var network: UpdateParking


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        //Obtenemos las preferencias donde esta almacenado el enlace del que obtener la lista de parkings

        val sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        var url: String? = sharedPref.getString("Enlace", getString(R.string.no_link))

        val lista =  ArrayList<Parking>()
        // Se añade un item falso de consultar datos como espera
        lista.add(Parking("0", getString(R.string.loading), "-", "-", "-", "-", "-", "-",false))
        viewManager = LinearLayoutManager(this)
        viewAdapter = ParkingListAdapter(lista, applicationContext)
        network= UpdateParking()
        network.actualizar(url,lista,this,viewAdapter)



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

}
