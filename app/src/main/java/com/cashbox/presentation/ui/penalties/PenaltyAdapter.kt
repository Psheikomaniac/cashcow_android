package com.cashbox.presentation.ui.penalties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cashbox.R
import com.cashbox.domain.model.Penalty
import com.google.android.material.chip.Chip

/**
 * Adapter for displaying penalties in a RecyclerView.
 */
class PenaltyAdapter(
    private val onItemClick: (Penalty) -> Unit
) : ListAdapter<Penalty, PenaltyAdapter.PenaltyViewHolder>(PenaltyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenaltyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_penalty, parent, false)
        return PenaltyViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: PenaltyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PenaltyViewHolder(
        itemView: View,
        private val onItemClick: (Penalty) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val textPenaltyType: TextView = itemView.findViewById(R.id.text_penalty_type)
        private val textPenaltyAmount: TextView = itemView.findViewById(R.id.text_penalty_amount)
        private val textPenaltyReason: TextView = itemView.findViewById(R.id.text_penalty_reason)
        private val textPenaltyUser: TextView = itemView.findViewById(R.id.text_penalty_user)
        private val chipPenaltyStatus: Chip = itemView.findViewById(R.id.chip_penalty_status)

        fun bind(penalty: Penalty) {
            textPenaltyType.text = penalty.type.name
            textPenaltyAmount.text = penalty.amount.toCurrency()
            textPenaltyReason.text = penalty.reason
            textPenaltyUser.text = penalty.teamUser.name
            
            val context = itemView.context
            if (penalty.isPaid) {
                chipPenaltyStatus.text = context.getString(R.string.paid)
                chipPenaltyStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
            } else {
                chipPenaltyStatus.text = context.getString(R.string.unpaid)
                chipPenaltyStatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
            }
            
            itemView.setOnClickListener { onItemClick(penalty) }
        }
    }

    class PenaltyDiffCallback : DiffUtil.ItemCallback<Penalty>() {
        override fun areItemsTheSame(oldItem: Penalty, newItem: Penalty): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Penalty, newItem: Penalty): Boolean {
            return oldItem == newItem
        }
    }
}