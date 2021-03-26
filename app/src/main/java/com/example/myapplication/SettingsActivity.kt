package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.SettingsActivityBinding

/**Pantalla que gestiona el cambio de enlace del que se obtienen los datos de los parking*/
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= SettingsActivityBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)
        val enlaceActual: TextView =binding.link
        var sharedPref =this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        val enlaceTexto: String?=sharedPref.getString("Enlace", getString(R.string.no_link))
        enlaceActual.text = enlaceTexto

        //Para activar el boton de atras
        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val save: ImageButton = binding.save
        save.setOnClickListener {
            sharedPref = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
            val saveText: EditText= binding.newLinkEdit
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



    }



}