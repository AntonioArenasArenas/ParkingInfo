package network

import adapter.ParkingListAdapter
import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.R
import model.Parking
import parser.ParkingXmlParser

/**Clase que gestiona la comunicación web para obtener los parking*/
class UpdateParking {

    /**Método que actualiza el estado actual de los parking. En él se conecta con el enlace adecuado y se parsea el XML correspondiente
     *
     * @param url URL a donde se hace la petición GET
     * @param lista ArrayList dónde se devuelven los Parking obtenidos
     * @param context Context de la actividad dónde se muestran los parking
     * @param viewAdapter adaptador que se encarga al final de la comunicación de hacer un notifyDataSetChanged()*/
    fun actualizar(url: String?, lista: ArrayList<Parking>, context: Context, viewAdapter: ParkingListAdapter) {
        val queue = Volley.newRequestQueue(context)


        // Se hace la petición con la URL correspondiente
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val responseStream = response.byteInputStream(Charsets.ISO_8859_1)
                lista.clear()
                lista.addAll(ParkingXmlParser(context).parse(responseStream))
                viewAdapter.updateFilterList(lista)
                viewAdapter.notifyDataSetChanged()

            },
            {
                lista.clear()
                lista.add(
                    Parking(
                        "0",
                        context.getString(R.string.update_error),
                        "-",
                        "-",
                        "-",
                        "-",
                        "-",
                        "",false
                    )
                )
                viewAdapter.notifyDataSetChanged()
            })

        // Se añade la peticion a la cola
        queue.add(stringRequest)
    }

    /**Método que sirve para actualizar la lista de favoritos cuando se le da al botón de actualizar estando en dicha pestaña
     *
     * @param context Context de la actividad donde se muestran los favoritos
     * @param url URL a donde se hace la petición GET
     * @param viewAdapter adaptador que se encarga al final de la comunicación de hacer un notifyDataSetChanged()*/
    fun getFavoriteListUpdated(context: Context, url: String?, viewAdapter: ParkingListAdapter){
        val queue = Volley.newRequestQueue(context)
        val lista= ArrayList<Parking>()

        // Se hace la petición con la URL correspondiente
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val responseStream = response.byteInputStream(Charsets.ISO_8859_1)
                lista.clear()
                lista.addAll(ParkingXmlParser(context).parse(responseStream))
                val codeList=viewAdapter.getFavorites()
                val favorites: ArrayList<Parking> = ArrayList()
                for(parking in lista){
                    if(codeList.contains(parking.codigo)){
                        favorites.add(parking)
                    }
                }
                viewAdapter.updateFavoriteList(favorites)
                viewAdapter.notifyDataSetChanged()

            },
            {
                lista.clear()
                lista.add(
                    Parking(
                        "0",
                        context.getString(R.string.update_error),
                        "-",
                        "-",
                        "-",
                        "-",
                        "-",
                        "",false
                    )
                )
                viewAdapter.notifyDataSetChanged()
            })

        // Se añade la peticion a la cola
        queue.add(stringRequest)
    }
}