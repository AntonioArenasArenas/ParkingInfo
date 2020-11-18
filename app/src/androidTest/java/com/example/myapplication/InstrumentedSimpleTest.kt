package com.example.myapplication


import adapter.ParkingListAdapter
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import model.Parking
import network.UpdateParking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedSimpleTest {

    @Test
    fun testdisplayFavorites() {
        // Context que estamos testeando
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adapter = ParkingListAdapter(ArrayList(), context)
        val listActual = ArrayList<Parking>()
        listActual.add(Parking("0", "", "", "", "", "", "", "", false))
        listActual.add(Parking("1", "", "", "", "", "", "", "", false))
        listActual.add(Parking("2", "", "", "", "", "", "", "", false))
        listActual.add(Parking("3", "", "", "", "", "", "", "", false))
        val codigos= ArrayList<String>()
        codigos.add("0")
        codigos.add("1")
        adapter.displayFavorites(codigos, listActual)
        assertThat(listActual.size).isEqualTo(2)

    }

    @Test
    fun testactualizar(){
        val update= UpdateParking()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adapter = ParkingListAdapter(ArrayList(), context)
        val listaParking = ArrayList<Parking>()
        update.actualizar("http://trafico.sevilla.org/aparcamientos.xml",listaParking,context,adapter)
        //Damos tiempo a que la llamada se complete
        if(listaParking.isEmpty()){
            Thread.sleep(1000)
        }
        assertThat(listaParking.size).isEqualTo(49)
    }







}