package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng

/**Pantalla que gestiona el cambio de enlace del que se obtienen los datos de los parking*/
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val enlaceActual: TextView =findViewById(R.id.link)
        var sharedPref =this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        val enlaceTexto: String?=sharedPref.getString("Enlace", getString(R.string.no_link))
        enlaceActual.text = enlaceTexto

        //Vemos si se está cogiendo la posición actual o una anteriormente guardada para actualizar el layout
        val sharedPref2 =this.getSharedPreferences("Posicion", Context.MODE_PRIVATE)
        val posicionMarcada: String?=sharedPref2.getString("Posicion", "error")
        val posicionMapa: TextView =findViewById(R.id.position_selected)
        val mapButton: ImageButton = findViewById(R.id.select_position)

        //Caso en el que no hay nada guardado anterior
        if(posicionMarcada.equals("error")){
            val opcionActual: RadioButton =findViewById(R.id.current_position_option)
            opcionActual.isChecked=true
            //Ocultamos el textview y el boton de la opcion de escoger posicion
            posicionMapa.visibility=View.GONE
            mapButton.visibility=View.GONE
            //Caso en el que hay algo anteriormente guardado
        }else{
            val opcionActual: RadioButton =findViewById(R.id.select_position_option)
            opcionActual.isChecked=true
            posicionMapa.visibility=View.VISIBLE
            mapButton.visibility=View.VISIBLE
            posicionMapa.text= posicionMarcada
        }



        //Para activar el boton de atras
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val save: ImageButton = findViewById(R.id.save)
        save.setOnClickListener {
            sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
            val saveText: EditText= findViewById(R.id.new_link_edit)
            with(sharedPref.edit()) {
                putString("Enlace", saveText.text.toString())
                commit()
            }
            sharedPref=this.getSharedPreferences("Favoritos", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                //Al cambiar de enlace reseteamos los favoritos
                remove("Favoritos")
                commit()
            }
            enlaceActual.text = saveText.text.toString()
        }

        mapButton.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent, 1)
        }


    }

    fun onRadioButtonClicked(view: View) {

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            val posicionMapa: TextView =findViewById(R.id.position_selected)
            val mapButton: ImageButton = findViewById(R.id.select_position)
            // Check which radio button was clicked
            when (view.getId()) {
                R.id.current_position_option ->
                    if (checked) {
                        posicionMapa.visibility=View.GONE
                        mapButton.visibility=View.GONE
                        posicionMapa.text=""
                        val sharedPref2 =this.getSharedPreferences("Posicion", Context.MODE_PRIVATE)
                        with(sharedPref2.edit()) {
                            //Al cambiar de enlace reseteamos las coordenadas
                            remove("Posicion")
                            commit()
                        }
                    }
                R.id.select_position_option ->
                    if (checked) {
                        posicionMapa.visibility=View.VISIBLE
                        mapButton.visibility=View.VISIBLE
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode==1){
            val posicionMapa: TextView =findViewById(R.id.position_selected)
            if(data!=null){
                val coord= data.getParcelableExtra<LatLng>("coord")
                if (coord != null) {
                    if(coord.latitude!=0.0){
                        posicionMapa.text=coord.toString()
                        val sharedPref2 =this.getSharedPreferences("Posicion", Context.MODE_PRIVATE)
                        with(sharedPref2.edit()) {
                            //Guardamos las coordenadas
                            putString("Posicion", coord.toString())
                            commit()
                        }

                    }
                }
            }
        }
    }


}