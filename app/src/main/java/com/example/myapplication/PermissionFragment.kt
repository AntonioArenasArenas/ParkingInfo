package com.example.myapplication

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment

/**DialogFragment que gestiona la información de los permisos de ubicación necesarios para el filtro de distancia*/
class PermissionFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Se usa el builder para la construccion del Dialog
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.permission_info)
                .setPositiveButton(R.string.grant_permission
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                }

                .setNegativeButton(R.string.cancel
                ) { _, _ ->
                    dismiss()
                    MainActivity.globalVar=false
                }
            // Se crea el Dialog y se devuelve
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}