package com.vimalcvs.trello.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vimalcvs.trello.activities.TaskListActivity
import com.vimalcvs.trello.databinding.ItemCardsBinding
import com.vimalcvs.trello.models.Card
import com.vimalcvs.trello.models.SelectedMembers

class CardListItemsAdapter(private val context: Context, private var list: ArrayList<Card>) :
    RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemCardsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.cardItemTvCardName.text = this.name
                if (this.labelColor.isNotEmpty()) {
                    binding.cardItemViewLabelColor.visibility = View.VISIBLE
                    binding.cardItemViewLabelColor.setBackgroundColor(Color.parseColor(this.labelColor))
                }
                if ((context as TaskListActivity).mAssignedMemberList.size > 0) {
                    val selectedMemberList: ArrayList<SelectedMembers> = ArrayList()

                    for (i in context.mAssignedMemberList.indices) {
                        for (j in this.assignedTo) {
                            if (context.mAssignedMemberList[i].id == j) {
                                val selectedMember =
                                    SelectedMembers(
                                        context.mAssignedMemberList[i].id,
                                        context.mAssignedMemberList[i].image
                                    )
                                selectedMemberList.add(selectedMember)
                            }
                        }
                    }
                    if (selectedMemberList.size > 0) {
                        binding.cardItemRvSelectedMemberList.adapter =
                            CardMemberListItemAdapter(context, selectedMemberList, false)

                    }
                }
                binding.root.setOnClickListener {
                    if (onItemClickListener != null) {
                        onItemClickListener!!.onClick(position)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(cardPosition: Int)
    }
}