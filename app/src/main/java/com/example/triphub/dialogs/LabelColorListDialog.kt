package com.example.triphub.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triphub.adapters.LabelColorListItemsAdapter
import com.example.triphub.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var items: ArrayList<String>,
    private var title: String = "",
    private var mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null
    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context, items, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickedListener =
            object : LabelColorListItemsAdapter.OnItemClickedListener {
                override fun onClick(position: Int, color: String) {
                    dismiss()
                    onItemSelected(color)
                }
            }
    }

    protected abstract fun onItemSelected(color: String)
}