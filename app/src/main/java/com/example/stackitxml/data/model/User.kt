package com.example.stackitxml.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val accessibleCollectionIds: List<String> = emptyList()
)
