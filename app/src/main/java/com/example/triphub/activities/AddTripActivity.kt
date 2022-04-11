package com.example.triphub.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.triphub.R
import com.example.triphub.databinding.ActivityAddTripBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException

class AddTripActivity : BaseActivity<ActivityAddTripBinding>() {

    private var mSelectedImageFileUri: Uri? = null
    private var mTripImageURL: String = ""

    private lateinit var userData: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            userData = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        }

        setUpActionBarForReturnAction(
            binding.toolbarAddTripActivity,
            icon = R.drawable.back_arrow_white
        )

        binding.ivTripImage.setOnClickListener {
            hideKeyboard()
            showImageChooserDialog()
        }

        binding.btnCreateTrip.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadTripImage()
            } else {
                createTrip()
            }
        }

    }

    private fun uploadTripImage() {
        showProgressDialog()
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "TRIP_IMAGE${System.currentTimeMillis()}.${getFileExtension(mSelectedImageFileUri)}"
            )
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.i("Downloadable Image URL", uri.toString())
                        mTripImageURL = uri.toString()
                        createTrip()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@AddTripActivity, exception.message, Toast.LENGTH_LONG)
                        .show()
                    hideProgressDialog()
                }
        }
    }

    private fun createTrip() {
        val name: String = binding.etName.text.toString().trim { it <= ' ' }
        val description: String = binding.etDescription.text.toString().trim { it <= ' ' }

        if (validateForm(name)) {
            hideKeyboard()
            showProgressDialog()
            val usersIDs: ArrayList<String> = ArrayList()
            usersIDs.add(userData.id)
            val newTrip = MyTrip(
                name = name,
                description = description,
                image = mTripImageURL,
                creatorID = userData.id,
                creatorName = userData.name,
                userIDs = usersIDs,
                createdAt = System.currentTimeMillis()
            )
            MyTripFireStore().createMyTrip(this, newTrip)
        }
    }

    private fun validateForm(name: String): Boolean {
        return when {
            name.isEmpty() -> {
                showErrorSnackBar(R.string.add_trip_name_error)
                false
            }
            else -> {
                true
            }
        }
    }

    override fun getViewBinding() = ActivityAddTripBinding.inflate(layoutInflater)

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
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.ivTripImage)
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
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.ivTripImage)
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

    fun onCreateMyTripSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun onCreateMyTripFailure() {
        hideProgressDialog()
        showErrorSnackBar(R.string.create_new_trip_error)
    }


}