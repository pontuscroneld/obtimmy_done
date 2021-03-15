package com.example.obtimmy

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class ShiftsFragment : Fragment(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, CoroutineScope by MainScope() {

    lateinit var databaseModel : DatabaseModel
    lateinit var shiftsModel : ShiftsModel
    lateinit var shiftsadapter : ShiftsAdapter
    lateinit var sharedPreferences: SharedPreferences
    lateinit var handelsSharedPref: SharedPreferences


    var x1 : Float = 0.0f
    var x2 : Float = 0.0f
    var y1 : Float = 0.0f
    var y2 : Float = 0.0f

    companion object {
        const val MIN_DISTANCE = 150
    }

    var sliderValue = 0

    var isStartTime = true

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shifts_2, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        databaseModel = DatabaseModel(requireContext())
        shiftsModel = ViewModelProvider(this).get(ShiftsModel::class.java)
        shiftsModel.database = databaseModel
        shiftsadapter = ShiftsAdapter(ctx = requireContext())
        shiftsModel.getAllShifts()
        shiftsadapter.shiftFrag = this

        loadSharedPref()

        if(shiftsModel.handelsOrRestaurang == 0){
            findNavController().navigate(R.id.action_reset_app)
        }

        var savedWage = loadWageData()

        if(savedWage != 0){
            view.findViewById<SeekBar>(R.id.shiftsSliderBar).isEnabled = false
        }

        view.findViewById<TextView>(R.id.shiftsWageTV).text = "Timlön: " + savedWage.toInt()


        var shiftRV = view.findViewById<RecyclerView>(R.id.shiftsRecView)
        shiftRV.layoutManager = LinearLayoutManager(context)
        shiftRV.addItemDecoration(DividerItemDecoration(shiftRV.context, DividerItemDecoration.VERTICAL))
        shiftRV.adapter = shiftsadapter


        val startTimeButton = view.findViewById<Button>(R.id.shiftsStartTimeButton)

        var wageSlider = view.findViewById<SeekBar>(R.id.shiftsSliderBar)
        wageSlider.max = 250
        wageSlider.progress = savedWage



        wageSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("timmydebug", progress.toString())
                view.findViewById<TextView>(R.id.shiftsWageTV).text = "Timlön: " + progress
                sliderValue = progress
                shiftsModel.hourlyWage = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

                Log.d("timmydebug", "Touching bar")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        val wageSwitch = view.findViewById<Switch>(R.id.shiftsWageSwitch)

        if(wageSwitch.isChecked){
            wageSwitch.text = "Ändra"
        } else {
            wageSwitch.text = "Spara"
        }

        wageSwitch.setOnClickListener {

            if(wageSwitch.isChecked){
                saveWageData()
                shiftsModel.hourlyWage = sliderValue
                wageSlider.isEnabled = false
                wageSwitch.text = "Ändra"

            } else{
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Vill du ändra din timlön?")
                builder.setMessage("Om du gör det kommer alla nya skift sparas med din nya lön. Alla redan listade skift kommer fortfarande räknas med din gamla.")

                builder.setPositiveButton("Ok!") { dialog, which ->
                    wageSlider.isEnabled = true
                    wageSwitch.text = "Spara"
                }
                builder.show()

            }

        }
        // START TIME BUTTON
        startTimeButton.setOnClickListener{
            isStartTime = true
            shiftsModel.getTimeDateCalender()
            var startDP = DatePickerDialog(requireContext())
            startDP.setOnDateSetListener { view, year, month, dayOfMonth ->

                shiftsModel.setStartDate(dayOfMonth, month, year)

                var startTD = TimePickerDialog(context, this, shiftsModel.hour, shiftsModel.minute, true)
                startTD.setTitle("Start")
                startTD.show()
            }
            startDP.show()

        }

        // RESET BUTTON
        val resetButton = view.findViewById<Button>(R.id.resetButton)
        resetButton.setOnClickListener {

            shiftsModel.deleteAllShifts(requireContext())

        }
        // CALCULATE BUTTON
        val calcButton = view.findViewById<Button>(R.id.shiftsTotalButton)
        calcButton.setOnClickListener {

            findNavController().navigate(R.id.action_shiftsToFinal)
/*
            var totalText = view.findViewById<TextView>(R.id.shiftsTotalTV)

            launch {
                totalText.text = "Total lön: " + shiftsModel.calculateSumOfEarnings()
            }*/
        }

        shiftsModel.getErrormessage().observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })

        databaseModel.liveDataShiftList.observe(viewLifecycleOwner, { allShifts ->

            shiftsadapter.shiftitems = allShifts
            shiftsadapter.notifyDataSetChanged()
            Log.d("timmydebug", "Data set changed")
        })

        shiftsModel.getAllShifts()

    }

    fun pickEndTime(){
        var endTD = TimePickerDialog(requireContext(), this, shiftsModel.hour, shiftsModel.minute, true)
        endTD.setTitle("Slut")
        endTD.show()
    }




    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        if(isStartTime){
            shiftsModel.setStartTime(minute, hourOfDay)
            isStartTime = false
            pickEndTime()

        } else {
            isStartTime = true
            shiftsModel.setEndTime(minute, hourOfDay)
        }
    }

    fun saveWageData(){

        sharedPreferences = this.requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putInt("Wage", sliderValue!!)
            putBoolean("SwitchBool", true)
        }.apply()

        Toast.makeText(this.getActivity(), "Timlön sparad", Toast.LENGTH_SHORT).show()
    }

    fun loadWageData() : Int {

        sharedPreferences = this.requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedInt = sharedPreferences.getInt("Wage", 0)
        val savedBoolean = sharedPreferences.getBoolean("SwitchBool", false)

        view?.findViewById<Switch>(R.id.shiftsWageSwitch)!!.isChecked = savedBoolean
        shiftsModel.hourlyWage = savedInt
        return savedInt

    }

    fun loadSharedPref() {

        handelsSharedPref = this.requireActivity().getSharedPreferences("handelsOrRest", Context.MODE_PRIVATE)
        val savedInt = handelsSharedPref.getInt("handelsInt", 1)

        Log.d("timmydebug", "Null/Handels/Restaurang: " + savedInt.toString())
        shiftsModel.handelsOrRestaurang = savedInt

    }



}

    /*
        shiftsModel = ViewModelProvider(this).get(ShiftsModel::class.java)

        val calculateButton = view.findViewById<Button>(R.id.shiftsCalculateButton)
        calculateButton.setOnClickListener {
            findNavController().navigate(R.id.action_shiftsToFinal)
        }

 */


