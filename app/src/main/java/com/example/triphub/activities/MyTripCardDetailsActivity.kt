package com.example.triphub.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.triphub.R
import com.example.triphub.adapters.MyTripCardMemberListItemsAdapter
import com.example.triphub.databinding.ActivityMyTripCardDetailsBinding
import com.example.triphub.dialogs.LabelColorListDialog
import com.example.triphub.dialogs.MembersListDialog
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.models.*
import com.example.triphub.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class MyTripCardDetailsActivity : BaseActivity<ActivityMyTripCardDetailsBinding>() {

    private lateinit var mTrip: MyTrip
    private var mTaskListPosition: Int = -1
    private var mCardPosition: Int = -1
    private var mSelectedColor: String = ""
    private lateinit var mMembersDetailsList: ArrayList<User>
    private var mSelectedDueDateMilliseconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData()

        binding.etNameCardDetails.setText(mTrip.taskList[mTaskListPosition].cards[mCardPosition].name)
        // sets cursor to the end of text
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length)

        mSelectedColor = mTrip.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this, "Enter a card name", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        setUpSelectedMembersList()

        mSelectedDueDateMilliseconds =
            mTrip.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliseconds > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = sdf.format(Date(mSelectedDueDateMilliseconds))
            binding.tvSelectDueDate.text = selectedDate
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mTrip.taskList[mTaskListPosition].cards[mCardPosition].name)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(R.string.confirmation_message_to_delete_card, cardName)
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteCard() {
        val cardsList: ArrayList<MyCard> = mTrip.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<MyTask> = mTrip.taskList
        taskList.removeAt(taskList.size - 1)  // gets rid of "Add Card" entry

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog()
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val sDayOfMonth =
                    if (selectedDayOfMonth < 10) "0$selectedDayOfMonth" else "$selectedDayOfMonth"
                val sMonth =
                    if ((selectedMonth + 1) < 10) "0${selectedMonth + 1}" else "${selectedMonth + 1}"

                val selectedDate = "$sDayOfMonth/$sMonth/$selectedYear"
                binding.tvSelectDueDate.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliseconds = theDate!!.time
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun membersListDialog() {
        val cardAssignedMembersList =
            mTrip.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        if (cardAssignedMembersList.size > 0) {
            mMembersDetailsList.forEach { member ->
                cardAssignedMembersList.forEach { assignedMemberId ->
                    if (member.id == assignedMemberId) {
                        member.selected = true
                    }
                }
            }
        } else {
            mMembersDetailsList.forEach { member ->
                member.selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this, mMembersDetailsList, resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mTrip.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mTrip.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mTrip.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(
                        user.id
                    )

                    mMembersDetailsList.forEach { member ->
                        if (member.id == user.id) {
                            member.selected = false
                        }
                    }
                }

                setUpSelectedMembersList()
            }
        }

        listDialog.show()
    }

    private fun setUpSelectedMembersList() {
        val cardAssignedMembersList =
            mTrip.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMember> = ArrayList()

        mMembersDetailsList.forEach { member ->
            cardAssignedMembersList.forEach { assignedMemberId ->
                if (member.id == assignedMemberId) {
                    val selectedMember = SelectedMember(
                        member.id,
                        member.image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMember("", ""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(this, 6)
            val adapter = MyTripCardMemberListItemsAdapter(this, selectedMembersList, true)
            binding.rvSelectedMembersList.adapter = adapter

            adapter.setOnClickListener(
                object : MyTripCardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun colorList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun updateCardDetails() {
        val card = MyCard(
            binding.etNameCardDetails.text.toString(),
            mTrip.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mTrip.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliseconds
        )

        val taskList: ArrayList<MyTask> = mTrip.taskList
        taskList.removeAt(taskList.size - 1)

        mTrip.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog()
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }

    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun getViewBinding() = ActivityMyTripCardDetailsBinding.inflate(layoutInflater)

    private fun getIntentData() {
        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        }
        if (intent.hasExtra(Constants.Intent.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.Intent.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.Intent.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.Intent.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.Intent.TRIP_MEMBERS_LIST)) {
            mMembersDetailsList =
                intent.getParcelableArrayListExtra(Constants.Intent.TRIP_MEMBERS_LIST)!!
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}