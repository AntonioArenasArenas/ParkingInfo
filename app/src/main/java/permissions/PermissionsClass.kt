package permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.myapplication.PermissionFragment

class PermissionsClass {

    fun askPermission(context: Context, activity: Activity, fragmentManager: FragmentManager){
        //Comprobamos que tenemos el permiso de localizacion para los filtros de distancia de los parking

        if(ContextCompat.checkSelfPermission(context , Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
                val dialog = PermissionFragment()
                dialog.show(fragmentManager,"Permisos")
            }else{
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
            }
        }

    }
}