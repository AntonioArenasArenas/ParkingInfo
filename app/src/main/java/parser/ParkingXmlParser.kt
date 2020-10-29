package parser

import android.content.Context
import android.util.Xml
import com.example.myapplication.R
import model.Parking
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

private val ns: String? = null

class ParkingXmlParser(private val context: Context) {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Parking> {
        inputStream.use { inputStreamUse->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStreamUse, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<Parking> {
        val parkings = mutableListOf<Parking>()

        parser.require(XmlPullParser.START_TAG, ns, "aparcamientos")
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "aparcamiento") {
                parkings.add(readParking(parser))
            } else {
                skip(parser)
            }

        }
        return parkings
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readParking(parser: XmlPullParser): Parking {
        parser.require(XmlPullParser.START_TAG, ns, "aparcamiento")

        val codigo = parser.getAttributeValue(null, "codigo")

        val name = parser.getAttributeValue(null, "nombre")

        var total = parser.getAttributeValue(null, "capacidad")
        if (total == null) {
            total = "-"
        }
        var libres = parser.getAttributeValue(null, "plazaslibres")
        if (libres == null) {
            libres = "-"
        }
        val posicion = parser.getAttributeValue(null, "coordenadas")

        var precio = parser.getAttributeValue(null, "precio")
        when {
            precio == null -> {
                precio = "-"
            }
            precio.contains("d") -> {
                precio=precio.removeSuffix("d")
                precio += " €/"+ context.getString(R.string.day)
            }
            precio.contains("h") -> {
                precio=precio.removeSuffix("h")
                precio += " €/"+ context.getString(R.string.hour)
            }
        }
        var horaInicio = parser.getAttributeValue(null, "hora_inicio")
        if (horaInicio == null) {
            horaInicio = ""
        }
        var horaFin = parser.getAttributeValue(null, "hora_fin")
        if (horaFin == null) {
            horaFin = ""
        }

        return Parking(codigo, name, total, libres, posicion, precio, horaInicio, horaFin,false)
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}