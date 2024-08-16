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
import com.vimalcvs.trello.databinding.ActivityMyProfileBinding
import com.vimalcvs.trello.firebase.FireStoreClass
import com.vimalcvs.trello.models.User
import com.vimalcvs.trello.utils.Constants


class MyProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private var mUser = User()


    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        const val PICK_IMAGE_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionbar()

        FireStoreClass().loadUserData(this)

        binding.myProfileEtName.doOnTextChanged { _, _, _, _ -> removeError() }
        binding.myProfileEtMobile.doOnTextChanged { _, _, _, _ -> removeError() }

        binding.myProfileIvProfile.setOnClickListener {
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

        binding.myProfileBtnUpdate.setOnClickListener {
            val name = binding.myProfileEtName.text.toString().trim()
            val mobile = binding.myProfileEtMobile.text.toString().trim()
            if (validateForm(name, mobile)) {
                if (mSelectedImageFileUri != null) {
                    showProgressDialog(resources.getString(R.string.progress_please_wait))
                    uploadUserImage()
                } else {
                    showProgressDialog(resources.getString(R.string.progress_please_wait))
                    updateUserDetails()
                }
            }
        }
    }

    private fun updateUserDetails() {
        val userName = binding.myProfileEtName.text.toString().trim()
        val userMobile = binding.myProfileEtMobile.text.toString().trim()

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotBlank() && mProfileImageURL != mUser.image)
            userHashMap[Constants.IMAGE] = mProfileImageURL

        if (userName.isNotBlank() && userName != mUser.name)
            userHashMap[Constants.NAME] = userName

        if (userMobile.isNotBlank() && userMobile.toLong() != mUser.mobile)
            userHashMap[Constants.MOBILE] = userMobile.toLong()
        else if (userMobile.isBlank())
            userHashMap[Constants.MOBILE] = 0L

        if (userHashMap.isNotEmpty()) {
            FireStoreClass().updateUserData(this, userHashMap)
        } else {
            hideProgressDialog()
            Toast.makeText(this, resources.getString(R.string.no_data_changed), Toast.LENGTH_SHORT)
                .show()
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
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data?.data != null) {
            mSelectedImageFileUri = data.data!!
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.img_user_placeholder)
                    .into(binding.myProfileIvProfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun setupActionbar() {
        setSupportActionBar(binding.myProfileToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.myProfileToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUserDataInUI(loggedInUser: User) {
        mUser = loggedInUser
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.img_user_placeholder)
            .into(binding.myProfileIvProfile)

        binding.myProfileEtName.setText(loggedInUser.name)
        binding.myProfileEtEmail.setText(loggedInUser.email)
        if (loggedInUser.mobile != 0L)
            binding.myProfileEtMobile.setText(loggedInUser.mobile.toString())

    }

    private fun uploadUserImage() {
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_" + mUser.name + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedImageFileUri
                )
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapShot ->
                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    mProfileImageURL = uri.toString()
                    updateUserDetails()
                }
            }.addOnFailureListener { exception ->
                hideProgressDialog()
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this,
            resources.getString(R.string.profile_updated_successfully),
            Toast.LENGTH_SHORT
        ).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun removeError() {
        binding.myProfileTilName.setErrorMessage("")
        binding.myProfileTilMobile.setErrorMessage("")
    }

    private fun validateForm(name: String, mobile: String): Boolean {
        return when {
            name.isBlank() -> {
                binding.myProfileTilName.setErrorMessage("Name can not be empty.")
                false
            }

            mobile.isNotBlank() && mobile.length != 10 -> {
                binding.myProfileTilMobile.setErrorMessage("Mobile number must be 10 digits.")
                false
            }

            else -> true
        }
    }

}