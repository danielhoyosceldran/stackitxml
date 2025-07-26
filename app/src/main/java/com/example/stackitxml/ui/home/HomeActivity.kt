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

class HomeActivity : AppCompatActivity() {

    private lateinit var collectionsRecyclerView: RecyclerView
    private lateinit var addCollectionFab: FloatingActionButton
    private lateinit var homeTitleTextView: TextView
    private lateinit var logoutButton: ImageButton
    private lateinit var editCollectionsButton: ImageButton

    private lateinit var collectionAdapter: CollectionAdapter
    private val firestoreRepository = FirestoreRepository()

    private var isEditCollectionsButtonVisible = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home) // Enllaça la view XML a aquest fitxer

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
            DialogUtils.showToast(this, "Sessió tancada correctament.")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Carrega les col·leccions en iniciar l'activitat
        loadCollections()
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
                DialogUtils.showToast(this, "El nom de la col·lecció no pot estar buit.")
                return@setOnClickListener
            }

            if (currentUserId == null) {
                DialogUtils.showLongToast(this, "Error: Usuari no autenticat. Torna a iniciar sessió.")
                dialog.dismiss()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = firestoreRepository.createCollection(name, description, currentUserId)
                result.onSuccess {
                    DialogUtils.showToast(this@HomeActivity, "Col·lecció '${it.name}' creada amb èxit!")
                    dialog.dismiss()
                    loadCollections()
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@HomeActivity, "Error al crear col·lecció: ${exception.message}")
                }
            }
        }

        dialog.show()
    }

    private fun loadCollections() {
        val currentUserId = firestoreRepository.getCurrentUserId()
        if (currentUserId == null) {
            DialogUtils.showLongToast(this, "Error: Usuari no autenticat. Torna a iniciar sessió.")
            return
        }

        lifecycleScope.launch {
            val result = firestoreRepository.getCollectionsForUser(currentUserId)
            result.onSuccess { collections ->
                if (collections.isEmpty()) {
                    DialogUtils.showToast(this@HomeActivity, "No tens col·leccions. Crea una!")
                }
                collectionAdapter.updateCollections(collections)
            }.onFailure { exception ->
                DialogUtils.showLongToast(this@HomeActivity, "Error en carregar col·leccions: ${exception.message}")
            }
        }
    }

    // Mostra un diàleg de confirmació abans d'eliminar una col·lecció.
    private fun showConfirmDeleteCollectionDialog(collection: Collection) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminació")
            .setMessage("Estàs segur que vols eliminar la col·lecció '${collection.name}'? Això eliminarà tots els seus ítems i la desvincularà de tots els usuaris.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                deleteCollection(collection.collectionId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel·lar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Elimina una col·lecció de la base de dades.
    private fun deleteCollection(collectionId: String) {
        lifecycleScope.launch {
            val result = firestoreRepository.deleteCollection(collectionId)
            result.onSuccess {
                DialogUtils.showToast(this@HomeActivity, "Col·lecció eliminada amb èxit!")
                loadCollections() // Recarrega la llista de col·leccions
            }.onFailure { exception ->
                DialogUtils.showLongToast(this@HomeActivity, "Error a l'eliminar col·lecció: ${exception.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCollections()
    }
}
