package com.example.obtimmy

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class ShiftsAdapter(ctx : Context) : RecyclerView.Adapter<MyViewHolder>(), CoroutineScope by MainScope() {

    lateinit var shiftFrag : ShiftsFragment
    var shiftdb = DatabaseModel(ctx)

    var shiftitems : List<DatabaseModel.SingleShift2>? = null

    //var sortedShiftItems = shiftitems?.sortedBy { DatabaseModel.SingleShift2. }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val vh = MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.shift_item, parent, false))
        return vh
    }

    override fun getItemCount(): Int {
        shiftitems?.let {
            return it.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        var currentItem = shiftitems!![position]

        holder.dateText.text = currentItem.readableTime
        holder.dayOfTheWeekText.text = currentItem.dayOfTheWeek

        holder.deleteButton.setOnClickListener {

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setMessage("Vill du radera det här skiftet?")


            builder.setPositiveButton("Ja") { dialog, which ->
                shiftFrag.shiftsModel.deleteSingleShift(currentItem)
            }

            builder.setNegativeButton("Nej") { dialog, which ->

            }

            builder.show()
        }

    }

}

class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    var dayOfTheWeekText = view.findViewById<TextView>(R.id.shiftItemDay)
    var dateText = view.findViewById<TextView>(R.id.shiftItemDate)
    var deleteButton = view.findViewById<Button>(R.id.shiftItemDeleteButton)

}