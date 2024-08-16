package com.vimalcvs.trello.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vimalcvs.trello.R
import com.vimalcvs.trello.databinding.ActivityCreateBoardBinding
import com.vimalcvs.trello.firebase.FireStoreClass
import com.vimalcvs.trello.models.Board
import com.vimalcvs.trello.utils.Constants

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var mUserName: String
    private var mSelectedBoardImageFileUri: Uri? = null
    private var mBoardImageURL: String = ""

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionbar()
        binding.createBoardEtBoardName.doOnTextChanged { _, _, _, _ -> removeError() }

        binding.createBoardIvBoard.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }
        binding.createBoardBtnUpdate.setOnClickListener {
            if (validateBoardName()) {
                if (mSelectedBoardImageFileUri != null) {
                    showProgressDialog(resources.getString(R.string.progress_please_wait))
                    uploadBoardImage()
                } else {
                    showProgressDialog(resources.getString(R.string.progress_please_wait))
                    createBoard()
                }
            }
        }
    }

    private fun removeError() {
        binding.createBoardTilBoardName.setErrorMessage("")
    }

    private fun validateBoardName(): Boolean {
        val isNameBlank = binding.createBoardEtBoardName.text.toString().trim().isBlank()
        return if (isNameBlank) {
            binding.createBoardTilBoardName.setErrorMessage("Board name can not be empty.")
            false
        } else true
    }

    fun boardCreatedSuccessFully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionbar() {
        setSupportActionBar(binding.createBoardToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.createBoardToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied storage permission, you can allow it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MyProfileActivity.PICK_IMAGE_REQUEST_CODE && data?.data != null) {
            mSelectedBoardImageFileUri = data.data!!
            try {
                Glide
                    .with(this)
                    .load(mSelectedBoardImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.img_user_placeholder)
                    .into(binding.createBoardIvBoard)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createBoard() {
        val currentUserId = FireStoreClass().getCurrentUserId()
        val assignedUserList: ArrayList<String> = ArrayList()
        assignedUserList.add(currentUserId)
        val board = Board(
            binding.createBoardEtBoardName.text.toString().trim(),
            mBoardImageURL,
            mUserName,
            currentUserId,
            assignedUserList
        )
        FireStoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        if (mSelectedBoardImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_" + mUserName + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedBoardImageFileUri
                )
            )
            sRef.putFile(mSelectedBoardImageFileUri!!).addOnSuccessListener { taskSnapShot ->
                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    mBoardImageURL = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener { exception ->
                hideProgressDialog()
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}