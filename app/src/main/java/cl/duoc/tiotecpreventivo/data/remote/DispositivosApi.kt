package cl.duoc.tiotecpreventivo.data.remote

import cl.duoc.tiotecpreventivo.data.remote.model.ArchivadoPeticion
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoDto
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoPeticion
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// #endpoints Retrofit de dispositivos
/** Endpoints de dispositivos-service (puerto 8082). Documentados en backend/dispositivos-service/README.md. */
interface DispositivosApi {

    // #listar dispositivos
    @GET("api/dispositivos")
    suspend fun listar(): List<DispositivoDto>

    // #obtener un dispositivo
    @GET("api/dispositivos/{id}")
    suspend fun obtener(@Path("id") id: Long): DispositivoDto

    // #crear dispositivo
    @POST("api/dispositivos")
    suspend fun crear(@Body peticion: DispositivoPeticion): DispositivoDto

    // #editar dispositivo
    @PUT("api/dispositivos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body peticion: DispositivoPeticion): DispositivoDto

    // #archivar dispositivo
    @PATCH("api/dispositivos/{id}/archivado")
    suspend fun cambiarArchivado(@Path("id") id: Long, @Body cuerpo: ArchivadoPeticion): DispositivoDto

    // #eliminar dispositivo
    @DELETE("api/dispositivos/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
