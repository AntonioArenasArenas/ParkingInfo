package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import com.example.myapplication.databinding.ActivityTravelDestinationBinding
import com.google.android.gms.maps.model.LatLng

class TravelDestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTravelDestinationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTravelDestinationBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)

        val sharedPref2 =this.getSharedPreferences("Posicion", Context.MODE_PRIVATE)
        val posicionMarcada: String?=sharedPref2.getString("Posicion", "error")
        val posicionMapa: TextView =binding.positionSelected
        val mapButton: ImageButton = binding.selectPosition

        //Caso en el que no hay nada guardado anterior
        if(posicionMarcada.equals("error")){
            val opcionActual: RadioButton =binding.currentPositionOption
            opcionActual.isChecked=true
            //Ocultamos el textview y el boton de la opcion de escoger posicion
            posicionMapa.visibility=View.GONE
            mapButton.visibility=View.GONE
            //Caso en el que hay algo anteriormente guardado
        }else{
            val opcionActual: RadioButton =binding.selectPositionOption
            opcionActual.isChecked=true
            posicionMapa.visibility=View.VISIBLE
            mapButton.visibility=View.VISIBLE
            posicionMapa.text= posicionMarcada
        }
        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapButton.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent, 1)
        }

    }

    fun onRadioButtonClicked(view: View) {

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            val posicionMapa: TextView =binding.positionSelected
            val mapButton: ImageButton = binding.selectPosition
            // Check which radio button was clicked
            when (view.getId()) {
                R.id.current_position_option ->
                    if (checked) {
                        posicionMapa.visibility= View.GONE
                        mapButton.visibility= View.GONE
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
                        posicionMapa.visibility= View.VISIBLE
                        mapButton.visibility= View.VISIBLE
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode==1){
            val posicionMapa: TextView =binding.positionSelected
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