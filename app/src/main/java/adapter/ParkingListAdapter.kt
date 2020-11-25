package adapter

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.parking.view.*
import model.Parking
import java.util.*
import kotlin.collections.ArrayList


class ParkingListAdapter(
    myDataset: ArrayList<Parking>,
    private val context: Context
) :
    RecyclerView.Adapter<ParkingListAdapter.MyViewHolder>(), Filterable {

    private var currentLocation: Location? = null

    private lateinit var sharedPref: SharedPreferences

    private var filterList: ArrayList<Parking> = myDataset

    private var originalList: ArrayList<Parking> = myDataset

    private var currentTab: Int = 0


    // Conectar cada item de la lista con su vista correspondiente
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Creacion de nuevas vistas
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val parkingView = LayoutInflater.from(parent.context)
            .inflate(R.layout.parking, parent, false)


        return MyViewHolder(parkingView)
    }

    // Setear el contenido de cada item, holder es la vista y position la posicion de la lista de item que estamos tratando

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        sharedPref = context.getSharedPreferences("Favoritos", Context.MODE_PRIVATE)
        val parking = filterList[position]
        holder.view.parkingName.text = parking.name
        //Se comprueba si esta o no expandido el parking
        val isExpanded = parking.expanded
        ocultarCamposExpandidos(isExpanded, holder)
        //Ocultar campos si hay fallo de conexion
        if (parking.name.contains("Error")) {
            ocultarCamposError(holder)
        } else {
            visibleCamposNoError(holder)
            holder.view.parkingFreeCount.text = parking.libres
            holder.view.parkingPlacesCount.text = parking.total
            holder.view.price_value.text = parking.precio
            holder.view.schedule_value.text = context.getString(
                R.string.schedule_value,
                parking.hora_inicio,
                parking.hora_fin
            )
            val codesMutable = getFavorites()
            if (codesMutable.contains(parking.codigo)) {
                holder.view.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
            } else {
                holder.view.favoriteButton.setImageResource(R.drawable.ic_favorite)
            }
        }


        //Intent del mapa
        holder.view.mapView.setOnClickListener {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=" + parking.posicion)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(mapIntent)
        }

        //Botón para expandir
        holder.view.setOnClickListener {
            // Ver si el item actual esta o no abierto
            val expanded: Boolean = parking.expanded
            // Cambiar al hacer clik el estado del item
            parking.expanded = !expanded
            // Cambiarlo en la lista
            notifyItemChanged(position)
        }

        //Botón para hacer favorito un parking
        holder.view.favoriteButton.setOnClickListener {
            val codesMutable = getFavorites()

            //Ya estaba en favorito, lo quitamos
            if (codesMutable.contains(parking.codigo)) {
                holder.view.favoriteButton.setImageResource(R.drawable.ic_favorite)
                codesMutable.remove(parking.codigo)
                if (currentTab == 1) {
                    filterList.removeAt(position)
                    originalList.remove(parking)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, filterList.size)
                }

                //No estaba en favoritos
            } else {
                holder.view.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                codesMutable.add(parking.codigo)

            }
            var codeString = codesMutable.joinToString()
            codeString = codeString.replace("\\s".toRegex(), "")
            with(sharedPref.edit()) {
                putString("Favoritos", codeString)
                commit()

            }


        }
    }

    /** Método para ocultar los campos que no corresponden si hay un fallo al obtener la lista
     *
     * @param holder ViewHolder sobre el que se ejecuta la acción */
    private fun ocultarCamposError(holder: MyViewHolder) {
        holder.view.mapView.visibility = View.GONE
        holder.view.parkingFreeCount.visibility = View.GONE
        holder.view.parkingPlacesCount.visibility = View.GONE
        holder.view.parkingFree.visibility = View.GONE
        holder.view.parkingPlaces.visibility = View.GONE
        holder.view.favoriteButton.visibility=View.GONE

    }

    /** Método para visibilizar los campos cuando no hay fallo al visibilizar la lista
     *
     * @param holder ViewHolder sobre el que se ejecuta la acción */
    private fun visibleCamposNoError(holder: MyViewHolder) {
        holder.view.mapView.visibility = View.VISIBLE
        holder.view.parkingFreeCount.visibility = View.VISIBLE
        holder.view.parkingPlacesCount.visibility = View.VISIBLE
        holder.view.parkingFree.visibility = View.VISIBLE
        holder.view.parkingPlaces.visibility = View.VISIBLE
        holder.view.favoriteButton.visibility=View.VISIBLE

    }

    /** Método para ocultar o mostrar los campos en función de si está expandido o no el parking
     *
     * @param holder ViewHolder sobre el que se ejecuta la acción
     * @param expandido este valor será true si el parking está expandido en este momento */
    private fun ocultarCamposExpandidos(expandido: Boolean, holder: MyViewHolder) {

        if (expandido) {
            holder.view.price.visibility = View.VISIBLE
            holder.view.price_value.visibility = View.VISIBLE
            holder.view.schedule.visibility = View.VISIBLE
            holder.view.schedule_value.visibility = View.VISIBLE
            holder.view.morelessarrow.setImageResource(R.drawable.ic_minus)

        } else {
            holder.view.price.visibility = View.GONE
            holder.view.price_value.visibility = View.GONE
            holder.view.schedule.visibility = View.GONE
            holder.view.schedule_value.visibility = View.GONE

        }
    }


    override fun getItemCount() = filterList.size


    override fun getFilter(): Filter {
        return object : Filter() {

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {

                if (filterResults.values != null) {
                    filterList = filterResults.values as ArrayList<Parking>
                }
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()

                val filterResults = FilterResults()

                val filters: List<String>? = queryString?.split(",")

                val nList = ArrayList<Parking>()


                //Los filtros están puestos en este orden precio,distancia,libre y abierto

                for (parking in originalList) {
                    var isCorrect = true
                    //Precio
                    if (!filters?.get(0)?.equals("-")!! && !parking.precio.equals("-")) {
                        var precio = filters[0]
                        //Este método deja el precio sólo con los números
                        val precioParking = parking.precio?.replace(Regex("[^\\d+(.\\d+)*\$]"), "")
                        if(precio.contains("d")){
                            if(!parking.precio?.contains("d")!!){
                                precio=precio.removeSuffix("d")
                                precio=(precio.toDouble()/24).toString()
                            }
                            precio=precio.removeSuffix("d")
                        }else if(!parking.precio?.contains("h")!!){
                            precio=(precio.toDouble()*24).toString()
                        }
                        if (precioParking?.toDouble()!! > precio.toDouble()) {
                            isCorrect = false
                        }
                    }
                    //Distancia
                    if (filters[1] != "-") {
                        //Si se apaga la ubicación antes de poder obtener una ubicación se comunica con un Toast
                        if (currentLocation == null) {
                            val info = Toast.makeText(
                                context,
                                context.getString(R.string.turn_on_location),
                                Toast.LENGTH_LONG
                            )
                            info.show()

                        } else {
                            val max = filters[1].toDouble()

                            val latitude = currentLocation?.latitude
                            val longitude = currentLocation?.longitude

                            val posicionArray = parking.posicion.split(",")
                            val latitudeParking = posicionArray[0].toDouble()
                            val longitudeParking = posicionArray[1].toDouble()
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                latitude!!,
                                longitude!!,
                                latitudeParking,
                                longitudeParking,
                                results
                            )
                            val distance = results[0]
                            if (distance > max) {
                                isCorrect = false
                            }

                        }
                    }

                    //Abierto
                    if (filters[2].toBoolean()) {
                        val currentTime: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                        if (parking.hora_inicio != "") {
                            if (parking.hora_inicio?.toInt()!! >= currentTime || currentTime > parking.hora_fin?.toInt()!!) {
                                isCorrect = false

                            }
                        }
                    }
                    //Libre
                    if (filters[3].toBoolean()) {
                        if (parking.libres != "") {
                            if (parking.libres?.toInt() == 0) {
                                isCorrect = false
                            }
                        }
                    }

                    if (isCorrect) {
                        nList.add(parking)
                    }

                }
                filterResults.values = nList
                filterResults.count = nList.size


                return filterResults
            }


        }
    }

    /**Actualiza la localización que va obteniendo el MainActivity
     *
     * @param newLocation: Nueva localización a actualizar
     * */
    fun setUpdateLocation(newLocation: Location?) {
        this.currentLocation = newLocation

    }

    /**Método que controla la actualización de la lista del Adapter cuando se pulsa el botón actualizar para que se actualicen los elementos en pantalla
     *
     * @param newList ArrayList que contiene la lista actualizada **completa** de la que sólo cogeremos aquellos parking que se estén mostrando en este momento*/
    fun updateFilterList(newList: ArrayList<Parking>) {
        val listaUpdated = ArrayList<Parking>()
        if (filterList.size != newList.size && filterList.isNotEmpty()) {
            for (parking in filterList) {
                val codigoFilter = parking.codigo
                for (originalParking in newList) {
                    if (codigoFilter == originalParking.codigo) {
                        listaUpdated.add(originalParking)
                    }
                }
            }
            filterList = listaUpdated
        }
    }

    /**Método que actualiza la lista del Adapter con los favoritos para poder mostrarlos
     *
     * @param codes ArrayList con los códigos de los parking seleccionados como favoritos
     * @param list Arraylist donde se devuelven los parking*/
    fun displayFavorites(codes: ArrayList<String>, list: ArrayList<Parking>) {
        val operationalList = ArrayList<Parking>(list)
        list.clear()
        for (parking in operationalList) {
            if (codes.contains(parking.codigo)) {
                list.add(parking)
            }
        }

    }

    /**Método que obtiene los códigos de los parking marcados como favoritos
     *
     * @return MutableList<String> con los String de los códigos*/
    fun getFavorites(): MutableList<String> {
        val favs: String? = sharedPref.getString("Favoritos", "")
        val codesMutable: MutableList<String>
        val codes: List<String> = favs!!.split(",")
        codesMutable = if (favs.isEmpty()) {
            ArrayList<String>().toMutableList()
        } else {
            codes.toMutableList()
        }

        return codesMutable
    }

    /**Set de la propiedad currentTab muestra la pestaña actual*/
    fun setCurrentTab(current: Int) {
        currentTab = current
    }
    /**Set de la lista con los parking que cumplen el filtro */
    fun setfilterList(list: ArrayList<Parking>) {
        filterList = list
    }

    /**Método que controla la actualización de la lista del Adapter cuando se pulsa el botón actualizar para que se actualicen los elementos en pantalla
     *
     * @param newList ArrayList que contiene la lista actualizada **de los parking favoritos** de la que sólo cogeremos aquellos parking que se estén mostrando en este momento */
    fun updateFavoriteList(newList: ArrayList<Parking>) {
        val listaUpdated = ArrayList<Parking>()

        for (parking in filterList) {
            val codigoFilter = parking.codigo
            for (originalParking in newList) {
                if (codigoFilter == originalParking.codigo) {
                    listaUpdated.add(originalParking)
                }
            }
        }
        filterList = listaUpdated

    }


}