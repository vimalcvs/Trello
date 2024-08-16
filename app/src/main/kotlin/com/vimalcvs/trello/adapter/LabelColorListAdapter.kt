package com.vimalcvs.trello.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vimalcvs.trello.databinding.ItemLabelColorBinding

class LabelColorListAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) :
    RecyclerView.Adapter<LabelColorListAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemLabelColorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLabelColorBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                if (this == "")
                    binding.itemLabelTvNoColor.visibility = View.VISIBLE
                else
                    binding.itemLabelColorView.setBackgroundColor(Color.parseColor(this))

                if (this == mSelectedColor)
                    binding.itemLabelColorImageView.visibility = View.VISIBLE

                binding.root.setOnClickListener {
                    if (onItemClickListener != null) {
                        onItemClickListener!!.onClick(position, this)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}