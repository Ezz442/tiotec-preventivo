package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.domain.ValidadorFormularios
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.repository.ResultadoEliminacion
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenes
import cl.duoc.tiotecpreventivo.data.imagenes.copiarImagen
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** Mensaje cuando se rechaza el permiso de cámara. */
const val PERMISO_CAMARA_DENEGADO =
    "Sin permiso de cámara no se puede tomar la foto. Puedes elegir una imagen de galería " +
        "o activar el permiso en Ajustes del teléfono."

// #estado y logica de Mis dispositivos (MVVM)
class DispositivosViewModel(
    private val dispositivoRepository: DispositivoRepository,
    private val perfilRepository: PerfilRepository,
    /** TT-18: archivos de las fotos. */
    private val almacenImagenes: AlmacenImagenes
) : ViewModel() {

    private val _uiState = MutableStateFlow(DispositivosUiState())
    // #estado que observa la pantalla
    val uiState = _uiState.asStateFlow()

    private var observacion: Job? = null
    private var sincronizacionJob: Job? = null

    init {
        observarDatos()
        sincronizar()
    }

    // #pide los dispositivos al microservicio
    /**
     * TT-29: pide la lista a dispositivos-service y actualiza la copia local. La pantalla ya muestra
     * lo que hay en Room; si el servicio no responde, se avisa y esos datos siguen visibles.
     */
    private fun sincronizar() {
        sincronizacionJob?.cancel()
        _uiState.update { it.copy(sincronizando = true, errorSincronizacion = null) }

        sincronizacionJob = viewModelScope.launch {
            try {
                dispositivoRepository.sincronizar()
                _uiState.update { it.copy(sincronizando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(sincronizando = false, errorSincronizacion = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sincronizando = false,
                        errorSincronizacion = "No pudimos actualizar desde el servicio."
                    )
                }
            }
        }
    }

    // #escucha los cambios de Room
    private fun observarDatos() {
        observacion?.cancel()

        _uiState.update {
            it.copy(cargando = true, errorCarga = null)
        }

        observacion = viewModelScope.launch {
            try {
                combine(
                    perfilRepository.observarPerfil(), dispositivoRepository.observarActivos()
                ) { perfil, dispositivos ->
                    perfil?.id to dispositivos
                }.collect { (perfilId, dispositivos) ->
                    _uiState.update {
                        it.copy(
                            perfilId = perfilId,
                            dispositivos = dispositivos,
                            cargando = false,
                            errorCarga = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false, errorCarga = "No pudimos cargar los dispositivos."
                    )
                }
            }
        }
    }

    fun reintentarCarga() {
        if (_uiState.value.cargando) return

        observarDatos()
        sincronizar()
    }

    // #abre el formulario para agregar dispositivo
    fun abrirNuevo() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.perfilId == null || estado.guardando || estado.eliminando) return

        _uiState.update {
            it.copy(
                formularioVisible = true,
                dispositivoEnEdicionId = null,
                nombre = "",
                sistemaOperativo = null,
                errorNombre = null,
                errorSistemaOperativo = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    // #abre el formulario para editar
    fun editarDispositivo(id: Long) {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.perfilId == null || estado.guardando || estado.eliminando) return

        val dispositivo = estado.dispositivos.find { it.id == id } ?: return

        _uiState.update {
            it.copy(
                formularioVisible = true,
                dispositivoEnEdicionId = dispositivo.id,
                nombre = dispositivo.nombre,
                sistemaOperativo = dispositivo.sistemaOperativo,
                errorNombre = null,
                errorSistemaOperativo = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    /**
     * TT-26: si hay cambios sin guardar, primero se pide confirmación con un AlertDialog;
     * si no hay cambios, el formulario se cierra de inmediato.
     */
    fun cancelarFormulario() {
        val estado = _uiState.value
        if (estado.guardando || !estado.formularioVisible) return

        val guardado = estado.dispositivoEnEdicionId?.let { id -> estado.dispositivos.find { it.id == id } }
        val hayCambios = ValidadorFormularios.hayCambiosDispositivo(
            nombre = estado.nombre,
            sistema = estado.sistemaOperativo,
            nombreGuardado = guardado?.nombre,
            sistemaGuardado = guardado?.sistemaOperativo
        )

        if (hayCambios) {
            _uiState.update { it.copy(confirmarDescartar = true) }
        } else {
            cerrarFormulario()
        }
    }

    /** Confirmó descartar: se cierra sin guardar. */
    fun descartarCambios() {
        if (_uiState.value.guardando) return
        cerrarFormulario()
    }

    /** Eligió seguir editando: el formulario queda igual. */
    fun seguirEditando() {
        _uiState.update { it.copy(confirmarDescartar = false) }
    }

    private fun cerrarFormulario() {
        _uiState.update {
            it.copy(
                formularioVisible = false,
                dispositivoEnEdicionId = null,
                nombre = "",
                sistemaOperativo = null,
                errorNombre = null,
                errorSistemaOperativo = null,
                errorOperacion = null,
                confirmarDescartar = false
            )
        }
    }

    // #actualiza el nombre y valida
    fun onNombreChange(nombre: String) {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                nombre = nombre, errorNombre = null, errorOperacion = null, mensaje = null
            )
        }
    }

    fun onSistemaOperativoChange(sistema: SistemaOperativo) {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                sistemaOperativo = sistema,
                errorSistemaOperativo = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    // #validar y guardar dispositivo (crear o editar)
    fun guardarDispositivo() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.eliminando || !estado.formularioVisible) return

        val perfilId = estado.perfilId ?: return
        val idEnEdicion = estado.dispositivoEnEdicionId

        // TT-26: las reglas viven en ValidadorFormularios; aquí solo se copian los errores.
        val validacion = ValidadorFormularios.validarDispositivo(estado.nombre, estado.sistemaOperativo)
        val nombreLimpio = validacion.nombreLimpio
        val sistema = estado.sistemaOperativo

        _uiState.update {
            it.copy(
                errorNombre = validacion.errorNombre,
                errorSistemaOperativo = validacion.errorSistemaOperativo,
                errorOperacion = null,
                mensaje = null
            )
        }

        if (!validacion.valida || sistema == null) return

        _uiState.update {
            it.copy(guardando = true)
        }

        viewModelScope.launch {
            try {
                val nombreRepetido = dispositivoRepository.existeNombreActivo(
                    nombre = nombreLimpio, excluirId = idEnEdicion ?: 0L
                )

                if (nombreRepetido) {
                    _uiState.update {
                        it.copy(
                            errorNombre = ValidadorFormularios.NOMBRE_REPETIDO
                        )
                    }
                    return@launch
                }

                if (idEnEdicion == null) {
                    dispositivoRepository.crear(
                        perfilId = perfilId, nombre = nombreLimpio, sistemaOperativo = sistema
                    )
                } else {
                    val actualizado = dispositivoRepository.actualizar(
                        id = idEnEdicion, nombre = nombreLimpio, sistemaOperativo = sistema
                    )

                    if (!actualizado) {
                        _uiState.update {
                            it.copy(
                                errorOperacion = "El dispositivo ya no está disponible."
                            )
                        }
                        return@launch
                    }
                }

                _uiState.update {
                    it.copy(
                        formularioVisible = false,
                        dispositivoEnEdicionId = null,
                        nombre = "",
                        sistemaOperativo = null,
                        errorNombre = null,
                        errorSistemaOperativo = null,
                        errorOperacion = null,
                        mensaje = if (idEnEdicion == null) {
                            "Dispositivo registrado correctamente."
                        } else {
                            "Dispositivo actualizado correctamente."
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(errorOperacion = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = "No pudimos guardar el dispositivo. Inténtalo nuevamente."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    // #pide confirmacion para eliminar
    fun solicitarEliminacion(id: Long) {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.eliminando || estado.formularioVisible) return

        val dispositivo = estado.dispositivos.find { it.id == id } ?: return

        _uiState.update {
            it.copy(
                dispositivoAEliminar = dispositivo, errorOperacion = null, mensaje = null
            )
        }
    }

    fun cancelarEliminacion() {
        if (_uiState.value.eliminando) return

        _uiState.update {
            it.copy(
                dispositivoAEliminar = null, errorOperacion = null
            )
        }
    }

    // #eliminar dispositivo (o archivar si tiene evaluaciones)
    fun confirmarEliminacion() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.eliminando || estado.formularioVisible) return

        val dispositivo = estado.dispositivoAEliminar ?: return

        _uiState.update {
            it.copy(
                eliminando = true, errorOperacion = null, mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val resultado = dispositivoRepository.eliminar(dispositivo.id)

                // TT-18: sin evaluaciones el registro se borra; su foto también.
                // Si se archiva, la foto se conserva junto con su historial.
                if (resultado is ResultadoEliminacion.Eliminado) {
                    almacenImagenes.borrar(resultado.imagenRuta)
                }

                _uiState.update { actual ->
                    when (resultado) {
                        is ResultadoEliminacion.Eliminado -> actual.copy(
                            dispositivoAEliminar = null,
                            mensaje = "Dispositivo eliminado correctamente."
                        )

                        ResultadoEliminacion.Archivado -> actual.copy(
                            dispositivoAEliminar = null,
                            mensaje = "Dispositivo archivado. Su historial se conserva."
                        )

                        ResultadoEliminacion.NoEncontrado -> actual.copy(
                            dispositivoAEliminar = null,
                            errorOperacion = "El dispositivo ya no está disponible."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(errorOperacion = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = "No pudimos eliminar el dispositivo. Inténtalo nuevamente."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(eliminando = false)
                }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update {
            it.copy(mensaje = null)
        }
    }

    // ---------- TT-18: foto con la cámara ----------

    private fun ocupado(estado: DispositivosUiState): Boolean =
        estado.cargando || estado.guardando || estado.eliminando || estado.procesandoFoto || estado.capturaFoto != null || estado.dispositivoGaleriaId != null || estado.formularioVisible

    // #preparar seleccion de imagen de galeria
    fun prepararGaleria(dispositivoId: Long): Boolean {
        val estado = _uiState.value

        if (ocupado(estado)) return false
        if (estado.dispositivos.none { it.id == dispositivoId }) return false

        _uiState.update {
            it.copy(
                dispositivoGaleriaId = dispositivoId, errorOperacion = null, mensaje = null
            )
        }

        return true
    }

    fun cancelarGaleria() {
        if (_uiState.value.procesandoFoto) return

        _uiState.update {
            it.copy(dispositivoGaleriaId = null)
        }
    }

    // #guardar la imagen elegida de galeria
    fun imagenSeleccionada(abrirOrigen: () -> InputStream?) {
        val estado = _uiState.value
        val dispositivoId = estado.dispositivoGaleriaId ?: return

        if (estado.procesandoFoto) return

        _uiState.update {
            it.copy(procesandoFoto = true, errorOperacion = null)
        }

        viewModelScope.launch {
            var rutaNueva: String? = null
            var guardada = false

            try {
                withContext(Dispatchers.IO) {
                    val ruta = almacenImagenes.nuevaRuta(dispositivoId)
                    rutaNueva = ruta
                    almacenImagenes.copiarImagen(ruta, abrirOrigen)
                }

                withContext(NonCancellable + Dispatchers.IO) {
                    val dispositivo = dispositivoRepository.obtener(dispositivoId)
                        ?: throw IOException("El dispositivo ya no existe.")

                    if (dispositivo.archivado) {
                        throw IOException("El dispositivo está archivado.")
                    }

                    dispositivoRepository.actualizarImagen(dispositivoId, rutaNueva)
                    guardada = true

                    runCatching {
                        almacenImagenes.borrar(dispositivo.imagenRuta)
                    }
                }

                _uiState.update {
                    it.copy(mensaje = "Imagen de galería guardada.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = "No pudimos guardar la imagen. Inténtalo nuevamente."
                    )
                }
            } finally {
                withContext(NonCancellable + Dispatchers.IO) {
                    if (!guardada) {
                        runCatching {
                            almacenImagenes.borrar(rutaNueva)
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        dispositivoGaleriaId = null, procesandoFoto = false
                    )
                }
            }
        }
    }

    // #preparar archivo para la foto de camara
    /**
     * Reserva una ruta para la foto y la devuelve para que la pantalla abra la cámara.
     * Devuelve null si no se puede tomar ahora (otra operación en curso).
     */
    fun prepararFoto(dispositivoId: Long): String? {
        val estado = _uiState.value
        if (ocupado(estado) || estado.dispositivos.none { it.id == dispositivoId }) return null

        return try {
            val ruta = almacenImagenes.nuevaRuta(dispositivoId)
            _uiState.update {
                it.copy(
                    capturaFoto = CapturaFoto(dispositivoId, ruta),
                    errorOperacion = null,
                    mensaje = null
                )
            }
            ruta
        } catch (e: Exception) {
            _uiState.update { it.copy(errorOperacion = "No pudimos preparar la foto. Inténtalo nuevamente.") }
            null
        }
    }

    // #guardar el resultado de la camara
    /**
     * Resultado de la cámara. `exito = false` si la persona canceló o la cámara falló:
     * se borra el archivo temporal y la foto anterior se conserva.
     */
    fun fotoTomada(exito: Boolean) {
        val captura = _uiState.value.capturaFoto ?: return

        if (!exito || !almacenImagenes.existe(captura.rutaTemporal)) {
            almacenImagenes.borrar(captura.rutaTemporal)
            _uiState.update {
                it.copy(
                    capturaFoto = null,
                    mensaje = "No se tomó la foto. La imagen anterior se mantiene."
                )
            }
            return
        }

        _uiState.update { it.copy(procesandoFoto = true) }
        viewModelScope.launch {
            try {
                val anterior = dispositivoRepository.obtener(captura.dispositivoId)?.imagenRuta
                dispositivoRepository.actualizarImagen(captura.dispositivoId, captura.rutaTemporal)
                // Se reemplazó: el archivo anterior ya no lo usa nadie.
                if (anterior != null && anterior != captura.rutaTemporal) almacenImagenes.borrar(
                    anterior
                )
                _uiState.update { it.copy(mensaje = "Foto guardada.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                almacenImagenes.borrar(captura.rutaTemporal)
                _uiState.update { it.copy(errorOperacion = "No pudimos guardar la foto. Inténtalo nuevamente.") }
            } finally {
                _uiState.update { it.copy(capturaFoto = null, procesandoFoto = false) }
            }
        }
    }

    // #aviso: el telefono no tiene camara
    /** El teléfono no tiene app de cámara: se avisa sin bloquear el resto de la app. */
    fun camaraNoDisponible() {
        _uiState.value.capturaFoto?.let { almacenImagenes.borrar(it.rutaTemporal) }
        _uiState.update {
            it.copy(
                capturaFoto = null,
                errorOperacion = "Este dispositivo no tiene una cámara disponible."
            )
        }
    }

    // #aviso: permiso de camara denegado
    /** La persona no concedió el permiso de cámara: se avisa y se puede usar la galería. */
    fun permisoCamaraDenegado() {
        _uiState.value.capturaFoto?.let { almacenImagenes.borrar(it.rutaTemporal) }
        _uiState.update {
            it.copy(
                capturaFoto = null,
                errorOperacion = PERMISO_CAMARA_DENEGADO
            )
        }
    }

    fun solicitarQuitarFoto(dispositivoId: Long) {
        val estado = _uiState.value
        if (ocupado(estado)) return
        val dispositivo = estado.dispositivos.find { it.id == dispositivoId } ?: return
        if (dispositivo.imagenRuta == null) return
        _uiState.update {
            it.copy(
                dispositivoQuitarFoto = dispositivo, errorOperacion = null, mensaje = null
            )
        }
    }

    fun cancelarQuitarFoto() {
        if (_uiState.value.procesandoFoto) return
        _uiState.update { it.copy(dispositivoQuitarFoto = null) }
    }

    // #quitar la foto del dispositivo
    fun confirmarQuitarFoto() {
        val dispositivo = _uiState.value.dispositivoQuitarFoto ?: return
        if (_uiState.value.procesandoFoto) return

        _uiState.update { it.copy(procesandoFoto = true) }
        viewModelScope.launch {
            try {
                dispositivoRepository.actualizarImagen(dispositivo.id, null)
                almacenImagenes.borrar(dispositivo.imagenRuta)
                _uiState.update { it.copy(dispositivoQuitarFoto = null, mensaje = "Foto quitada.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorOperacion = "No pudimos quitar la foto. Inténtalo nuevamente.") }
            } finally {
                _uiState.update { it.copy(procesandoFoto = false) }
            }
        }
    }
}