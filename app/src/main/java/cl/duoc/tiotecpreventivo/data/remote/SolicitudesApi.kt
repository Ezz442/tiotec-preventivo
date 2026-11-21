package cl.duoc.tiotecpreventivo.data.remote

import cl.duoc.tiotecpreventivo.data.remote.model.CambioEstadoPeticion
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudDto
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudPeticion
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// #endpoints Retrofit de solicitudes
/** Endpoints de solicitudes-service (puerto 8081). Documentados en backend/solicitudes-service/README.md. */
interface SolicitudesApi {

    // #listar solicitudes
    @GET("api/solicitudes")
    suspend fun listar(): List<SolicitudDto>

    // #obtener una solicitud
    @GET("api/solicitudes/{id}")
    suspend fun obtener(@Path("id") id: Long): SolicitudDto

    // #crear solicitud
    @POST("api/solicitudes")
    suspend fun crear(@Body peticion: SolicitudPeticion): SolicitudDto

    // #editar solicitud
    @PUT("api/solicitudes/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body peticion: SolicitudPeticion): SolicitudDto

    // #cambiar estado de la solicitud
    @PATCH("api/solicitudes/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body cambio: CambioEstadoPeticion): SolicitudDto

    // #eliminar solicitud
    @DELETE("api/solicitudes/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
