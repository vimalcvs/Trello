package com.vimalcvs.trello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vimalcvs.trello.R
import com.vimalcvs.trello.databinding.ItemCardSelectedMemberBinding
import com.vimalcvs.trello.models.SelectedMembers

class CardMemberListItemAdapter(
    private val context: Context,
    private val list: ArrayList<SelectedMembers>,
    private val isAddMember: Boolean
) : RecyclerView.Adapter<CardMemberListItemAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private lateinit var binding: ItemCardSelectedMemberBinding

    inner class ViewHolder(val binding: ItemCardSelectedMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                if (!isAddMember) {
                    binding.itemCardSelectedMemberCv.visibility = View.VISIBLE
                    binding.itemCardSelectedMemberCvUser.visibility = View.GONE
                    Glide
                        .with(context)
                        .load(this.image)
                        .placeholder(R.drawable.img_user_placeholder)
                        .centerCrop()
                        .into(binding.itemCardSelectedMemberCv)
                } else {
                    if (position == list.size - 1) {
                        binding.itemCardSelectedMemberIbAddUser.visibility = View.VISIBLE
                        binding.itemCardSelectedMemberCvUser.visibility = View.GONE
                    } else {
                        Glide
                            .with(context)
                            .load(this.image)
                            .placeholder(R.drawable.img_user_placeholder)
                            .centerCrop()
                            .into(binding.itemCardSelectedMemberCvUser)
                    }
                }
            }
            binding.itemCardSelectedMemberIbAddUser.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick()
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick()
    }
}