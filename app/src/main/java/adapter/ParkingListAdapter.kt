package adapter

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.parking.view.*
import model.Parking

class ParkingListAdapter(private val myDataset: ArrayList<Parking>, private val context: Context) :
    RecyclerView.Adapter<ParkingListAdapter.MyViewHolder>() {

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

        val parking =myDataset[position]
        holder.view.parkingName.text=parking.name
        //Se comprueba si esta o no expandido el parking
        val isExpanded=parking.expanded
        ocultarCamposExpandidos(isExpanded,holder)
        //Ocultar campos si hay fallo de conexion
        if(parking.name.contains("Error")){
            ocultarCamposError(holder)
        }else {
            holder.view.parkingFreeCount.text=parking.libres
            holder.view.parkingPlacesCount.text=parking.total
            holder.view.price_value.text=parking.precio
            holder.view.schedule_value.text=context.getString(R.string.schedule_value,parking.hora_inicio,parking.hora_fin)
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
            // Get the current state of the item
            val expanded: Boolean = parking.expanded
            // Change the state
            parking.expanded=!expanded
            // Notify the adapter that item has changed
            notifyItemChanged(position)
        }

    }

    private fun ocultarCamposError(holder: MyViewHolder){
        holder.view.mapView.visibility=View.GONE
        holder.view.parkingFreeCount.visibility=View.GONE
        holder.view.parkingPlacesCount.visibility=View.GONE
        holder.view.parkingFree.visibility=View.GONE
        holder.view.parkingPlaces.visibility=View.GONE
        holder.view.price.visibility=View.GONE
        holder.view.price_value.visibility=View.GONE
        holder.view.schedule.visibility=View.GONE
        holder.view.schedule_value.visibility=View.GONE

    }

    private fun ocultarCamposExpandidos(expandido: Boolean, holder: MyViewHolder){

        if(expandido){
            holder.view.price.visibility=View.VISIBLE
            holder.view.price_value.visibility=View.VISIBLE
            holder.view.schedule.visibility=View.VISIBLE
            holder.view.schedule_value.visibility=View.VISIBLE
            holder.view.morelessarrow.setImageResource(R.drawable.ic_minus)

        }else{
            holder.view.price.visibility=View.GONE
            holder.view.price_value.visibility=View.GONE
            holder.view.schedule.visibility=View.GONE
            holder.view.schedule_value.visibility=View.GONE
        }
    }


    override fun getItemCount() = myDataset.size
}