package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val enlaceActual: TextView =findViewById(R.id.link)
        val sharedPref =this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
        val enlaceTexto: String?=sharedPref.getString("Enlace", getString(R.string.no_link))
        enlaceActual.text = enlaceTexto

        //Para activar el boton de atras
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val save: ImageButton = findViewById(R.id.save)
        save.setOnClickListener {
            val sharedPrefButton = this.getSharedPreferences("Enlace", Context.MODE_PRIVATE)
            val saveText: EditText= findViewById(R.id.new_link_edit)
            with(sharedPrefButton.edit()) {
                putString("Enlace", saveText.text.toString())
                commit()
            }
            enlaceActual.text = saveText.text.toString()
        }


    }


}