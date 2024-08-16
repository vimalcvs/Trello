package com.vimalcvs.trello.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.vimalcvs.trello.R
import com.vimalcvs.trello.activities.TaskListActivity
import com.vimalcvs.trello.databinding.CustomDialogBoxBinding
import com.vimalcvs.trello.databinding.ItemTaskBinding
import com.vimalcvs.trello.models.Task
import java.util.Collections

class TaskListItemAdapter(private val context: Context, private var list: ArrayList<Task>) :
    RecyclerView.Adapter<TaskListItemAdapter.ViewHolder>() {
    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    inner class ViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskListItemAdapter.ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)

        binding.root.layoutParams = layoutParams

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        with(holder) {
            if (position == list.size - 1) {
                binding.itemTaskTvAddList.visibility = View.VISIBLE
                binding.itemTaskLlMain.visibility = View.GONE
            } else {
                binding.itemTaskTvAddList.visibility = View.GONE
                binding.itemTaskLlMain.visibility = View.VISIBLE
            }


            binding.itemTaskTvAddList.setOnClickListener {
                binding.itemTaskTvAddList.visibility = View.GONE
                binding.itemTaskCvAddTaskListName.visibility = View.VISIBLE
            }


            binding.itemTaskIbCloseListName.setOnClickListener {
                removeError(binding.itemTaskTilListName)
                binding.itemTaskTvAddList.visibility = View.VISIBLE
                binding.itemTaskCvAddTaskListName.visibility = View.GONE
            }
            binding.itemTaskEtListName.doOnTextChanged { _, _, _, _ -> removeError(binding.itemTaskTilListName) }
            binding.itemTaskIbSaveListName.setOnClickListener {
                val listName = binding.itemTaskEtListName.text.toString().trim()
                if (listName.isEmpty()) {
                    binding.itemTaskTilListName.setErrorMessage("Name can not be empty.")
                } else {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                }
            }


            binding.itemTaskIbEditListName.setOnClickListener {
                binding.itemTaskEtEditListName.setText(model.title)
                binding.llTitleView.visibility = View.GONE
                binding.itemTaskCvEditTaskListName.visibility = View.VISIBLE
            }
            binding.itemTaskIbDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }
            binding.itemTaskTvTaskListTitle.text = model.title


            binding.itemTaskIbCloseEditableView.setOnClickListener {
                removeError(binding.itemTaskTilEditListName)
                binding.llTitleView.visibility = View.VISIBLE
                binding.itemTaskCvEditTaskListName.visibility = View.GONE
            }
            binding.itemTaskEtEditListName.doOnTextChanged { _, _, _, _ ->
                removeError(
                    binding.itemTaskTilEditListName
                )
            }
            binding.itemTaskIbDoneEditListName.setOnClickListener {
                val listName = binding.itemTaskEtEditListName.text.toString().trim()
                if (listName.isEmpty()) {
                    binding.itemTaskTilEditListName.setErrorMessage("Name can not be empty.")
                } else {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                }
            }


            binding.itemTaskIbCloseCardName.setOnClickListener {
                removeError(binding.itemTaskTilCardName)
                binding.itemTaskTvAddCard.visibility = View.VISIBLE
                binding.itemTaskCvAddCard.visibility = View.GONE
            }
            binding.itemTaskEtCardName.doOnTextChanged { _, _, _, _ ->
                removeError(
                    binding.itemTaskTilCardName
                )
            }
            binding.itemTaskIbDoneCardName.setOnClickListener {
                val cardName = binding.itemTaskEtCardName.text.toString().trim()
                if (cardName.isEmpty()) {
                    binding.itemTaskTilCardName.setErrorMessage("Name can not be empty.")
                } else {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                }
            }


            binding.itemTaskTvAddCard.setOnClickListener {
                binding.itemTaskTvAddCard.visibility = View.GONE
                binding.itemTaskCvAddCard.visibility = View.VISIBLE
            }


            binding.itemTaskRvCardList.setHasFixedSize(true)
            val adapter = CardListItemsAdapter(context, model.cards)
            binding.itemTaskRvCardList.adapter = adapter

            adapter.setOnClickListener(object : CardListItemsAdapter.OnItemClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity)
                        context.cardDetails(adapterPosition, cardPosition)
                }
            })

            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            binding.itemTaskRvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val dragPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition

                        if (mPositionDraggedFrom == -1) mPositionDraggedFrom = dragPosition
                        mPositionDraggedTo = targetPosition

                        Collections.swap(list[adapterPosition].cards, dragPosition, targetPosition)
                        adapter.notifyItemMoved(dragPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {
                            (context as TaskListActivity).updateCardsInTaskList(
                                adapterPosition,
                                list[adapterPosition].cards
                            )
                        }
                        mPositionDraggedFrom = -1
                        mPositionDraggedTo = -1
                    }
                }
            )
            helper.attachToRecyclerView(binding.itemTaskRvCardList)
        }
    }

    override fun getItemCount() = list.size

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun TextInputLayout.setErrorMessage(message: String) {
        error = message
        isErrorEnabled = message.isNotEmpty()
    }

    private fun removeError(itemTaskTilListName: TextInputLayout) {
        itemTaskTilListName.setErrorMessage("")
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val dialog = AlertDialog.Builder(context)
        val binding = CustomDialogBoxBinding.inflate(LayoutInflater.from(context))
        dialog.setView(binding.root)
        binding.customDialogTvMainText.text =
            context.getString(R.string.are_you_sure_you_want_to_delete, title)

        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
        binding.customDialogBtnYes.setOnClickListener {
            alertDialog.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        binding.customDialogBtnNo.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}
