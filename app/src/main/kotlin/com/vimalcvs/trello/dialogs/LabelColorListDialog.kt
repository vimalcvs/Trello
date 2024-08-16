package com.vimalcvs.trello.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.vimalcvs.trello.adapter.LabelColorListAdapter
import com.vimalcvs.trello.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogListBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setUpRecyclerview()
    }

    private fun setUpRecyclerview() {
        binding.dialogListTvTitle.text = title
        binding.dialogSelectBtnDone.visibility = View.GONE
        val adapter = LabelColorListAdapter(context, list, mSelectedColor)
        binding.dialogRvList.adapter = adapter
        adapter.setOnClickListener(object : LabelColorListAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        })
    }

    protected abstract fun onItemSelected(color: String)
}