package com.vimalcvs.trello.firebase

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.vimalcvs.trello.R
import com.vimalcvs.trello.activities.CardDetailsActivity
import com.vimalcvs.trello.activities.CreateBoardActivity
import com.vimalcvs.trello.activities.MainActivity
import com.vimalcvs.trello.activities.MembersActivity
import com.vimalcvs.trello.activities.MyProfileActivity
import com.vimalcvs.trello.activities.SignInActivity
import com.vimalcvs.trello.activities.SignUpActivity
import com.vimalcvs.trello.activities.TaskListActivity
import com.vimalcvs.trello.models.Board
import com.vimalcvs.trello.models.User
import com.vimalcvs.trello.utils.Constants

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.signUpSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Registration Failure!!!.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsToUI(boardList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.binding.mainIncludeAppBar.mainContentRvBoards.visibility =
                    View.GONE
                activity.binding.mainIncludeAppBar.mainContentTvNoBoards.visibility =
                    View.VISIBLE
                activity.binding.mainIncludeAppBar.mainContentTvNoBoards.text =
                    activity.getString(R.string.failed_to_load_boards)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessFully()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Some error occurred.", Toast.LENGTH_SHORT).show()

            }

    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun updateUserData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is MainActivity ->
                        activity.tokenUpdateSuccess()

                    is MyProfileActivity ->
                        activity.profileUpdateSuccess()
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is MainActivity ->
                        activity.hideProgressDialog()

                    is MyProfileActivity ->
                        activity.hideProgressDialog()
                }
                Toast.makeText(activity, R.string.error_in_profile_update, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { documentSnapShot ->
                val loggedInUser = documentSnapShot.toObject(User::class.java)
                if (loggedInUser != null)
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess()
                        }

                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }

                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if (activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener {
                if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if (activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Toast.makeText(activity, "Task update Failure!!!.", Toast.LENGTH_SHORT).show()
            }

    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "getting boards Failure!!!.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                val userList: ArrayList<User> = ArrayList()
                for (i in document.documents) {
                    val user = i.toObject((User::class.java))!!
                    userList.add(user)
                }
                if (activity is MembersActivity)
                    activity.setUpMemberList(userList)
                else if (activity is TaskListActivity)
                    activity.boardMembersDetailsList(userList)
            }
            .addOnFailureListener {
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                Toast.makeText(activity, "Failed to load members", Toast.LENGTH_SHORT).show()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Failed to load user details.", Toast.LENGTH_SHORT).show()
            }
    }

    fun assignedMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Failed to updating board details.", Toast.LENGTH_SHORT)
                    .show()

            }
    }

    fun removeMemberFromBoard(activity: MembersActivity, board: Board, position: Int, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        board.assignedTo.remove(user.id)
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberRemovedSuccess(position, user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(
                    activity,
                    "Failed to remove member from the board.",
                    Toast.LENGTH_SHORT
                )
                    .show()

            }
    }
}