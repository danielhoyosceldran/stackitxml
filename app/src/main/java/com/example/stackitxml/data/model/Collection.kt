package com.example.stackitxml.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Collection(
    @DocumentId // Mapeja l'ID del document de Firestore a aquesta propietat
    val collectionId: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(), // Llista d'UIDs dels membres que tenen accés
    @ServerTimestamp // Anotació per obtenir la marca de temps del servidor de Firestore
    val createdAt: Date? = null
)
