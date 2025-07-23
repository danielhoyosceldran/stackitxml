package com.example.stackitxml.ui.collectiondetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stackitxml.R
import com.example.stackitxml.data.model.Item
import com.google.firebase.auth.FirebaseAuth

// Adaptador per al RecyclerView que mostra la llista d'ítems d'una col·lecció.
class ItemAdapter(
    private var items: List<Item>,
    private val onAddClick: (Item) -> Unit,
    private val onSubtractClick: (Item) -> Unit,
    private var showEditCollectionButton: Boolean
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private val currentUserId =
        FirebaseAuth.getInstance().currentUser?.uid // Obté l'UID de l'usuari actual

    // ViewHolder per a cada element d'ítem a la llista.
    // Conté les referències a les vistes del layout item_collection_item.xml.
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        val personalCountTextView: TextView = itemView.findViewById(R.id.personalCountTextView)
        val subtractButton: Button = itemView.findViewById(R.id.subtractButton)
        val addButton: Button = itemView.findViewById(R.id.addButton)
        val editCollectionButton: ImageButton = itemView.findViewById(R.id.deleteItemButton)
    }

    // Crea i retorna un nou ViewHolder.
    // S'invoca quan el RecyclerView necessita un nou ViewHolder per representar un element.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_collection_item, parent, false)
        return ItemViewHolder(view)
    }

    // Vincula les dades d'un ítem a un ViewHolder.
    // S'invoca per actualitzar el contingut d'un ViewHolder existent amb noves dades.
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemNameTextView.text = item.name

        // Mostra el comptador personal de l'usuari actual
        val userCount = item.personalCount[currentUserId] ?: 0L
        holder.personalCountTextView.text = userCount.toString()

        holder.editCollectionButton.visibility = if (showEditCollectionButton) View.VISIBLE else View.GONE

        // Configura els listeners dels botons
        holder.addButton.setOnClickListener { onAddClick(item) }
        holder.subtractButton.setOnClickListener { onSubtractClick(item) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<Item>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    fun toggleNewButtonVisibility(isVisible: Boolean) {
        this.showEditCollectionButton = isVisible
        notifyDataSetChanged()
    }
}