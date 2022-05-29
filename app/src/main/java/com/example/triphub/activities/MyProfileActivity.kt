package com.example.triphub.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMyProfileBinding
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.*

class MyProfileActivity : BaseActivity<ActivityMyProfileBinding>() {

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""

    private lateinit var mUser: User
    private lateinit var auth: FirebaseAuth

    private val galleryImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data!!.data != null) {
                    mSelectedImageFileUri = result.data!!.data
                    try {
                        Glide
                            .with(this)
                            .load(mSelectedImageFileUri)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_placeholder_image)
                            .into(binding.ivUserImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun chooseImageFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    galleryImageResultLauncher.launch(galleryIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }

        }).onSameThread().check()
    }

    private fun afterPictureTaken(imageUri: Uri?) {
        try {
            mSelectedImageFileUri = imageUri

            Glide
                .with(this)
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder_image)
                .into(binding.ivUserImage)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val cameraImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val imageUri = result.data!!.data

                    afterPictureTaken(imageUri)
                }
            }
        }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraImageResultLauncher.launch(cameraIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }

        }).onSameThread().check()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        setUpActionBarForReturnAction(
            binding.toolbarMyProfileActivity,
            icon = R.drawable.back_arrow_white
        )

        UserFireStore().loadUserData(this@MyProfileActivity)

        binding.ivUserImage.setOnClickListener {
            showImageChooserDialog()
        }

        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                updateUserProfileData()
            }
        }

        binding.tvChangePassword.setOnClickListener {
            showPasswordFields()
        }

        binding.btnUpdatePassword.setOnClickListener {
            onPasswordUpdateButtonClicked()
        }

    }

    private fun showPasswordFields() {
        binding.tilOldPassword.visibility = View.VISIBLE
        binding.tilNewPassword1.visibility = View.VISIBLE
        binding.tilNewPassword2.visibility = View.VISIBLE
        binding.btnUpdatePassword.visibility = View.VISIBLE

        binding.tvChangePassword.visibility = View.GONE
    }

    private fun hidePasswordFields() {
        binding.tilOldPassword.visibility = View.GONE
        binding.tilNewPassword1.visibility = View.GONE
        binding.tilNewPassword2.visibility = View.GONE
        binding.btnUpdatePassword.visibility = View.GONE

        binding.tvChangePassword.visibility = View.VISIBLE
    }

    private fun cleanPasswordInputs() {
        binding.etOldPassword.setText("")
        binding.etNewPassword1.setText("")
        binding.etNewPassword2.setText("")
    }

    private fun onPasswordUpdateButtonClicked() {
        hideKeyboard()
        showProgressDialog()

        val oldPassword: String = binding.etOldPassword.text.toString().trim { it <= ' ' }
        val newPassword1: String = binding.etNewPassword1.text.toString().trim { it <= ' ' }
        val newPassword2: String = binding.etNewPassword2.text.toString().trim { it <= ' ' }

        if (validatePasswordChangeForm(oldPassword, newPassword1, newPassword2)) {
            auth.signInWithEmailAndPassword(mUser.email, oldPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        UserFireStore().getCurrentUser()!!.updatePassword(newPassword1)
                            .addOnSuccessListener {
                                hidePasswordFields()
                                cleanPasswordInputs()
                                hideProgressDialog()
                                Toast.makeText(
                                    this@MyProfileActivity,
                                    getString(R.string.my_profile_password_update_success),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                hidePasswordFields()
                                hideProgressDialog()
                                Toast.makeText(
                                    this@MyProfileActivity,
                                    getString(
                                        R.string.my_profile_password_update_failure,
                                        e.message
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        hideProgressDialog()
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            hideProgressDialog()
        }
    }

    private fun validatePasswordChangeForm(
        oldPassword: String,
        newPassword1: String,
        newPassword2: String
    ): Boolean {
        return when {
            oldPassword.isEmpty() -> {
                showErrorSnackBar(R.string.my_profile_enter_old_password)
                false
            }
            newPassword1.isEmpty() -> {
                showErrorSnackBar(R.string.my_profile_enter_new_password)
                false
            }
            newPassword2.isEmpty() -> {
                showErrorSnackBar(R.string.my_profile_repeat_new_password)
                false
            }
            else -> {
                if (newPassword1 == newPassword2) {
                    true
                } else {
                    showErrorSnackBar(R.string.my_profile_entered_new_passwords_different_error)
                    false
                }
            }
        }
    }

    private fun showImageChooserDialog() {
        val imageDialog = AlertDialog.Builder(this)
        imageDialog.setTitle(R.string.select_action)
        val imageDialogItems = arrayOf(
            resources.getString(R.string.select_image_from_gallery),
            resources.getString(R.string.take_photo)
        )
        imageDialog.setItems(imageDialogItems) { _, which ->
            when (which) {
                0 -> chooseImageFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        imageDialog.show()
    }

    override fun getViewBinding() = ActivityMyProfileBinding.inflate(layoutInflater)

    fun setUserDataInUI(user: User) {
        mUser = user

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder_image)
            .into(binding.ivUserImage)
    }

    private fun uploadUserImage() {
        showProgressDialog()
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE${System.currentTimeMillis()}.${getFileExtension(mSelectedImageFileUri)}"
            )
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.i("Downloadable Image URL", uri.toString())
                        mProfileImageURL = uri.toString()
                        deleteOldUserProfileImage()
                        updateUserProfileData()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG)
                        .show()
                    hideProgressDialog()
                }
        }
    }

    private fun deleteOldUserProfileImage() {
        if (mUser.image.isNotEmpty()) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(mUser.image)
            sRef.delete()
                .addOnSuccessListener {
                    Log.i("Firebase Image", "Old user profile picture ${mUser.image} deleted")
                }
                .addOnFailureListener {
                    Log.i(
                        "Firebase Image",
                        "Could not delete old user profile picture ${mUser.image}"
                    )
                }
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUser.image) {
            userHashMap[Constants.Models.User.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }
        if (binding.etName.text.toString() != mUser.name) {
            userHashMap[Constants.Models.User.NAME] = binding.etName.text.toString()
            anyChangesMade = true
        }

        if (anyChangesMade) {
            showProgressDialog()
            UserFireStore().updateUser(this, UserFireStore().getCurrentUserId(), userHashMap)
        }
    }

    fun onUserUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun onUserUpdateFailure() {
        hideProgressDialog()
        showErrorSnackBar(R.string.could_not_update_user)
    }
}