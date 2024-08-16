package com.vimalcvs.trello.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vimalcvs.trello.R
import com.vimalcvs.trello.databinding.ItemCardDetailMemberBinding
import com.vimalcvs.trello.models.User
import com.vimalcvs.trello.utils.Constants

class MemberListDialogItemAdapter(
    private val context: Context,
    private var list: ArrayList<User>,
    private var createdByUserId: String = ""
) :
    RecyclerView.Adapter<MemberListDialogItemAdapter.ViewHolder>() {


    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: ItemCardDetailMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCardDetailMemberBinding.inflate(LayoutInflater.from(context), parent, false)
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
                    .into(binding.itemCardDetailIv)
                binding.itemCardDetailTvMemberName.text = this.name
                binding.itemCardDetailTvEmail.text = this.email

                if (this.selected) {
                    binding.itemCardDetailIvSelectedMember.visibility = View.VISIBLE
                } else {
                    binding.itemCardDetailIvSelectedMember.visibility = View.GONE
                }
                binding.root.setOnClickListener {
                    if (onItemClickListener != null) {
                        if (this.id == createdByUserId) {

                        } else if (this.selected) {
                            this.selected = false
                            onItemClickListener!!.onClick(position, this, Constants.UN_SELECT)
                            binding.itemCardDetailIvSelectedMember.visibility = View.GONE
                        } else {
                            this.selected = true
                            onItemClickListener!!.onClick(position, this, Constants.SELECT)
                            binding.itemCardDetailIvSelectedMember.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int, user: User, isSelected: String)
    }

    fun setOnClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}