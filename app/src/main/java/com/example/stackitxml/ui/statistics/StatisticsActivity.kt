package com.example.stackitxml.ui.statistics

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Item
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.util.Constants
import com.example.stackitxml.util.DialogUtils
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var statisticsTitleTextView: TextView
    private lateinit var collectionNameStatsTextView: TextView
    private lateinit var mostCountedItemTextView: TextView
    private lateinit var mostCountedByTextView: TextView

    private val firestoreRepository = FirestoreRepository()
    private var collectionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Obtenir l'ID de la col·lecció de l'Intent
        collectionId = intent.getStringExtra(Constants.EXTRA_COLLECTION_ID)

        if (collectionId == null) {
            DialogUtils.showLongToast(this, "Error: No s'ha trobat l'ID de la col·lecció per a les estadístiques.")
            finish() // Tanca l'activitat si no hi ha ID
            return
        }

        // Inicialitza les vistes
        statisticsTitleTextView = findViewById(R.id.statisticsTitleTextView)
        collectionNameStatsTextView = findViewById(R.id.collectionNameStatsTextView)
        mostCountedItemTextView = findViewById(R.id.mostCountedItemTextView)
        mostCountedByTextView = findViewById(R.id.mostCountedByTextView)

        // Carrega les estadístiques
        loadStatistics()
    }

    /**
     * Carrega les dades de la col·lecció i calcula les estadístiques.
     */
    private fun loadStatistics() {
        collectionId?.let { id ->
            lifecycleScope.launch {
                // Obtenir el nom de la col·lecció
                val collectionResult = firestoreRepository.getCollectionById(id)
                collectionResult.onSuccess { collection ->
                    collectionNameStatsTextView.text = "Col·lecció: ${collection.name}"
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@StatisticsActivity, "Error en carregar nom de la col·lecció: ${exception.message}")
                    collectionNameStatsTextView.text = "Col·lecció: Desconeguda"
                }

                // Obtenir tots els ítems de la col·lecció
                val itemsResult = firestoreRepository.getItemsInCollection(id)
                itemsResult.onSuccess { items ->
                    if (items.isEmpty()) {
                        mostCountedItemTextView.text = "No hi ha ítems en aquesta col·lecció."
                        mostCountedByTextView.text = ""
                        return@onSuccess
                    }

                    // Trobar l'ítem amb el totalCount més alt
                    val mostCountedItem = items.maxByOrNull { it.totalCount }

                    if (mostCountedItem != null) {
                        mostCountedItemTextView.text = "${mostCountedItem.name} (Total: ${mostCountedItem.totalCount})"

                        // Determinar qui ha comptat principalment aquest ítem
                        val personalCounts = mostCountedItem.personalCount
                        if (personalCounts.isNotEmpty()) {
                            val mostActiveUserEntry = personalCounts.maxByOrNull { it.value } // Troba l'entrada amb el valor més alt
                            if (mostActiveUserEntry != null) {
                                val userId = mostActiveUserEntry.key
                                val count = mostActiveUserEntry.value

                                // Obtenir el nom d'usuari
                                val userResult = firestoreRepository.getUserDetails(userId)
                                userResult.onSuccess { user ->
                                    mostCountedByTextView.text = "${user.username} (${count} vegades)"
                                }.onFailure {
                                    mostCountedByTextView.text = "Usuari desconegut (${count} vegades)"
                                }
                            } else {
                                mostCountedByTextView.text = "Sense dades de comptadors personals."
                            }
                        } else {
                            mostCountedByTextView.text = "Sense dades de comptadors personals."
                        }
                    } else {
                        mostCountedItemTextView.text = "No s'ha pogut determinar l'ítem més comptat."
                        mostCountedByTextView.text = ""
                    }

                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@StatisticsActivity, "Error en carregar ítems per a estadístiques: ${exception.message}")
                    mostCountedItemTextView.text = "Error en carregar dades."
                    mostCountedByTextView.text = ""
                }
            }
        }
    }
}
