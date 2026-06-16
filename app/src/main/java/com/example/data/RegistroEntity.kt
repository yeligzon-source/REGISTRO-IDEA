package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String, // Tracks ownership of registration

    // Compatibility fields
    val title: String, // "Nombre Apellido"
    val category: String, // "Célula", "Bautismo", "Capacitación", "Univ. Vida", "Seguimiento", "Otros"
    val content: String, // Quick contact/detail summary
    val timestamp: Long,
    val rating: Int = 0,
    val notes: String = "",

    // 1. INFORMACIÓN PERSONAL
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val cedula: String = "",
    val cumpleanos: String = "",
    val direccionCompleta: String = "",
    val quienInvito: String = "",

    // 2. SEGUIMIENTO
    val llamada1: Boolean = false,
    val llamada2: Boolean = false,
    val llamada3: Boolean = false,
    val visita: Boolean = false,
    val nombreLiderVisito: String = "",

    // 3. PROCESO CREYENTE
    val fechaConversion: String = "",
    val universidadVida: Boolean = false,
    val universidadVidaInicio: String = "",
    val universidadVidaFin: String = "",
    val bautismo: Boolean = false,
    val fechaBautismo: String = "",
    val bautismoOtraCongregacion: Boolean = false,
    val nombreCongregacion: String = "",
    val capacitacionDestino: Boolean = false,
    val capacitacionDestinoInicio: String = "",
    val capacitacionDestinoFin: String = "",
    val capacitacionDestinoNivel1: Boolean = false,
    val capacitacionDestinoNivel2: Boolean = false,
    val capacitacionDestinoNivel3: Boolean = false,
    val celula: Boolean = false,
    val nombreCelula: String = "",
    val rolCelula: String = "", // "Líder", "Anfitrión", "Timoteo", "Asistente"
    val territorioCelula: String = "",
    val fechaIngresoCelula: String = "",
    val estadoCivil: String = "", // "Soltero", "Casado", "Divorciado", "Viudo"
    val liderAsignado: String = "",
    val redAsignada: String = ""
)
