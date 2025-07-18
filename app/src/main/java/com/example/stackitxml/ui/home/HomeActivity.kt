package com.example.stackitxml.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog // Importa per a AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Collection
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.ui.collectiondetail.CollectionDetailActivity // La crearem aviat
import com.example.stackitxml.util.Constants
import com.example.stackitxml.util.DialogUtils // Importa la utilitat de diàlegs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var collectionsRecyclerView: RecyclerView
    private lateinit var addCollectionFab: FloatingActionButton
    private lateinit var homeTitleTextView: TextView

    private lateinit var collectionAdapter: CollectionAdapter
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicialitza les vistes
        collectionsRecyclerView = findViewById(R.id.collectionsRecyclerView)
        addCollectionFab = findViewById(R.id.addCollectionFab)
        homeTitleTextView = findViewById(R.id.homeTitleTextView)

        // Configura el RecyclerView
        collectionsRecyclerView.layoutManager = LinearLayoutManager(this)
        collectionAdapter = CollectionAdapter(emptyList()) { collection ->
            // Gestionar el clic en una col·lecció (anar a CollectionDetailActivity)
            val intent = Intent(this, CollectionDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_COLLECTION_ID, collection.collectionId)
            }
            startActivity(intent)
        }
        collectionsRecyclerView.adapter = collectionAdapter

        // Configura el botó flotant per afegir col·leccions
        addCollectionFab.setOnClickListener {
            showAddCollectionDialog()
        }

        // Carrega les col·leccions en iniciar l'activitat
        loadCollections()
    }

    /**
     * Mostra un diàleg per afegir una nova col·lecció.
     */
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
            dialog.dismiss() // Tanca el diàleg
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
                // Opcional: redirigir a LoginActivity
                // startActivity(Intent(this, LoginActivity::class.java))
                // finish()
                return@setOnClickListener
            }

            // Crida al repositori per crear la col·lecció
            lifecycleScope.launch {
                val result = firestoreRepository.createCollection(name, description, currentUserId)
                result.onSuccess {
                    DialogUtils.showToast(this@HomeActivity, "Col·lecció '${it.name}' creada amb èxit!")
                    dialog.dismiss()
                    loadCollections() // Recarrega la llista de col·leccions
                }.onFailure { exception ->
                    DialogUtils.showLongToast(this@HomeActivity, "Error al crear col·lecció: ${exception.message}")
                }
            }
        }

        dialog.show() // Mostra el diàleg
    }

    /**
     * Carrega les col·leccions a les quals l'usuari actual té accés.
     */
    private fun loadCollections() {
        val currentUserId = firestoreRepository.getCurrentUserId()
        if (currentUserId == null) {
            DialogUtils.showLongToast(this, "Error: Usuari no autenticat. Torna a iniciar sessió.")
            // Opcional: redirigir a LoginActivity si no hi ha usuari
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
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

    // Recarrega les col·leccions cada vegada que l'activitat es torna visible
    override fun onResume() {
        super.onResume()
        loadCollections()
    }
}
