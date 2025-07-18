package com.example.stackitxml.ui.collectiondetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Item
import com.google.firebase.auth.FirebaseAuth // Per obtenir l'UID de l'usuari actual

/**
 * Adaptador per al RecyclerView que mostra la llista d'ítems d'una col·lecció.
 * @param items La llista inicial d'ítems a mostrar.
 * @param onAddClick Listener per gestionar els clics al botó '+' d'un ítem.
 * @param onSubtractClick Listener per gestionar els clics al botó '-' d'un ítem.
 */
class ItemAdapter(
    private var items: List<Item>,
    private val onAddClick: (Item) -> Unit,
    private val onSubtractClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Obté l'UID de l'usuari actual

    /**
     * ViewHolder per a cada element d'ítem a la llista.
     * Conté les referències a les vistes del layout item_collection_item.xml.
     */
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        val personalCountTextView: TextView = itemView.findViewById(R.id.personalCountTextView)
        val subtractButton: Button = itemView.findViewById(R.id.subtractButton)
        val addButton: Button = itemView.findViewById(R.id.addButton)
    }

    /**
     * Crea i retorna un nou ViewHolder.
     * S'invoca quan el RecyclerView necessita un nou ViewHolder per representar un element.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collection_item, parent, false)
        return ItemViewHolder(view)
    }

    /**
     * Vincula les dades d'un ítem a un ViewHolder.
     * S'invoca per actualitzar el contingut d'un ViewHolder existent amb noves dades.
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemNameTextView.text = item.name

        // Mostra el comptador personal de l'usuari actual
        val userCount = item.personalCount[currentUserId] ?: 0L
        holder.personalCountTextView.text = userCount.toString()

        // Configura els listeners dels botons
        holder.addButton.setOnClickListener { onAddClick(item) }
        holder.subtractButton.setOnClickListener { onSubtractClick(item) }
    }

    /**
     * Retorna el nombre total d'elements a la llista d'ítems.
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Actualitza la llista d'ítems i notifica al RecyclerView els canvis.
     * @param newItems La nova llista d'ítems.
     */
    fun updateItems(newItems: List<Item>) {
        this.items = newItems
        notifyDataSetChanged() // Notifica a l'adaptador que les dades han canviat
    }
}
