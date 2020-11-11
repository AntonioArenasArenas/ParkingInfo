package model

/**Modelo de Parking. Algunas propiedades pueden no estar disponibles al leer los XML
 *
 * @param codigo Codigo que identifica a cada parking
 * @param name Nombre del parking
 * @param total Número de plazas totales del parking
 * @param libres Número de plazas libres del parking
 * @param posicion Latitud y longitud donde se sitúa el parking
 * @param precio Precio del parking
 * @param hora_inicio Hora de apertura del parking
 * @param hora_fin Hora de cierre del parking
 * @param expanded True si el parking está expandido en la lista en el momento actual*/
class Parking(var codigo: String, var name: String, var total: String?, var libres: String?, var posicion: String, var precio: String?,
              var hora_inicio: String?, var hora_fin: String?, var expanded: Boolean)