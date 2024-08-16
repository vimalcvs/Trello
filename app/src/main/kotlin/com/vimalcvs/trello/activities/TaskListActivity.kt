package com.vimalcvs.trello.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.vimalcvs.trello.R
import com.vimalcvs.trello.adapter.TaskListItemAdapter
import com.vimalcvs.trello.databinding.ActivityTaskListBinding
import com.vimalcvs.trello.firebase.FireStoreClass
import com.vimalcvs.trello.models.Board
import com.vimalcvs.trello.models.Card
import com.vimalcvs.trello.models.Task
import com.vimalcvs.trello.models.User
import com.vimalcvs.trello.utils.Constants


class TaskListActivity : BaseActivity() {
    lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mDocumentId: String
    lateinit var mAssignedMemberList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        if (mDocumentId.isNotEmpty()) {
            showProgressDialog(resources.getString(R.string.progress_please_wait))
            FireStoreClass().getBoardDetails(this, mDocumentId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARS_DETAIL, mBoardDetails)
                resultLauncher.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK)
                FireStoreClass().getBoardDetails(this, mDocumentId)
            }

        }

    fun boardDetails(board: Board) {
        mBoardDetails = board
        setupActionbar()

        FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.taskListToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.taskListToolBar.title = mBoardDetails.name
        binding.taskListToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess() {
        FireStoreClass().getBoardDetails(this, mDocumentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        mBoardDetails.taskList.add(task)
        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy, model.cards)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val currentUser = FireStoreClass().getCurrentUserId()

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(currentUser)

        val card = Card(cardName, currentUser, cardAssignedUsersList)

        val cardList = mBoardDetails.taskList[position].cards

        cardList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList
        )
        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun boardMembersDetailsList(list: ArrayList<User>) {
        hideProgressDialog()
        mAssignedMemberList = list

        val addTaskList = Task(resources.getString(R.string.item_task_add_List))
        mBoardDetails.taskList.add(addTaskList)
        binding.rvTaskList.setHasFixedSize(true)
        val adapter = TaskListItemAdapter(this, mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter
    }

    fun cardDetails(taskListPosition: Int, cardListPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARS_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.BOARS_MEMBER_LIST, mAssignedMemberList)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardListPosition)
        resultLauncher.launch(intent)
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog(resources.getString(R.string.progress_please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }
}