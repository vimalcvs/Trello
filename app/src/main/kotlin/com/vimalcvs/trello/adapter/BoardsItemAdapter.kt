package com.vimalcvs.trello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vimalcvs.trello.R
import com.vimalcvs.trello.databinding.ItemBoardsBinding
import com.vimalcvs.trello.models.Board

class BoardsItemAdapter(private val context: Context, private val list: ArrayList<Board>) :
    RecyclerView.Adapter<BoardsItemAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemBoardsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BoardsItemAdapter.ViewHolder {
        val binding = ItemBoardsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                Glide
                    .with(context)
                    .load(this.image)
                    .centerCrop()
                    .placeholder(R.drawable.img_employee_manages)
                    .into(binding.itemBoardIv)
                binding.itemBoardTvBoardName.text = this.name
                binding.itemBoardTvCreatedBy.text =
                    context.getString(R.string.created_by, this.createdBy)
                binding.root.setOnClickListener {
                    onItemClickListener?.onClick(position, list[position])
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, model: Board)
    }
}
