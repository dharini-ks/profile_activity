package com.example.sample

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private var items: List<ProfileItem>) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
        val value: TextView = itemView.findViewById(R.id.value)
        val divider: View = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_row, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.value.text = item.value

        // Highlight "REFRESH AVAILABLE" in green if present in label
        val keyword = "REFRESH AVAILABLE"
        if (item.label.contains(keyword, ignoreCase = true)) {
            val spannable = SpannableString(item.label)
            val startIndex = item.label.indexOf(keyword)
            if (startIndex != -1) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00FF00")), // Green color
                    startIndex,
                    startIndex + keyword.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            holder.label.text = spannable
        } else {
            holder.label.text = item.label
        }

        // Show divider only below specific labels
        if (item.label.contains("Credit Score", ignoreCase = true) ||
            item.label.contains("Lifetime Cashback", ignoreCase = true)) {
            holder.divider.visibility = View.VISIBLE
        } else {
            holder.divider.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ProfileItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
