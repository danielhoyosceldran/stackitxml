package com.example.stackitxml.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Collection
import com.google.firebase.auth.FirebaseAuth

// Adaptador per al RecyclerView que mostra la llista de col·leccions.
class CollectionAdapter(
    private var collections: List<Collection>,
    private var showEditCollectionsButton: Boolean,
    private val onDeleteCollectionClick: (Collection) -> Unit,
    private val onItemClick: (Collection) -> Unit,
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid

    // ViewHolder per a cada element de la col·lecció a la llista.
    // Conté le les referències a vistes del layout item_collection.xml.
    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionNameTextView)
        val collectionDescriptionTextView: TextView = itemView.findViewById(R.id.collectionDescriptionTextView)
        val collectionMembersCountTextView: TextView = itemView.findViewById(R.id.collectionMembersCountTextView)
        val collectionOwnerTextView: TextView = itemView.findViewById(R.id.collectionOwnerTextView)
        val deleteCollectionButton: ImageButton = itemView.findViewById(R.id.deleteCollectionButton)
    }

     // Crea i retorna un nou ViewHolder.
     // S'invoca quan el RecyclerView necessita un nou ViewHolder per representar un element.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        return CollectionViewHolder(view)
    }

     // Vincula les dades d'una col·lecció a un ViewHolder.
     // S'invoca per actualitzar el contingut d'un ViewHolder existent amb noves dades.
    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val collection = collections[position]
        holder.collectionNameTextView.text = collection.name
        holder.collectionDescriptionTextView.text = collection.description
        holder.collectionMembersCountTextView.text = "Members: ${collection.memberIds.size}"

         // Lògica per mostrar/ocultar el TextView de propietat de la col·lecció
         if (currentUserId != null) {
             if (collection.ownerId == currentUserId) {
                holder.collectionOwnerTextView.visibility = View.VISIBLE
             }
             holder.deleteCollectionButton.setOnClickListener {
                 onDeleteCollectionClick(collection)
             }
         } else {
             holder.collectionOwnerTextView.visibility = View.GONE
         }

         holder.deleteCollectionButton.visibility = if (showEditCollectionsButton) View.VISIBLE else View.GONE

        // Configura el listener de clic per a tot l'element de la llista
        holder.itemView.setOnClickListener {
            onItemClick(collection)
        }
    }

    override fun getItemCount(): Int {
        return collections.size
    }

    fun updateCollections(newCollections: List<Collection>) {
        this.collections = newCollections
        notifyDataSetChanged() // Notifica a l'adaptador que les dades han canviat
    }

    fun toggleDeleteCollectionVisibility(isVisible: Boolean) {
        this.showEditCollectionsButton = isVisible
        notifyDataSetChanged()
    }
}
