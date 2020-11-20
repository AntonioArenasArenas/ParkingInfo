package permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.myapplication.MainActivity
import com.example.myapplication.PermissionFragment

/**Esta clase se usa para pedir permisos acerca de la ubicación. Si el usuario rechaza dar permiso, se genera un Dialog explicativo que usa PermissionFragment*/
class PermissionsClass {

    /**Función que se encarga de pedir permisos. Si no los da pero no le da a no volver a mostrar, se le enseña un Dialog explicativo
     *
     * @param context Context del Activity donde se hace la comprobación de los permisos
     * @param activity Activity donde se hace la comprobación de los permisos
     * @param fragmentManager FragmentManager que gestiona el Dialog en caso de necesitarlo*/
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