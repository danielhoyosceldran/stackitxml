package com.example.stackitxml.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Item
import com.example.stackitxml.data.model.User
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.util.Constants
import com.example.stackitxml.util.DialogUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StatisticsActivity : AppCompatActivity() {

    private lateinit var statisticsTitleTextView: TextView
    private lateinit var collectionNameStatsTextView: TextView
    private lateinit var itemRowsContainer: LinearLayout // Contenidor per a les files d'ítems
    private lateinit var noItemsMessageTextView: TextView // Missatge quan no hi ha ítems

    private val firestoreRepository = FirestoreRepository()
    private var collectionId: String? = null
    private var currentUserId: String? = null // L'ID de l'usuari actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        enableEdgeToEdge()

        // Obtenir l'ID de la col·lecció de l'Intent
        collectionId = intent.getStringExtra(Constants.EXTRA_COLLECTION_ID)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Obtenir l'ID de l'usuari actual

        if (collectionId == null || currentUserId == null) {
            DialogUtils.showLongToast(this, "Error: Collection ID or user not found.")
            finish()
            return
        }

        // Inicialitza les vistes
        statisticsTitleTextView = findViewById(R.id.statisticsTitleTextView)
        collectionNameStatsTextView = findViewById(R.id.collectionNameStatsTextView)
        itemRowsContainer = findViewById(R.id.itemRowsContainer)
        noItemsMessageTextView = findViewById(R.id.noItemsMessageTextView)

        // Carrega les estadístiques
        loadStatistics()
    }

    /**
     * Carrega les dades de la col·lecció i calcula les estadístiques per a la taula de rànquing.
     */
    private fun loadStatistics() {
        collectionId?.let { collId ->
            currentUserId?.let { userId ->
                lifecycleScope.launch {
                    // Obtenir el nom de la col·lecció
                    val collectionResult = firestoreRepository.getCollectionById(collId)
                    collectionResult.onSuccess { collection ->
                        collectionNameStatsTextView.text = "${collection.name}"
                    }.onFailure { exception ->
                        DialogUtils.showLongToast(this@StatisticsActivity, "Error loading collection name: ${exception.message}")
                        collectionNameStatsTextView.text = "unknown"
                    }

                    // Obtenir tots els ítems de la col·lecció
                    val itemsResult = firestoreRepository.getItemsInCollection(collId)
                    itemsResult.onSuccess { items ->
                        if (items.isEmpty()) {
                            noItemsMessageTextView.visibility = View.VISIBLE
                            itemRowsContainer.visibility = View.GONE
                            return@onSuccess
                        }
                        noItemsMessageTextView.visibility = View.GONE
                        itemRowsContainer.visibility = View.VISIBLE
                        itemRowsContainer.removeAllViews() // Neteja les files anteriors

                        // Obtenir tots els usuaris per resoldre els noms (per a Main Contributor)
                        val usersResult = firestoreRepository.getAllUsers()
                        val usersMap = if (usersResult.isSuccess) {
                            usersResult.getOrNull()?.associateBy { it.userId } ?: emptyMap()
                        } else {
                            DialogUtils.showLongToast(this@StatisticsActivity, "Error loading users: ${usersResult.exceptionOrNull()?.message}")
                            emptyMap()
                        }

                        // Ordenar els ítems per totalCount de forma descendent
                        val sortedItems = items.sortedByDescending { it.totalCount }

                        // Afegir cada ítem com una fila a la taula
                        sortedItems.forEach { item ->
                            val mainContributorEntry = item.personalCount.maxByOrNull { it.value }
                            val mainContributorId = mainContributorEntry?.key
                            val mainContributorCount = mainContributorEntry?.value ?: 0L

                            val mainContributorUsername = usersMap[mainContributorId]?.username ?: "Unknown"

                            val myCount = item.personalCount[userId] ?: 0L

                            // Inflar la fila de l'ítem i omplir les dades
                            val itemRowView = LayoutInflater.from(this@StatisticsActivity)
                                .inflate(R.layout.item_statistics_row, itemRowsContainer, false) as LinearLayout

                            itemRowView.findViewById<TextView>(R.id.itemNameStatTextView).text = item.name
                            itemRowView.findViewById<TextView>(R.id.itemCountsTextView).text = item.totalCount.toString()
                            itemRowView.findViewById<TextView>(R.id.mainContributorTextView).text = "$mainContributorUsername ($mainContributorCount)"
                            itemRowView.findViewById<TextView>(R.id.myCountTextView).text = myCount.toString()

                            itemRowsContainer.addView(itemRowView)
                        }

                    }.onFailure { exception ->
                        DialogUtils.showLongToast(this@StatisticsActivity, "Error loading items for statistics: ${exception.message}")
                        noItemsMessageTextView.text = "Error loading data."
                        noItemsMessageTextView.visibility = View.VISIBLE
                        itemRowsContainer.visibility = View.GONE
                    }
                }
            }
        }
    }
}
