package cl.duoc.tiotecpreventivo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.R
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.FormularioRecordatorio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formulario de recordatorio con los selectores de fecha y hora de Material 3 (TT-16).
 * No valida: muestra los errores por campo que calcula RecordatorioViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatorioDialogo(
    formulario: FormularioRecordatorio,
    guardando: Boolean,
    errorOperacion: String?,
    onFechaChange: (fechaUtc: Long) -> Unit,
    onHoraChange: (hora: Int, minuto: Int) -> Unit,
    onGuardar: () -> Unit,
    onCancelarRecordatorio: (recordatorioId: Long) -> Unit,
    onCerrar: () -> Unit
) {
    var eligiendoFecha by rememberSaveable { mutableStateOf(false) }
    var eligiendoHora by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        title = {
            Text(if (formulario.recordatorioId == null) "Programar recordatorio" else "Editar recordatorio")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(formulario.titulo, style = MaterialTheme.typography.titleSmall)

                OutlinedButton(
                    onClick = { eligiendoFecha = true },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formulario.fechaUtc?.let { "Fecha: ${formatearFechaUtc(it)}" } ?: "Elegir fecha")
                }
                formulario.errorFecha?.let { ErrorCampo(it) }

                OutlinedButton(
                    onClick = { eligiendoHora = true },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val hora = formulario.hora
                    val minuto = formulario.minuto
                    Text(
                        if (hora != null && minuto != null) "Hora: %02d:%02d".format(hora, minuto)
                        else "Elegir hora"
                    )
                }
                formulario.errorHora?.let { ErrorCampo(it) }

                errorOperacion?.let { ErrorCampo(it) }

                formulario.recordatorioId?.let { id ->
                    TextButton(onClick = { onCancelarRecordatorio(id) }, enabled = !guardando) {
                        Text("Cancelar recordatorio", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onGuardar, enabled = !guardando) {
                Text(if (guardando) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !guardando) { Text("Cerrar") }
        }
    )

    if (eligiendoFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = formulario.fechaUtc ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { eligiendoFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let(onFechaChange)
                    eligiendoFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { eligiendoFecha = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (eligiendoHora) {
        val estadoHora = rememberTimePickerState(
            initialHour = formulario.hora ?: 9,
            initialMinute = formulario.minuto ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { eligiendoHora = false },
            title = { Text("Elige la hora") },
            text = { TimePicker(state = estadoHora) },
            confirmButton = {
                TextButton(onClick = {
                    onHoraChange(estadoHora.hour, estadoHora.minute)
                    eligiendoHora = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { eligiendoHora = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ErrorCampo(texto: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_riesgo_alto),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Text(texto, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

/** El DatePicker entrega medianoche UTC: se formatea en UTC para no mostrar el día anterior. */
fun formatearFechaUtc(fechaUtc: Long): String =
    SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL"))
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(fechaUtc))

@Preview(showBackground = true, name = "Recordatorio con errores")
@Composable
fun RecordatorioDialogoPreview() {
    TioTecPreventivoTheme {
        RecordatorioDialogo(
            formulario = FormularioRecordatorio(
                accionId = 1,
                titulo = "Activar la verificación en dos pasos",
                errorFecha = "Elige una fecha.",
                errorHora = "Elige una hora."
            ),
            guardando = false,
            errorOperacion = null,
            onFechaChange = {}, onHoraChange = { _, _ -> }, onGuardar = {},
            onCancelarRecordatorio = {}, onCerrar = {}
        )
    }
}
