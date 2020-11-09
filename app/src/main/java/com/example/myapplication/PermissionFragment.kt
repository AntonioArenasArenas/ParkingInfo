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
                    requireActivity().onRequestPermissionsResult(1, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        intArrayOf())
                }
            // Se crea el Dialog y se devuelve
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //Controlamos si el usuario cancela el Dialog para notificar que ha cancelado
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().onRequestPermissionsResult(1, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            intArrayOf())
    }
}