package com.example.stackitxml.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.ui.auth.LoginActivity
import com.example.stackitxml.data.model.Collection
import com.example.stackitxml.ui.collectiondetail.CollectionDetailActivity
import com.example.stackitxml.util.Constants
import com.example.stackitxml.util.DialogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration

class HomeActivity : AppCompatActivity() {

    private lateinit var collectionsRecyclerView: RecyclerView
    private lateinit var addCollectionFab: FloatingActionButton
    private lateinit var homeTitleTextView: TextView
    private lateinit var logoutButton: ImageButton
    private lateinit var editCollectionsButton: ImageButton

    private lateinit var collectionAdapter: CollectionAdapter
    private val firestoreRepository = FirestoreRepository()

    private var isEditCollectionsButtonVisible = false

    private var collectionsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Aplica els insets a la vista arrel per evitar superposició amb barres i elements del mobil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Carreguem els elements de la UI
        collectionsRecyclerView = findViewById(R.id.collectionsRecyclerView)
        addCollectionFab = findViewById(R.id.addCollectionFab)
        homeTitleTextView = findViewById(R.id.homeTitleTextView)
        logoutButton = findViewById(R.id.logoutButton)
        editCollectionsButton = findViewById(R.id.editCollectionsButton)

        // Configura el RecyclerView (la llista de col·leccions)
        collectionsRecyclerView.layoutManager = LinearLayoutManager(this)
        // Inicialitzem el collectionAdapter buit, fins que omplim amb dades de la BBDD
        // collection adapter és, com diu el nom un adabtador. Ho tinc apuntat a la docu
        collectionAdapter = CollectionAdapter(
            emptyList(),
            onItemClick = { collection ->
                val intent = Intent(this, CollectionDetailActivity::class.java).apply {
                    putExtra(Constants.EXTRA_COLLECTION_ID, collection.collectionId)
                }
                startActivity(intent)
            },
            showEditCollectionsButton = false,
            onDeleteCollectionClick = { collection -> showConfirmDeleteCollectionDialog(collection) }
        )
        collectionsRecyclerView.adapter = collectionAdapter

        // Configura el botó flotant per afegir col·leccions
        addCollectionFab.setOnClickListener {
            showAddCollectionDialog()
        }

        // configura el listener per al botó d'eliminar col·lecció
        editCollectionsButton.setOnClickListener {
            isEditCollectionsButtonVisible = !isEditCollectionsButtonVisible
            collectionAdapter.toggleDeleteCollectionVisibility(isEditCollectionsButtonVisible)
            DialogUtils.showToast(this, "Edit Collection mode: ${if (isEditCollectionsButtonVisible) "ON" else "OFF"}")
        }

        // Configura el listener per al botó de Log Out
        logoutButton.setOnClickListener {
            firestoreRepository.signOut()
            DialogUtils.showToast(this, "Session closed successfully.")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showAddCollectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_collection, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.collectionNameEditText)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.collectionDescriptionEditText)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val createButton: Button = dialogView.findViewById(R.id.createButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        createButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val currentUserId = firestoreRepository.getCurrentUserId()

            if (name.isEmpty()) {
                DialogUtils.showToast(this, "The collection name cannot be empty.")
                return@setOnClickListener
            }

            if (currentUserId == null) {
                DialogUtils.showLongToast(this, "Error: User not authenticated. Please log in again.")
                dialog.dismiss()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = firestoreRepository.createCollection(name, description, currentUserId)
                result.onSuccess {
                    DialogUtils.showToast(this@HomeActivity, "Collection '${it.name}' created successfully!")
                    dialog.dismiss()
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@HomeActivity, "Error creating collection: ${exception.message}")
                }
            }
        }

        dialog.show()
    }

    private fun loadCollections() {
        val currentUserId = firestoreRepository.getCurrentUserId()
        if (currentUserId == null) {
            DialogUtils.showLongToast(this, "Error: User not authenticated. Please log in again.")
            return
        }

        collectionsListener?.remove()

        collectionsListener = firestoreRepository.getCollectionsForUserRealtime(currentUserId) { result ->
            result.onSuccess { collections ->
                if (collections.isEmpty()) {
                    DialogUtils.showToast(this@HomeActivity, "You have no collections. Create one!")
                }
                collectionAdapter.updateCollections(collections)
            }.onFailure { exception ->
                DialogUtils.showLongToast(this@HomeActivity, "Error loading collections: ${exception.message}")
            }
        }
    }

    // Mostra un diàleg de confirmació abans d'eliminar una col·lecció.
    private fun showConfirmDeleteCollectionDialog(collection: Collection) {
        val currentUserId = firestoreRepository.getCurrentUserId()

        val isOwner = collection.ownerId == currentUserId
        val dialogTitle: String
        val dialogMessage: String

        dialogTitle = getString(R.string.delete_confirmation)
        if (isOwner) {
            dialogMessage = getString(R.string.delete_confirmation_message) + " collection `${collection.name}`? " + getString(R.string.owner_collection_delete_description)
        } else {
            dialogMessage = "Are you sure you want to remove from your collections the collection '${collection.name}'?"
        }

        // Inflar el diàleg genèric
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null)
        val titleTextView: TextView = dialogView.findViewById(R.id.dialogTitleTextView)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialogMessageTextView)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val confirmButton: Button = dialogView.findViewById(R.id.confirmButton)

        titleTextView.text = dialogTitle
        messageTextView.text = dialogMessage

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        confirmButton.setOnClickListener {
            deleteCollection(collection.collectionId)
            dialog.dismiss()
        }

        dialog.show()
    }

    // Elimina una col·lecció de la base de dades.
    private fun deleteCollection(collectionId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.deleteCollection(collectionId)
            result.onSuccess {
                DialogUtils.showToast(this@HomeActivity, "Collection deleted successfully!")
            }.onFailure { exception ->
                DialogUtils.showLongToast(this@HomeActivity, "Error deleting collection: ${exception.message}")
            }
        }
    }

    // Inicia l'escoltador de Firestore quan l'activitat es fa visible
    override fun onStart() {
        super.onStart()
        loadCollections()
    }

    override fun onStop() {
        super.onStop()
        collectionsListener?.remove()
    }
}
