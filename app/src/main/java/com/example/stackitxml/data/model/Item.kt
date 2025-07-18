package com.example.stackitxml.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Item(
    @DocumentId // Mapeja l'ID del document de Firestore a aquesta propietat
    val itemId: String = "",
    val name: String = "",
    val creatorId: String = "",
    val personalCount: Map<String, Long> = emptyMap(), // Mapa d'UIDs d'usuari a comptadors personals
    val totalCount: Long = 0, // Suma total de tots els comptadors personals
    @ServerTimestamp // Anotació per obtenir la marca de temps del servidor de Firestore
    val createdAt: Date? = null
)