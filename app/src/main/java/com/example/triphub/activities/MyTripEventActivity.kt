package com.example.triphub.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.example.triphub.R
import com.example.triphub.databinding.ActivityMyTripEventBinding
import com.example.triphub.databinding.DialogAddEventBinding
import com.example.triphub.firebase.MyTripFireStore
import com.example.triphub.firebase.UserFireStore
import com.example.triphub.models.MyTrip
import com.example.triphub.models.User
import com.example.triphub.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class MyTripEventActivity : BaseActivity<ActivityMyTripEventBinding>() {

    private lateinit var mTrip: MyTrip
    private lateinit var mUser: User

    private var calendar = Calendar.getInstance()

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

        binding.btnAddEvent.setOnClickListener {
            val dialog = Dialog(this)
            val dialogBinding = DialogAddEventBinding.inflate(LayoutInflater.from(this))
            dialog.setContentView(dialogBinding.root)
            dialogBinding.etDate.setOnClickListener {
                onDatePickerClicked(dialogBinding)
            }
            dialogBinding.tvCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.tvAdd.setOnClickListener {
                val title = dialogBinding.etTitle.text.toString()
                val date = dialogBinding.etDate.text.toString()
                if (title.isNotEmpty() && date.isNotEmpty()) {
                    dialog.dismiss()
//                    showProgressDialog(R.string.please_wait)
//                    MyTripFireStore().addEvent(this@MyTripEventActivity, )
                } else {
                    Toast.makeText(
                        this@MyTripEventActivity,
                        getString(R.string.fill_in_all_fields), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.show()
        }
    }

    private fun onDatePickerClicked(dialogBinding: DialogAddEventBinding) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)

                updateDateInView(dialogBinding)
            },
            year, month, day
        )
        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()
    }

    private fun updateDateInView(dialogBinding: DialogAddEventBinding) {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        dialogBinding.etDate.setText(sdf.format(calendar.time).toString())
    }

    override fun getViewBinding() = ActivityMyTripEventBinding.inflate(layoutInflater)

    private fun setUpTaskBarNavigation() {
        binding.ivBoard.setOnClickListener {
            val intent = Intent(this, MyTripBoardActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivPeople.setOnClickListener {
            val intent = Intent(this, MyTripPeopleActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivMap.setOnClickListener {
            val intent = Intent(this, MyTripMapActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        binding.ivChat.setOnClickListener {
            val intent = Intent(this, MyTripChatActivity::class.java)
            intent.putExtra(Constants.Intent.TRIP, mTrip)
            intent.putExtra(Constants.Intent.USER_DATA, mUser)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}