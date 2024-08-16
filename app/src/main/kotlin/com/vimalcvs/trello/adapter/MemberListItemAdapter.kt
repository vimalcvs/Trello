package com.vimalcvs.trello.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vimalcvs.trello.R
import com.vimalcvs.trello.databinding.ItemMemberBinding
import com.vimalcvs.trello.models.User

class MemberListItemAdapter(
    private val context: Context,
    private var list: ArrayList<User>,
    private var createdById: String,
    private var currentUserId: String
) :
    RecyclerView.Adapter<MemberListItemAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false)
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
                    .placeholder(R.drawable.img_user_placeholder)
                    .into(binding.itemMemberIv)
                if (this.id == createdById || currentUserId != createdById) binding.memberItemIvRemoveMember.visibility =
                    View.GONE
                binding.itemBoardTvMemberName.text = this.name
                binding.itemMemberTvEmail.text = this.email
                binding.memberItemIvRemoveMember.setOnClickListener {
                    onItemClickListener?.onClick(position, this)
                }
            }
        }
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, user: User)
    }
}