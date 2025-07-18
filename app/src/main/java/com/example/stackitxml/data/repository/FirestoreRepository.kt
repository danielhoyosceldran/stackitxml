package com.example.stackitxml.data.repository

import com.example.stackitxml.data.model.Collection
import com.example.stackitxml.data.model.Item
import com.example.stackitxml.data.model.User
import com.example.stackitxml.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Mètode per registrar un nou usuari
    suspend fun registerUser(email: String, password: String, username: String): Result<User> {
        return try {
            val usernameExists = isUsernameTaken(username)
            if (usernameExists) {
                return Result.failure(Exception("El nom d'usuari ja està en ús. Si us plau, tria un altre."))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("No s'ha pogut obtenir l'usuari de Firebase.")

            val userId = firebaseUser.uid
            val newUser = User(
                userId = userId,
                email = email,
                username = username,
                accessibleCollectionIds = emptyList()
            )

            db.collection(Constants.COLLECTION_USERS).document(userId).set(newUser).await()

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mètode per iniciar sessió
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser ?: throw Exception("No s'ha pogut obtenir l'usuari actual.")

            val userDocument = db.collection(Constants.COLLECTION_USERS).document(firebaseUser.uid).get().await()
            val user = userDocument.toObject(User::class.java) ?: throw Exception("No s'han trobat dades d'usuari a Firestore.")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mètode per verificar si un nom d'usuari ja està en ús
    private suspend fun isUsernameTaken(username: String): Boolean {
        val querySnapshot = db.collection(Constants.COLLECTION_USERS)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()
        return !querySnapshot.isEmpty
    }

    // Mètode per obtenir detalls d'un usuari per ID (útil per estadístiques i mostrar noms)
    suspend fun getUserDetails(userId: String): Result<User> {
        return try {
            val documentSnapshot = db.collection(Constants.COLLECTION_USERS).document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuari no trobat a Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mètode per obtenir l'ID de l'usuari actual
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Mètode per tancar sessió
    fun signOut() {
        auth.signOut()
    }

    // --- MÈTODES PER A COL·LECCIONS ---

    /**
     * Crea una nova col·lecció a Firestore i l'assigna a l'usuari actual.
     * També actualitza la llista de col·leccions accessibles de l'usuari.
     */
    suspend fun createCollection(name: String, description: String, ownerId: String): Result<Collection> {
        return try {
            val newCollection = Collection(
                name = name,
                description = description,
                ownerId = ownerId,
                memberIds = listOf(ownerId) // Inicialment només el creador és membre
            )
            val docRef = db.collection(Constants.COLLECTION_COLLECTIONS).add(newCollection).await()
            val collectionId = docRef.id

            // Actualitzar l'ID de la col·lecció a l'objecte retornat
            val createdCollection = newCollection.copy(collectionId = collectionId)

            // Actualitzar la llista de col·leccions accessibles de l'usuari
            db.collection(Constants.COLLECTION_USERS).document(ownerId)
                .update("accessibleCollectionIds", FieldValue.arrayUnion(collectionId))
                .await()

            Result.success(createdCollection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obté una col·lecció per la seva ID.
     */
    suspend fun getCollectionById(collectionId: String): Result<Collection> {
        return try {
            val docSnapshot = db.collection(Constants.COLLECTION_COLLECTIONS).document(collectionId).get().await()
            val collection = docSnapshot.toObject(Collection::class.java)
            if (collection != null) {
                Result.success(collection)
            } else {
                Result.failure(Exception("Col·lecció no trobada amb ID: $collectionId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obté totes les col·leccions a les quals l'usuari té accés (és membre).
     */
    suspend fun getCollectionsForUser(userId: String): Result<List<Collection>> {
        return try {
            val userDoc = db.collection(Constants.COLLECTION_USERS).document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            val accessibleIds = user?.accessibleCollectionIds ?: emptyList()

            if (accessibleIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val collections = mutableListOf<Collection>()
            val chunks = accessibleIds.chunked(10) // Divideix les IDs en trossos de 10 (límit de whereIn)

            for (chunk in chunks) {
                val querySnapshot = db.collection(Constants.COLLECTION_COLLECTIONS)
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                querySnapshot.documents.mapNotNullTo(collections) { it.toObject(Collection::class.java) }
            }

            Result.success(collections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Comparteix una col·lecció amb un altre usuari.
     * Afegeix l'usuari a la llista de membres de la col·lecció i la col·lecció a la llista d'accessibles de l'usuari.
     */
    suspend fun shareCollection(collectionId: String, recipientEmail: String): Result<Unit> {
        return try {
            // 1. Trobar l'usuari receptor per correu electrònic
            val recipientQuery = db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("email", recipientEmail)
                .limit(1)
                .get()
                .await()

            val recipientUser = recipientQuery.documents.firstOrNull()?.toObject(User::class.java)
                ?: return Result.failure(Exception("Usuari amb aquest correu electrònic no trobat."))

            val recipientUserId = recipientUser.userId

            // 2. Afegir el recipient a la llista de membres de la col·lecció
            db.collection(Constants.COLLECTION_COLLECTIONS).document(collectionId)
                .update("memberIds", FieldValue.arrayUnion(recipientUserId))
                .await()

            // 3. Afegir la col·lecció a la llista de col·leccions accessibles del recipient
            db.collection(Constants.COLLECTION_USERS).document(recipientUserId)
                .update("accessibleCollectionIds", FieldValue.arrayUnion(collectionId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MÈTODES PER A ÍTEMS ---

    /**
     * Afegeix un nou ítem a una col·lecció.
     */
    suspend fun addItemToCollection(collectionId: String, item: Item): Result<Item> {
        return try {
            val docRef = db.collection(Constants.COLLECTION_COLLECTIONS)
                .document(collectionId)
                .collection(Constants.COLLECTION_ITEMS)
                .add(item)
                .await()
            val itemId = docRef.id
            val createdItem = item.copy(itemId = itemId)
            Result.success(createdItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obté tots els ítems d'una col·lecció específica.
     */
    suspend fun getItemsInCollection(collectionId: String): Result<List<Item>> {
        return try {
            val querySnapshot = db.collection(Constants.COLLECTION_COLLECTIONS)
                .document(collectionId)
                .collection(Constants.COLLECTION_ITEMS)
                .get()
                .await()
            val items = querySnapshot.documents.mapNotNull { it.toObject(Item::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualitza el comptador personal d'un ítem i el totalCount.
     * @param collectionId L'ID de la col·lecció a la qual pertany l'ítem.
     * @param itemId L'ID de l'ítem a actualitzar.
     * @param userId L'ID de l'usuari el comptador del qual s'actualitza.
     * @param newCount El nou valor del comptador personal.
     */
    suspend fun updateItemCount(collectionId: String, itemId: String, userId: String, newCount: Long): Result<Unit> {
        return try {
            val itemRef = db.collection(Constants.COLLECTION_COLLECTIONS)
                .document(collectionId)
                .collection(Constants.COLLECTION_ITEMS)
                .document(itemId)

            // Obtenir l'ítem actual per actualitzar personalCount i recalcular totalCount
            val itemDoc = itemRef.get().await()
            val currentItem = itemDoc.toObject(Item::class.java)
                ?: return Result.failure(Exception("Ítem no trobat per actualitzar el comptador."))

            val updatedPersonalCount = currentItem.personalCount.toMutableMap()
            updatedPersonalCount[userId] = newCount

            // Recalcular totalCount sumant tots els valors de updatedPersonalCount
            val newTotalCount = updatedPersonalCount.values.sum()

            itemRef.update(
                mapOf(
                    "personalCount" to updatedPersonalCount,
                    "totalCount" to newTotalCount
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
