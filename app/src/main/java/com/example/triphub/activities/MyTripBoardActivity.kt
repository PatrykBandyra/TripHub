package com.example.triphub.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triphub.R
import com.example.triphub.adapters.MyTaskListItemsAdapter
import com.example.triphub.databinding.ActivityMyTripBoardBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.models.MyCard
import com.example.triphub.models.MyTask
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants

/**
 * TaskListActivity
 */
class MyTripBoardActivity : BaseActivity<ActivityMyTripBoardBinding>() {

    private lateinit var mTrip: MyTrip
    private lateinit var mUser: User
    lateinit var mAssignedMemberDetailsList: ArrayList<User>

    private val cardDetailsResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showProgressDialog(R.string.please_wait)
                MyTripFireStore().getMyTrip(this, mTrip.documentId)
            } else {
                Log.i("Cancelled", "Cancelled")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(Constants.Intent.TRIP)) {
            mTrip = intent.getParcelableExtra(Constants.Intent.TRIP)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No MyTrip object provided")
        }

        if (intent.hasExtra(Constants.Intent.USER_DATA)) {
            mUser = intent.getParcelableExtra(Constants.Intent.USER_DATA)!!
        } else {
            throw Exception("${this::class.java.simpleName}: No User object provided")
        }

        setUpTaskBarNavigation()
        MyTripFireStore().getMyTrip(this, mTrip.documentId)
    }

    private fun setUpTaskBarNavigation() {
        binding.ivPeople.setOnClickListener {
            val intent = Intent(this, MyTripPeopleActivity::class.java)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivMap.setOnClickListener {
            val intent = Intent(this, MyTripMapActivity::class.java)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivChat.setOnClickListener {
            val intent = Intent(this, MyTripChatActivity::class.java)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun getViewBinding() = ActivityMyTripBoardBinding.inflate(layoutInflater)

    fun createTaskList(taskListName: String) {
        val task = MyTask(taskListName, mUser.id)
        mTrip.taskList.add(0, task)
        mTrip.taskList.removeAt(mTrip.taskList.size - 1)  // UI element

        showProgressDialog()
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }

    fun onGetMyTripSuccess(myTrip: MyTrip) {
        mTrip = myTrip
        hideProgressDialog()

        showProgressDialog()
        MyTripFireStore().loadPeopleAssignedToTrip(this, mTrip.userIDs)
    }

    fun onPeopleLoadedSuccess(usersList: ArrayList<User>) {
        mAssignedMemberDetailsList = usersList
        hideProgressDialog()

        val addTaskList = MyTask(resources.getString(R.string.action_add_list))
        mTrip.taskList.add(addTaskList)  // UI element

        binding.rvTaskList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaskList.setHasFixedSize(true)

        val adapter = MyTaskListItemsAdapter(this, mTrip.taskList)
        binding.rvTaskList.adapter = adapter
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog()
        MyTripFireStore().getMyTrip(this, mTrip.documentId)
    }

    fun updateTaskList(position: Int, listName: String, task: MyTask) {
        val myTask = MyTask(listName, task.createdBy)

        mTrip.taskList[position] = myTask
        mTrip.taskList.removeAt(mTrip.taskList.size - 1)

        showProgressDialog()
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mTrip.taskList.removeAt(mTrip.taskList.size - 1)  // UI change

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        val currentUserId = mUser.id
        cardAssignedUsersList.add(currentUserId)

        val card = MyCard(cardName, currentUserId, cardAssignedUsersList)

        val cardsList = mTrip.taskList[position].cards
        cardsList.add(card)

        val task = MyTask(
            mTrip.taskList[position].title,
            mTrip.taskList[position].createdBy,
            cardsList
        )

        mTrip.taskList[position] = task

        showProgressDialog()
        // Updating the parent
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, MyTripCardDetailsActivity::class.java)
        intent.putExtra(Constants.Intent.TRIP, mTrip)
        intent.putExtra(Constants.Intent.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.Intent.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.Intent.TRIP_MEMBERS_LIST, mAssignedMemberDetailsList)
        cardDetailsResultLauncher.launch(intent)
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<MyCard>) {
        mTrip.taskList.removeAt(mTrip.taskList.size - 1)
        mTrip.taskList[taskListPosition].cards = cards
        showProgressDialog()
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }

    fun deleteTaskList(position: Int) {
        mTrip.taskList.removeAt(position)
        mTrip.taskList.removeAt(mTrip.taskList.size - 1)

        showProgressDialog()
        MyTripFireStore().addUpdateTaskList(this, mTrip)
    }


}