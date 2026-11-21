package cl.duoc.tiotecpreventivo.data.remote

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.HistorialSolicitudEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoDto
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudDto
import java.util.Locale

/** Convierte el texto del JSON en un enum de la app; null si el servicio manda un valor que no conocemos. */
private inline fun <reified T : Enum<T>> String.aEnumOrNull(): T? =
    enumValues<T>().firstOrNull { it.name == this }

// #convierte la respuesta del servidor a datos locales (Room)
/** null si la categoría o el estado no existen en la app: ese registro se ignora en vez de romper todo. */
fun SolicitudDto.aEntidad(perfilId: Long): SolicitudEntity? {
    val categoriaLocal = categoria.aEnumOrNull<Categoria>() ?: return null
    val estadoLocal = estado.aEnumOrNull<EstadoSolicitud>() ?: return null
    return SolicitudEntity(
        id = id,
        perfilId = perfilId,
        codigo = codigo ?: String.format(Locale.ROOT, "DEMO-%03d", id),
        categoria = categoriaLocal,
        estado = estadoLocal,
        creadaEn = creadaEn
    )
}

fun SolicitudDto.historialEntidades(): List<HistorialSolicitudEntity> =
    historial.orEmpty().mapNotNull { registro ->
        val estadoLocal = registro.estado.aEnumOrNull<EstadoSolicitud>() ?: return@mapNotNull null
        HistorialSolicitudEntity(
            id = registro.id,
            solicitudId = id,
            estado = estadoLocal,
            fecha = registro.fecha,
            nota = registro.nota
        )
    }

// #convierte dispositivo del servidor a dato local (Room)
fun DispositivoDto.aEntidad(perfilId: Long, imagenRuta: String? = null): DispositivoEntity? {
    val sistema = sistemaOperativo.aEnumOrNull<SistemaOperativo>() ?: return null
    return DispositivoEntity(
        id = id,
        perfilId = perfilId,
        nombre = nombre,
        sistemaOperativo = sistema,
        imagenRuta = imagenRuta,
        archivado = archivado,
        creadoEn = creadoEn,
        actualizadoEn = actualizadoEn
    )
}
