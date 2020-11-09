package adapter

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
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

    var filterList: List<Parking> = myDataset

    var originalList: List<Parking> = myDataset

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

        val parking = filterList[position]
        holder.view.parkingName.text = parking.name
        //Se comprueba si esta o no expandido el parking
        val isExpanded = parking.expanded
        ocultarCamposExpandidos(isExpanded, holder)
        //Ocultar campos si hay fallo de conexion
        if (parking.name.contains("Error")) {
            ocultarCamposError(holder)
        } else {
            holder.view.parkingFreeCount.text = parking.libres
            holder.view.parkingPlacesCount.text = parking.total
            holder.view.price_value.text = parking.precio
            holder.view.schedule_value.text = context.getString(
                R.string.schedule_value,
                parking.hora_inicio,
                parking.hora_fin
            )
            holder.view.mapView.setOnClickListener {
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=" + parking.posicion)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                mapIntent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(mapIntent)
            }
        }

        holder.view.setOnClickListener {
            // Ver si el item actual esta o no abierto
            val expanded: Boolean = parking.expanded
            // Cambiar al hacer clik el estado del item
            parking.expanded = !expanded
            // Cambiarlo en la lista
            notifyItemChanged(position)
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
        holder.view.price.visibility = View.GONE
        holder.view.price_value.visibility = View.GONE
        holder.view.schedule.visibility = View.GONE
        holder.view.schedule_value.visibility = View.GONE

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
                        val precio = filters[0]
                        //Este método deja el precio sólo con los números
                        val precioParking = parking.precio?.replace(Regex("[^\\d+(.\\d+)*\$]"), "")
                        if (precioParking?.toDouble()!! > precio.toDouble()) {
                            isCorrect = false
                        }
                    }
                    //Distancia
                    if (filters[1] != "-") {
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
                        val distance =
                            results[0] / 1000 //Conversion a Km ya que el método lo devuelve en metros
                        if (distance > max) {
                            isCorrect = false
                        }


                    }

                    //Abierto
                    if (filters[2].toBoolean()) {
                        val currentTime: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                        if (parking.hora_inicio != "") {
                            if (parking.hora_inicio?.toInt()!! > currentTime || currentTime > parking.hora_fin?.toInt()!!) {
                                isCorrect = false

                            }
                        }
                    }
                    //Libre
                    if (filters[3].toBoolean()) {
                        if (parking.libres!="") {
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
    fun updateLocation(newLocation: Location?) {
        this.currentLocation = newLocation

    }


}