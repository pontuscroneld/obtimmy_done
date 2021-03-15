package com.example.obtimmy

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class StartFragment : Fragment() {

    lateinit var handelsSharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSharedPref()
        if(isDarkModeOn()){
            view.findViewById<ImageView>(R.id.startTimmyText).visibility = View.INVISIBLE
        }



        val restButton = view.findViewById<Button>(R.id.startRestButton)

        restButton.setOnClickListener {
            savePrefDataRest()
            findNavController().navigate(R.id.action_startToShifts)
        }


        val shopButton = view.findViewById<Button>(R.id.startShopButton)

        shopButton.setOnClickListener {
            savePrefDataHandels()
            findNavController().navigate(R.id.action_startToShifts)
        }

        val debug = view.findViewById<Button>(R.id.debugButton)

        debug.setOnClickListener {
            loadSharedPref()
            findNavController().navigate(R.id.action_startToShifts)
        }

    }

    fun savePrefDataHandels(){

        handelsSharedPref = this.requireActivity().getSharedPreferences("handelsOrRest", Context.MODE_PRIVATE)
        val editor = handelsSharedPref.edit()
        editor.apply{

            putInt("handelsInt", 1)
        }.apply()

        Log.d("timmydebug", "Jobbar inom Handels")

    }

    fun savePrefDataRest(){

        handelsSharedPref = this.requireActivity().getSharedPreferences("handelsOrRest", Context.MODE_PRIVATE)
        val editor = handelsSharedPref.edit()
        editor.apply{

            putInt("handelsInt", 2)
        }.apply()

        Log.d("timmydebug", "Jobbar inom Restaurang")

    }

    fun loadSharedPref() {

        handelsSharedPref = this.requireActivity().getSharedPreferences("handelsOrRest", Context.MODE_PRIVATE)

        val savedInt = handelsSharedPref.getInt("handelsInt", 0)

        if(savedInt != 0){
            findNavController().navigate(R.id.action_startToShifts)
        }

        Log.d("timmydebug", savedInt.toString())

    }

    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }



}