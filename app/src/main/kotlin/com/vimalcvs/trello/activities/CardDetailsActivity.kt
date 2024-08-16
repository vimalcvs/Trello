package com.vimalcvs.trello.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.vimalcvs.trello.R
import com.vimalcvs.trello.adapter.CardMemberListItemAdapter
import com.vimalcvs.trello.databinding.ActivityCardDetailsBinding
import com.vimalcvs.trello.databinding.CustomDialogBoxBinding
import com.vimalcvs.trello.dialogs.LabelColorListDialog
import com.vimalcvs.trello.dialogs.MemberListDialog
import com.vimalcvs.trello.firebase.FireStoreClass
import com.vimalcvs.trello.models.Board
import com.vimalcvs.trello.models.Card
import com.vimalcvs.trello.models.SelectedMembers
import com.vimalcvs.trello.models.Task
import com.vimalcvs.trello.models.User
import com.vimalcvs.trello.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor: String = ""
    private var mMembersDetailList: ArrayList<User> = ArrayList()
    private var mSelectedDueDateMilliSeconds: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        setupActionbar()

        binding.cardDetailEtCardName.doOnTextChanged { _, _, _, _ -> removeError() }
        binding.cardDetailEtCardName.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        mSelectedColor =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        setColor()

        binding.cardDetailBtnUpdate.setOnClickListener {
            if (validateCardName()) {
                updateCardDetails()
            }
        }
        binding.cardDetailTvSelectColor.setOnClickListener {
            labelColorListDialog()
        }
        mSelectedDueDateMilliSeconds =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate
        if (mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val theDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding.cardDetailTvSelectDueDate.text = theDate
        }
        setupSelectedMembersList()
        binding.cardDetailTvSelectDueDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun labelColorListDialog() {
        val colorList = colorList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorList,
            resources.getString(R.string.select_label_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setColor() {
        if (mSelectedColor.isNotBlank()) {
            binding.cardDetailTvSelectColor.text = ""
            binding.cardDetailTvSelectColor.setBackgroundColor(Color.parseColor(mSelectedColor))
        } else {
            binding.cardDetailTvSelectColor.text = resources.getString(R.string.select_label_color)
            binding.cardDetailTvSelectColor.setBackgroundResource(android.R.color.transparent)

        }
    }

    private fun removeError() {
        binding.cardDetailTilCardName.setErrorMessage("")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARS_DETAIL)) mBoardDetails =
            intent.getParcelableExtra(Constants.BOARS_DETAIL)!!

        if (intent.hasExtra(Constants.BOARS_MEMBER_LIST)) {
            mMembersDetailList =
                intent.getParcelableArrayListExtra(Constants.BOARS_MEMBER_LIST)!!
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) mTaskListPosition =
            intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) mCardListPosition =
            intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.cardDetailsToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cardDetailsToolBar.title =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        binding.cardDetailsToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun membersListDialog() {
        val cardAssignedMemberList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMemberList) {
                if (mMembersDetailList[i].id == j) {
                    mMembersDetailList[i].selected = true
                }
            }
        }
        val listDialog = object : MemberListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.select_member),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy
        ) {
            override fun onItemSelected(user: User, isSelected: String) {
                if (isSelected == Constants.SELECT) {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(
                        user.id
                    )

                } else if (isSelected == Constants.UN_SELECT) {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(
                        user.id
                    )
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun updateCardDetails() {
        val card = Card(
            binding.cardDetailEtCardName.text.toString().trim(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard() {
        val cardList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardList.removeAt(mCardListPosition)
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun validateCardName(): Boolean {
        val isCardBlank = binding.cardDetailEtCardName.text.toString().trim().isBlank()
        return if (isCardBlank) {
            binding.cardDetailTilCardName.setErrorMessage("Card name can not be empty.")
            false
        } else true
    }

    private fun alertDialogForDeleteCard(title: String) {
        val dialog = AlertDialog.Builder(this)
        val binding = CustomDialogBoxBinding.inflate(LayoutInflater.from(this))
        dialog.setView(binding.root)
        binding.customDialogTvMainText.text =
            resources.getString(R.string.are_you_sure_you_want_to_delete, title)

        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
        binding.customDialogBtnYes.setOnClickListener {
            alertDialog.dismiss()
            deleteCard()

        }
        binding.customDialogBtnNo.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun colorList(): ArrayList<String> {
        val colorList: ArrayList<String> = ArrayList()
        colorList.add("")
        colorList.add("#D2E0FB")
        colorList.add("#FEFFAC")
        colorList.add("#A8DF8E")
        colorList.add("#FFBFBF")
        colorList.add("#45FFCA")
        colorList.add("#e7bc91")
        colorList.add("#BEADFA")
        colorList.add("#D67BFF")
        colorList.add("#989898")

        return colorList
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMemberList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMemberList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMemberList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember =
                        SelectedMembers(mMembersDetailList[i].id, mMembersDetailList[i].image)
                    selectedMemberList.add(selectedMember)
                }
            }
        }
        selectedMemberList.add(SelectedMembers("", ""))

        val adapter = CardMemberListItemAdapter(this, selectedMemberList, true)
        binding.cardDetailRvSelectedMember.adapter = adapter
        binding.cardDetailRvSelectedMember.layoutManager =
            GridLayoutManager(this, calculateNumberOfSpan())
        adapter.setOnClickListener(object : CardMemberListItemAdapter.OnItemClickListener {
            override fun onClick() {
                membersListDialog()
            }
        })
    }

    private fun calculateNumberOfSpan(): Int {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        val numberOfCols = (dpWidth - 64) / 55
        return numberOfCols.toInt()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this,
            { _, mYear, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else monthOfYear
                val selectDate = "$sDayOfMonth/$sMonthOfYear/$mYear"
                binding.cardDetailTvSelectDueDate.text = selectDate
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }
}