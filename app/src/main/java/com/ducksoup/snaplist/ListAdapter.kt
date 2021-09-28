package com.ducksoup.snaplist

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.list_item_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = Store.getItems()[position]
        holder.label.text = item.label
        setChecked(holder.label, item.checked)
        holder.label.setOnClickListener {
            Store.toggleChecked(item.id) {
                this.notifyDataSetChanged()
            }
        }
    }

    private fun setChecked(textView: TextView, isChecked: Boolean) {
        if (isChecked) {
            textView.setTextColor(Color.parseColor("#606060"))
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.setTextColor(Color.parseColor("#000000"))
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        }
    }

    override fun getItemCount(): Int {
        return Store.getItems().size
    }
}