package com.loyalt.loyaltclient.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loyalt.loyaltclient.databinding.ItemPartnerBinding
import com.loyalt.loyaltclient.models.LoyaltyType
import com.loyalt.loyaltclient.models.Partner

class PartnerAdapter(
    private val partners: List<Partner>,
    private val onClick: (Partner) -> Unit
) : RecyclerView.Adapter<PartnerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPartnerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(partner: Partner) {
            binding.tvPartnerName.text = partner.partnerId
            if (partner.loyaltyType == LoyaltyType.STAMP_CARD) {
                binding.tvLoyaltyInfo.text = "Собрано ${partner.currentValueLoyalty} из ${partner.maxValueOrPercent} шт"
            } else {
                binding.tvLoyaltyInfo.text = "Кэшбек ${partner.maxValueOrPercent}%"
            }

            binding.root.setOnClickListener { onClick(partner) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(partners[position])
    }

    override fun getItemCount() = partners.size
}