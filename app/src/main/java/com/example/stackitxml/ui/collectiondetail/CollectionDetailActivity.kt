package com.example.stackitxml.ui.collectiondetail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Item
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.ui.statistics.StatisticsActivity
import com.example.stackitxml.util.Constants
import com.example.stackitxml.util.DialogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CollectionDetailActivity : AppCompatActivity() {
    private lateinit var collectionDetailNameTextView: TextView
    private lateinit var shareCollectionButton: ImageButton
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var addItemFab: FloatingActionButton
    private lateinit var viewStatsFab: Button
    private lateinit var collectionDescriptionTextView: TextView

    private lateinit var itemAdapter: ItemAdapter
    private val firestoreRepository = FirestoreRepository()

    private var collectionId: String? = null // ID de la col·lecció actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_detail)
        enableEdgeToEdge()

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
        collectionDescriptionTextView = findViewById(R.id.collectionDescriptionTextView)

        // Setup de la descripció
        collectionDescriptionTextView.text = getString(R.string.collection_description_placeholder)

        // Configura el RecyclerView, igual que a Home
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

    // Carrega el nom de la col·lecció i actualitza el TextView.
    private fun loadCollectionDetails() {
        collectionId?.let { id ->
            lifecycleScope.launch {
                val result = firestoreRepository.getCollectionById(id)
                result.onSuccess { collection ->
                    collectionDetailNameTextView.text = collection.name
                    collectionDescriptionTextView.text = collection.description
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@CollectionDetailActivity, "Error en carregar detalls de la col·lecció: ${exception.message}")
                    finish() // Tanca si no es poden carregar els detalls
                }
            }
        }
    }

    // Carrega els ítems de la col·lecció actual.
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

    private fun showAddItemDialog() {
        // carregar components UI
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
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
                DialogUtils.showToast(this, "The item name can't be empty")
                return@setOnClickListener
            }
            if (currentUserId == null) {
                DialogUtils.showLongToast(this, getString(R.string.session_expired))
                dialog.dismiss()
                return@setOnClickListener
            }
            collectionId?.let { collId ->
                lifecycleScope.launch {
                    val newItem = Item(name = itemName, creatorId = currentUserId)
                    val result = firestoreRepository.addItemToCollection(collId, newItem)
                    result.onSuccess {
                        DialogUtils.showToast(this@CollectionDetailActivity, "Item '${it.name}' added succesfuly")
                        dialog.dismiss()
                        loadItems() // Recarrega la llista d'ítems
                    }.onFailure { exception ->
                        DialogUtils.showLongToast(this@CollectionDetailActivity, "Error adding item: ${exception.message}")
                    }
                }
            }
        }
        dialog.show()
    }

    private fun updateItemCount(item: Item, change: Long) {
        val currentUserId = firestoreRepository.getCurrentUserId()
        if (currentUserId == null) {
            DialogUtils.showLongToast(this, getString(R.string.session_expired))
            return
        }
        collectionId?.let { collId ->
            lifecycleScope.launch {
                val currentCount = item.personalCount[currentUserId] ?: 0L // 0L assegura que el 0 és de tipus Long
                val newCount = (currentCount + change).coerceAtLeast(0L)

                val result = firestoreRepository.updateItemCount(collId, item.itemId, currentUserId, newCount)
                result.onSuccess {
                    loadItems() // todo: revisar si cal carregar tot l'element o podem sumar directament el nou comptador
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@CollectionDetailActivity, "Error a l'actualitzar comptador: ${exception.message}")
                }
            }
        }
    }

    private fun showShareCollectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_share_collection, null)
        val emailEditText: EditText = dialogView.findViewById(R.id.shareEmailEditText)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelShareButton)
        val shareButton: Button = dialogView.findViewById(R.id.shareConfirmButton)

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
