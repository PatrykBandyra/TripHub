package com.example.triphub.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMyTripPeopleBinding
import com.example.triphub.databinding.DialogAddPersonBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

class MyTripPeopleActivity : BaseActivity<ActivityMyTripPeopleBinding>() {

    private lateinit var mTrip: MyTrip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        }

        binding.btnAddPerson.setOnClickListener {
            val dialog = Dialog(this)
            val dialogBinding = DialogAddPersonBinding.inflate(LayoutInflater.from(this))
            dialog.setContentView(dialogBinding.root)
            dialogBinding.tvCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.tvAdd.setOnClickListener {
                val email = dialogBinding.etEmailSearchMember.text.toString()
                if (email.isNotEmpty()) {
                    dialog.dismiss()
                    showProgressDialog(R.string.please_wait)
                    UserFireStore().getPersonDetails(this, email)
                } else {
                    Toast.makeText(
                        this@MyTripPeopleActivity,
                        getString(R.string.enter_person_email), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.show()
        }

    }

    override fun getViewBinding() = ActivityMyTripPeopleBinding.inflate(layoutInflater)

    fun onGetPersonDetailsFail() {
        hideProgressDialog()
        showErrorSnackBar(R.string.error_person_data)
    }

    fun onNoSuchPerson() {
        hideProgressDialog()
        showErrorSnackBar(R.string.no_such_person)
    }

    fun personDetails(user: User) {
        mTrip.userIDs.add(user.id)
        MyTripFireStore().addPersonToTrip(this, mTrip)
    }

    fun onAddPersonSuccess() {
        hideProgressDialog()
        // TODO: add person to recycler view
    }

    fun onAddPersonFailure() {
        hideProgressDialog()
        showErrorSnackBar(R.string.error_adding_person_to_trip)
    }
}