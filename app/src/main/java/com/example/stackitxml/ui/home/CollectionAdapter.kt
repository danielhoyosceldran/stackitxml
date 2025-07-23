package com.example.stackitxml.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Collection
// Adaptador per al RecyclerView que mostra la llista de col·leccions.
class CollectionAdapter(
    private var collections: List<Collection>,
    private val onItemClick: (Collection) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    // ViewHolder per a cada element de la col·lecció a la llista.
    // Conté le les referències a vistes del layout item_collection.xml.
    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionNameTextView)
        val collectionDescriptionTextView: TextView = itemView.findViewById(R.id.collectionDescriptionTextView)
        val collectionMembersCountTextView: TextView = itemView.findViewById(R.id.collectionMembersCountTextView)
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
        holder.collectionMembersCountTextView.text = "Membres: ${collection.memberIds.size}"

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
}
