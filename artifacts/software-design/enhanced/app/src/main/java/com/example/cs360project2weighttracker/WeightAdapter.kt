package com.example.cs360project2weighttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeightAdapter(
    private var items: List<WeightEntry>,
    private val onClick: (WeightEntry) -> Unit,
    private val onLongClick: (WeightEntry) -> Unit
) : RecyclerView.Adapter<WeightAdapter.WeightVH>() {

    fun submit(newItems: List<WeightEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight_tile, parent, false)
        return WeightVH(v)
    }

    override fun onBindViewHolder(holder: WeightVH, position: Int) {
        val item = items[position]
        holder.bind(item, onClick, onLongClick)
    }

    override fun getItemCount(): Int = items.size

    class WeightVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvWeight: TextView = itemView.findViewById(R.id.tvWeight)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)

        fun bind(
            item: WeightEntry,
            onClick: (WeightEntry) -> Unit,
            onLongClick: (WeightEntry) -> Unit
        ) {
            tvDate.text = item.date
            tvWeight.text = "${item.weight} lbs"
            tvNotes.text = item.notes ?: ""

            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}