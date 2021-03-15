package com.example.obtimmy

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ShiftsModel(app: Application) : AndroidViewModel(app), CoroutineScope by MainScope() {

    lateinit var database : DatabaseModel
    private var VMdateInfo = MutableLiveData<String>()
    lateinit var shiftsadapter : ShiftsAdapter
    private val errorMessage = MutableLiveData<String>()
    var handelsOrRestaurang = 1
    var hourlyWage = 0


    //////////////CALENDER//////////////////////////////////////////////////////////////////////

    var startDay = 0
    var startMonth = 0
    var startYear = 0
    var endDay = 0
    var endMonth = 0
    var endYear = 0

    var startHour = 0
    var startMinute = 0
    var endHour = 0
    var endMinute = 0

    var hour = 0
    var minute = 0
    var day = 0
    var month = 0
    var year = 0

    var startStamp = 0L
    var endStamp = 0L

    fun getTimeDateCalender(){

        var cal = Calendar.getInstance()
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)

    }

    fun setStartDate(setDayOfMonth: Int, setMonth: Int, setYear : Int){

        startYear = setYear
        startMonth = setMonth
        startDay = setDayOfMonth
        val cal = Calendar.getInstance()
        Log.d("timmydebug", "TZ " + cal.timeZone.toString())
        cal.set(Calendar.YEAR, startYear)
        cal.set(Calendar.MONTH, startMonth)
        cal.set(Calendar.DAY_OF_MONTH, startDay)
    }

    fun setStartTime(setMinute: Int, setHour: Int) {
        startHour = setHour
        startMinute = setMinute
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, startYear)
        cal.set(Calendar.MONTH, startMonth)
        cal.set(Calendar.DAY_OF_MONTH, startDay)
        cal.set(Calendar.HOUR_OF_DAY, startHour)
        cal.set(Calendar.MINUTE, startMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        startStamp = cal.timeInMillis

    }

    fun setEndTime(setMinute: Int, setHour: Int) {
        endHour = setHour
        endMinute = setMinute
        val cal = Calendar.getInstance()
        Log.d("timmydebug", "TZ " + cal.timeZone.toString())

        cal.set(Calendar.YEAR, startYear)
        cal.set(Calendar.MONTH, startMonth)
        cal.set(Calendar.DAY_OF_MONTH, startDay)
        cal.set(Calendar.HOUR_OF_DAY, endHour)
        cal.set(Calendar.MINUTE, endMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        endStamp = cal.timeInMillis

        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
        val dateString = simpleDateFormat.format(endStamp)
        val dateString2 = simpleDateFormat.format(startStamp)

        Log.d("timmydebug", "Start time set to " + startStamp.toString())
        Log.d("timmydebug", "As a date: " + dateString2)
        Log.d("timmydebug", "End time set to " + endStamp.toString())
        Log.d("timmydebug", "As a date: " + dateString)

        calcDuration(startStamp, endStamp)
    }

    ////////////// API //////////////////////////////////////////////////////////////////////

    fun getDateInfo(): LiveData<String> {
        return VMdateInfo
    }

    fun loadDate(chosenDate: Long, endDate: Long) {
        launch {
            Log.d("timmydebug", "Running fun loadDate in ShiftsModel")

            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            val dateString = simpleDateFormat.format(chosenDate)
            Log.d("timmydebug", dateString)
            val firstDate = loadapi(dateString)
            Log.d("timmydebug", "firstDate loaded")
            if (firstDate.datum == null) {
                //errorString.value = "Felaktig start"
                return@launch
            }

            var dateText = ""

            dateText += firstDate.datum
            dateText += "\n"
            dateText += firstDate.veckodag

            if(firstDate.rodDag == "Nej"){
                dateText += "\n"
                dateText += "Inte röd dag"
            } else {
                dateText += "\n"
                dateText += "Röd dag"
            }

            if(firstDate.helgdag == null){
                dateText += "\n"
                dateText += "Inte helgdag"
            } else {
                dateText += "\n"
                dateText += firstDate.helgdag
            }

            Log.d("timmydebug", dateText)
            Log.d("timmydebug", chosenDate.toString() + endDate.toString())
            VMdateInfo.value = dateText

            createShift(
                    chosenDate,
                    endDate,
                    firstDate.datum,
                    firstDate.veckodag,
                    firstDate.rodDag,
                    firstDate.helgdag
            )
        }
    }

    private suspend fun loadapi(dateString: String): apiDateInfo {

        Log.d("timmydebug", "Running fun loadapi in ShiftsModel")

        return withContext(Dispatchers.IO) {
            val theurl = URL("https://sholiday.faboul.se/dagar/v2.1/" + dateString)


            val theConnection = (theurl.openConnection() as? HttpURLConnection)!!.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")

            }
            Log.d("timmydebug", theurl.toString())
            val reader = BufferedReader(theConnection.inputStream.reader())
            Log.d("timmydebug", "Kommer den hit?")
            val theResultString = reader.readText()
            val theInfo = Gson().fromJson(theResultString, apiDays::class.java)

            return@withContext theInfo.dagar[0]


        }
    }

    //////////////ROOM//////////////////////////////////////////////////////////////////////

    fun getAllShifts()
    {
        Log.d("timmydebug", "Running getAllShifts")

        launch(Dispatchers.IO){
            var allTheShifts = database.shiftDB.ShiftDao().loadAll()

            allTheShifts = allTheShifts.sortedBy { it.startTime }

            launch(Dispatchers.Main){
                database.liveDataShiftList.value = allTheShifts
            }
        }
    }

    fun deleteAllShifts(ctx : Context){

        val builder = AlertDialog.Builder(ctx)
        builder.setTitle("Vill du tömma listan?")
        builder.setMessage("Detta kommer att radera alla pass du har registrerat.")

        builder.setPositiveButton("Ja") { dialog, which ->
            launch(Dispatchers.IO){
                database.shiftDB.ShiftDao().nukeTable()
                shiftsadapter = ShiftsAdapter(ctx)
                getAllShifts()
            }
        }
        builder.setNegativeButton("Nej") { dialog, which ->
        }
        builder.show()
    }

    fun deleteSingleShift(deleteShift : DatabaseModel.SingleShift2)
    {
        launch(Dispatchers.IO) {
            database.shiftDB.ShiftDao().delete(deleteShift)
            getAllShifts()
        }
    }

    suspend fun calculateSumOfEarnings(): Double {

        var allShiftEarnings = 0.0
        var allObEarnings = 0.0
        var totalEarnings = 0.0

        return withContext(Dispatchers.IO) {

            var listOfShifts = database.shiftDB.ShiftDao().loadAll()

            for (shift in listOfShifts) {
                Log.d("10Marchdebug", shift.readableTime!!)
                Log.d("10Marchdebug", "This shift is worth: " + shift.shiftEarnings.toString())
                totalEarnings = totalEarnings + shift.shiftEarnings!! + shift.obEarnings!!
                Log.d("10Marchdebug", "Total earnings are: " + totalEarnings.toString())
            }

            return@withContext totalEarnings
        }
    }

    suspend fun seeTotalTime() : String {

        var allMinutes = 0L
        var allHours = 0
        var remainderMinutes = 0L

        return withContext(Dispatchers.IO) {

            var listOfShifts = database.shiftDB.ShiftDao().loadAll()

            for (shift in listOfShifts) {
                allMinutes += shift.shiftDuration!!
            }

            Log.d("12march", allMinutes.toString())
            allHours = allMinutes.toInt()/60
            remainderMinutes = allMinutes-allHours*60

            return@withContext allHours.toString() + "h och " + remainderMinutes + "min"
        }
    }

    suspend fun seeTotalOBEarnings() : String {

        var allEarnings = 0.0
        var allOBEarnings = 0.0

        return withContext(Dispatchers.IO) {

            var listOfShifts = database.shiftDB.ShiftDao().loadAll()

            for (shift in listOfShifts) {
                allEarnings += shift.shiftEarnings!!
            }

            for(shift in listOfShifts){
                allOBEarnings += shift.obEarnings!!
            }

            var obInt = allOBEarnings.toInt()
            var wageInt = allEarnings.toInt()
            var bruttoInkomst = obInt+wageInt

            var summary = "\nBrutto inkomst: " + bruttoInkomst + "kr\nOrdinarie lön: " + wageInt + "kr\nOB-ersättning: " + obInt + "kr"

            return@withContext summary
        }
    }

    suspend fun seeTotalBreakTime() : String {

        var allMinutes = 0L
        var allHours = 0
        var remainderMinutes = 0L

        return withContext(Dispatchers.IO) {

            var listOfShifts = database.shiftDB.ShiftDao().loadAll()

            for (shift in listOfShifts) {
                allMinutes += shift.breakTime!!
            }

            Log.d("12march", allMinutes.toString())
            allHours = allMinutes.toInt()/60
            remainderMinutes = allMinutes-allHours*60

            return@withContext allHours.toString() + "h och " + remainderMinutes + "min"
        }
    }

    //////////////LOKALA FUNKTIONER///////////////////////////////////////////////////////////////

    fun getErrormessage(): LiveData<String> {
        return errorMessage
    }




    fun calcDuration(startStamp: Long, endStamp: Long) {

        Log.d("timmydebug", "Running fun calcDuration in ShiftsModel")
        if (startStamp > endStamp) {

            errorMessage.value = "Slutdatum är före startdatum."

            Log.d("timmydebug", "Slutdatum är före startdatum.")
        } else {

            var diffTime = (endStamp - startStamp) / 1000
            // Diff time är tiden man jobbar i sekunder
            var diffTimeInMinutes = diffTime / 60
            val diffTimeInHours = diffTime / 3600
            val minutesMinusHours = diffTimeInMinutes - (diffTimeInHours * 60)

            var minuteWage = hourlyWage.toDouble() / 60
            var earnings = (diffTimeInHours * hourlyWage) + (minutesMinusHours * minuteWage)
            Log.d("timmydebug", "Timmar: " + diffTimeInHours + " Minuter: " + diffTimeInMinutes)
            loadDate(startStamp, endStamp)
        }
    }

    fun createShift(startTime: Long, endTime: Long, date: String, dayOfTheWeek: String, redDay: String, holiday: String?
    ) {

        var newShift = DatabaseModel.SingleShift2()

        if (redDay == "Ja") { newShift.weekday = dayType.holidayDay }
        if (holiday == null && redDay == "Nej") {
            if (dayOfTheWeek == "Lördag") { newShift.weekday = dayType.holidayEve
            } else { newShift.weekday = dayType.notHoliday }
        }
        if (holiday != null && redDay == "Nej") { newShift.weekday = dayType.holidayEve }

        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
        val dateString = simpleDateFormat.format(startTime)

        launch(Dispatchers.IO) {
            newShift.shiftDuration = (endTime - startTime) / 1000 / 60
            newShift.startTime = startTime
            newShift.endTime = endTime
            newShift.breakTime = newShift.calcBreakTime()
            newShift.dayOfTheWeek = dayOfTheWeek
            newShift.shiftEarnings = newShift.getShiftEarnings(hourlyWage.toDouble())

            if(handelsOrRestaurang == 2){
                newShift.obEarnings = newShift.getOBHoursRest()
            } else{
                newShift.obEarnings = newShift.getOBHoursHandels(hourlyWage.toDouble())
            }
            newShift.date = dateString
            newShift.readableTime = newShift.getReadableTimePeriod(
                startMonth,
                startDay,
                startHour,
                startMinute,
                endHour,
                endMinute
            )


            database.shiftDB.ShiftDao().insertAll(newShift)
            Log.d("timmydebug", newShift.toString())
            getAllShifts()
        }
    }
}

class Factory(val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShiftsModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShiftsModel(app) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}