package com.example.stackitxml.ui.collectiondetail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Item
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.ui.statistics.StatisticsActivity // La crearem aviat
import com.example.stackitxml.util.Constants
import com.example.stackitxml.util.DialogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CollectionDetailActivity : AppCompatActivity() {

    private lateinit var collectionDetailNameTextView: TextView
    private lateinit var shareCollectionButton: ImageButton
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var addItemFab: FloatingActionButton
    private lateinit var viewStatsFab: FloatingActionButton

    private lateinit var itemAdapter: ItemAdapter
    private val firestoreRepository = FirestoreRepository()

    private var collectionId: String? = null // Per emmagatzemar l'ID de la col·lecció actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_detail)

        // Obtenir l'ID de la col·lecció de l'Intent
        collectionId = intent.getStringExtra(Constants.EXTRA_COLLECTION_ID)

        if (collectionId == null) {
            DialogUtils.showLongToast(this, "Error: No s'ha trobat l'ID de la col·lecció.")
            finish() // Tanca l'activitat si no hi ha ID
            return
        }

        // Inicialitza les vistes
        collectionDetailNameTextView = findViewById(R.id.collectionDetailNameTextView)
        shareCollectionButton = findViewById(R.id.shareCollectionButton)
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView)
        addItemFab = findViewById(R.id.addItemFab)
        viewStatsFab = findViewById(R.id.viewStatsFab)

        // Configura el RecyclerView
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        itemAdapter = ItemAdapter(emptyList(),
            onAddClick = { item -> updateItemCount(item, 1) }, // Sumar 1
            onSubtractClick = { item -> updateItemCount(item, -1) } // Restar 1
        )
        itemsRecyclerView.adapter = itemAdapter

        // Carrega els detalls de la col·lecció i els seus ítems
        loadCollectionDetails()
        loadItems()

        // Configura el botó flotant per afegir ítems
        addItemFab.setOnClickListener {
            showAddItemDialog()
        }

        // Configura el botó per compartir la col·lecció
        shareCollectionButton.setOnClickListener {
            showShareCollectionDialog()
        }

        // Configura el botó per veure estadístiques
        viewStatsFab.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java).apply {
                putExtra(Constants.EXTRA_COLLECTION_ID, collectionId)
            }
            startActivity(intent)
        }
    }

    /**
     * Carrega el nom de la col·lecció i actualitza el TextView.
     */
    private fun loadCollectionDetails() {
        collectionId?.let { id ->
            lifecycleScope.launch {
                val result = firestoreRepository.getCollectionById(id)
                result.onSuccess { collection ->
                    collectionDetailNameTextView.text = collection.name
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@CollectionDetailActivity, "Error en carregar detalls de la col·lecció: ${exception.message}")
                    finish() // Tanca si no es poden carregar els detalls
                }
            }
        }
    }

    /**
     * Carrega els ítems de la col·lecció actual.
     */
    private fun loadItems() {
        collectionId?.let { id ->
            lifecycleScope.launch {
                val result = firestoreRepository.getItemsInCollection(id)
                result.onSuccess { items ->
                    itemAdapter.updateItems(items)
                    if (items.isEmpty()) {
                        DialogUtils.showToast(this@CollectionDetailActivity, "Aquesta col·lecció no té ítems. Afegeix-ne un!")
                    }
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@CollectionDetailActivity, "Error en carregar ítems: ${exception.message}")
                }
            }
        }
    }

    /**
     * Mostra un diàleg per afegir un nou ítem a la col·lecció.
     */
    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null) // Crearem dialog_add_item.xml aviat
        val nameEditText: EditText = dialogView.findViewById(R.id.itemNameEditText)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val createButton: Button = dialogView.findViewById(R.id.createButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        createButton.setOnClickListener {
            val itemName = nameEditText.text.toString().trim()
            val currentUserId = firestoreRepository.getCurrentUserId()

            if (itemName.isEmpty()) {
                DialogUtils.showToast(this, "El nom de l'ítem no pot estar buit.")
                return@setOnClickListener
            }
            if (currentUserId == null) {
                DialogUtils.showLongToast(this, "Error: Usuari no autenticat. Torna a iniciar sessió.")
                dialog.dismiss()
                return@setOnClickListener
            }
            collectionId?.let { collId ->
                lifecycleScope.launch {
                    val newItem = Item(name = itemName, creatorId = currentUserId)
                    val result = firestoreRepository.addItemToCollection(collId, newItem)
                    result.onSuccess {
                        DialogUtils.showToast(this@CollectionDetailActivity, "Ítem '${it.name}' afegit amb èxit!")
                        dialog.dismiss()
                        loadItems() // Recarrega la llista d'ítems
                    }.onFailure { exception ->
                        DialogUtils.showLongToast(this@CollectionDetailActivity, "Error a l'afegir ítem: ${exception.message}")
                    }
                }
            }
        }
        dialog.show()
    }

    /**
     * Actualitza el comptador personal d'un ítem.
     * @param item L'ítem a actualitzar.
     * @param change La quantitat a sumar o restar (p. ex., 1 o -1).
     */
    private fun updateItemCount(item: Item, change: Long) {
        val currentUserId = firestoreRepository.getCurrentUserId()
        if (currentUserId == null) {
            DialogUtils.showLongToast(this, "Error: Usuari no autenticat. Torna a iniciar sessió.")
            return
        }
        collectionId?.let { collId ->
            lifecycleScope.launch {
                val currentCount = item.personalCount[currentUserId] ?: 0L
                val newCount = (currentCount + change).coerceAtLeast(0L) // Assegura que el comptador no sigui negatiu

                val result = firestoreRepository.updateItemCount(collId, item.itemId, currentUserId, newCount)
                result.onSuccess {
                    // No cal Toast per a cada clic, però útil per depurar
                    // DialogUtils.showToast(this@CollectionDetailActivity, "Comptador actualitzat!")
                    loadItems() // Recarrega els ítems per reflectir el canvi
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@CollectionDetailActivity, "Error a l'actualitzar comptador: ${exception.message}")
                }
            }
        }
    }

    /**
     * Mostra un diàleg per compartir la col·lecció amb un altre usuari.
     */
    private fun showShareCollectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_share_collection, null) // Crearem dialog_share_collection.xml aviat
        val emailEditText: EditText = dialogView.findViewById(R.id.shareEmailEditText) // ID per al camp de correu electrònic
        val cancelButton: Button = dialogView.findViewById(R.id.cancelShareButton) // ID per al botó de cancel·lar
        val shareButton: Button = dialogView.findViewById(R.id.shareConfirmButton) // ID per al botó de compartir

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        shareButton.setOnClickListener {
            val emailToShareWith = emailEditText.text.toString().trim()
            if (emailToShareWith.isEmpty()) {
                DialogUtils.showToast(this, "Introdueix un correu electrònic per compartir.")
                return@setOnClickListener
            }
            collectionId?.let { collId ->
                lifecycleScope.launch {
                    val result = firestoreRepository.shareCollection(collId, emailToShareWith)
                    result.onSuccess {
                        DialogUtils.showToast(this@CollectionDetailActivity, "Col·lecció compartida amb èxit!")
                        dialog.dismiss()
                        // No cal recarregar els ítems, ja que el fet de compartir no els afecta directament
                        // Però hauríem d'actualitzar el camp memberIds a la col·lecció de Firestore
                    }.onFailure { exception ->
                        DialogUtils.showLongToast(this@CollectionDetailActivity, "Error al compartir col·lecció: ${exception.message}")
                    }
                }
            }
        }
        dialog.show()
    }

    // Recarrega els ítems cada vegada que l'activitat es torna visible
    override fun onResume() {
        super.onResume()
        loadItems()
    }
}
