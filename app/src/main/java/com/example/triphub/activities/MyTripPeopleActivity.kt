package com.example.triphub.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.triphub.R
import com.example.triphub.adapters.MyTripPeopleAdapter
import com.example.triphub.databinding.ActivityMyTripPeopleBinding
import com.example.triphub.databinding.DialogAddPersonBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import com.example.triphub.utils.SwipeToDeleteCallback

class MyTripPeopleActivity : BaseActivity<ActivityMyTripPeopleBinding>() {

    private lateinit var mTrip: MyTrip
    private lateinit var mAdapter: MyTripPeopleAdapter
    private var isCreator: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No MyTrip object provided")
        }

        setUpTaskBarNavigation()

        isCreator = mTrip.creatorID == UserFireStore().getCurrentUserId()
        MyTripFireStore().getMyTrip(this, mTrip.documentId)

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

    private fun setUpTaskBarNavigation() {
        binding.ivBoard.setOnClickListener {
            startActivity(Intent(this, MyTripBoardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivMap.setOnClickListener {
            startActivity(Intent(this, MyTripMapActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivChat.setOnClickListener {
            startActivity(Intent(this, MyTripChatActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
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

    private fun setUpPeopleListInAdapter(items: ArrayList<User>) {
        binding.rvPeople.layoutManager = LinearLayoutManager(this)
        mAdapter = MyTripPeopleAdapter(this, items)
        binding.rvPeople.adapter = mAdapter

        if (isCreator) {
            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val deleteDialog = AlertDialog.Builder(this@MyTripPeopleActivity)
                    deleteDialog.setIcon(R.drawable.ic_delete_red_24dp)
                    deleteDialog.setTitle(R.string.remove_person_from_trip)
                    deleteDialog.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        showProgressDialog()
                        mAdapter.removeAt(
                            this@MyTripPeopleActivity,
                            viewHolder.adapterPosition,
                            mTrip
                        )
                        dialog.dismiss()
                    }
                    deleteDialog.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                        mAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    deleteDialog.show()
                }
            }

            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(binding.rvPeople)
        }
    }

    private fun getPeopleList(trip: MyTrip) {
        showProgressDialog()
        MyTripFireStore().loadPeopleAssignedToTrip(this, trip.userIDs)
    }

    fun onPeopleLoadedSuccess(users: ArrayList<User>) {
        setUpPeopleListInAdapter(users)
        hideProgressDialog()
        mAdapter.notifyItemInserted(mAdapter.itemCount)
    }

    fun onAddPersonSuccess() {
        hideProgressDialog()
        getPeopleList(mTrip)
    }

    fun onAddPersonFailure() {
        hideProgressDialog()
        showErrorSnackBar(R.string.error_adding_person_to_trip)
    }

    fun onPeopleLoadedFailure() {
        hideProgressDialog()
        showErrorSnackBar(R.string.could_not_load_people_trip)
    }

    fun onPersonRemovalSuccess(position: Int) {
        hideProgressDialog()
        mAdapter.items.removeAt(position)
        mAdapter.notifyItemRemoved(position)
    }

    fun onPersonRemovalFailure(position: Int) {
        hideProgressDialog()
        mAdapter.notifyItemChanged(position)
        showErrorSnackBar(R.string.could_not_remove_person_from_trip)
    }

    fun onGetMyTripSuccess(myTrip: MyTrip) {
        mTrip = myTrip
        getPeopleList(mTrip)
    }
}