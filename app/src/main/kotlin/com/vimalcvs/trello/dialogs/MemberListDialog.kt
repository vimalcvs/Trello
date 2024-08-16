package com.vimalcvs.trello.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.vimalcvs.trello.adapter.MemberListDialogItemAdapter
import com.vimalcvs.trello.databinding.DialogListBinding
import com.vimalcvs.trello.models.User

abstract class MemberListDialog(
    private val context: Context,
    private var list: ArrayList<User>,
    private val title: String = "",
    private var createdByUserId: String = ""
) : Dialog(context) {

    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogListBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setUpRecyclerview()
    }

    private fun setUpRecyclerview() {
        binding.dialogListTvTitle.text = title
        val adapter = MemberListDialogItemAdapter(context, list, createdByUserId)
        binding.dialogRvList.adapter = adapter
        adapter.setOnClickListener(object : MemberListDialogItemAdapter.OnItemClickListener {
            override fun onClick(position: Int, user: User, isSelected: String) {
                onItemSelected(user, isSelected)
            }
        })
        binding.dialogSelectBtnDone.setOnClickListener {
            dismiss()
        }
    }

    protected abstract fun onItemSelected(user: User, isSelected: String)
}