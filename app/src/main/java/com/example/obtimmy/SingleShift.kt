package com.example.obtimmy

import android.util.Log
import java.util.*




data class SingleShift(var weekday: dayType? = null,
                       var startTime: Long? = null,
                       var endTime: Long? = null,
                       var shiftDuration: Long? = null,
                       var shiftEarnings: Double? = null,
                       var obEarnings: Double? = null,
                       var dayOfTheWeek: String? = null,
                       var date: String? = null

){

    fun getShiftEarnings(hourlyWage : Double) : Double {
        var diffTime = (endTime!!-startTime!!)/1000

        // Diff time är tiden man jobbar i sekunder

        var diffTimeInMinutes = diffTime / 60
        val diffTimeInHours = diffTime / 3600
        val minutesMinusHours = diffTimeInMinutes - (diffTimeInHours * 60)

        Log.d("timmydebug", "Timmar: " + diffTimeInHours + " Minuter: " + minutesMinusHours)

        var minuteWage = hourlyWage.toDouble()/60
        var earnings = (diffTimeInHours * hourlyWage) + (minutesMinusHours * minuteWage )

        Log.d("timmydebug", "Timlön för " + diffTimeInHours + " timmar + " + minutesMinusHours + " minuter blir " + earnings + "kr")

        return earnings
    }

    fun getOBHoursHandels(hourlyWage: Double) : Double {


        var minuteWage = hourlyWage.toDouble()/60

        //var shift = SingleShift(dayType.holidayDay, 1201239, 12031093, 132131, 13.0, 0.0, "Måndag")

        val cal = Calendar.getInstance()
        cal.timeInMillis = startTime!!

        val currentYear = cal[Calendar.YEAR]
        val currentMonth = cal[Calendar.MONTH]
        val currentDay = cal[Calendar.DAY_OF_MONTH]


        if (weekday == dayType.holidayDay) {
            var extraWage = shiftEarnings

            Log.d("timmydebug", "OB earnings are: " + extraWage)
            return extraWage!!
            // På en söndag/helgdag så räknas varje timme dubbelt. Därför blir extra wage samma som shiftEarnings.

        }

        // Holiday Eve
        if (weekday == dayType.holidayEve) {

            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, currentYear)
            cal.set(Calendar.MONTH, currentMonth)
            cal.set(Calendar.DAY_OF_MONTH, currentDay)
            cal.set(Calendar.HOUR_OF_DAY, 12)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val OBtimeStamp = cal.timeInMillis

            var difference = 0L

            if(OBtimeStamp < startTime!!){
                difference = (endTime!! - startTime!!) /1000

            } else {
                difference = (endTime!!-OBtimeStamp) / 1000
            }

            Log.d("timmydebug", "OBTimeStamp är : " + OBtimeStamp.toString())
            Log.d("timmydebug", "OB-seconds are: " + difference.toString())

            val OBHours = difference / 3600
            difference = difference - OBHours * 3600
            val OBMinutes = difference / 60

            Log.d("timmydebug", "OB-hours are: " + OBHours.toString())
            Log.d("timmydebug", "OB-minutes are: " + OBMinutes.toString())

            var extraWage = (OBHours * hourlyWage + OBMinutes * minuteWage).toDouble()

            Log.d("timmydebug", "OB earnings are: " + extraWage)
            return extraWage

            // På en lördag ska man få dubbel lön efter klockan tolv. Därför räknas timmar och minuter efter kl 12 två gånger.

        }


        // Vardag
        if(weekday == dayType.notHoliday){
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, currentDay)
            cal.set(Calendar.HOUR_OF_DAY, 18)
            cal.set(Calendar.MINUTE, 15)

            val OBtimeStamp = cal.time.time
            var difference = (endTime!!-OBtimeStamp) / 1000

            if(difference < 0){
                // PERSONEN FÅR INGEN OB
                return 0.0

            } else {
                val OBHours = difference / 3600
                difference = difference - OBHours * 3600
                val OBMinutes = difference / 60

                var extraWage = (OBHours * hourlyWage + OBMinutes * minuteWage)/2.toDouble()

                Log.d("timmydebug", "OB-hours are: " + OBHours.toString())
                Log.d("timmydebug", "OB-minutes are: " + OBMinutes.toString())
                Log.d("timmydebug", "OB earnings are: " + extraWage)
                return extraWage
                // En vardag tjänar man 50% extra efter kl 18.15.

            }
        } else {
            // Om inget stämmer in, ge ingen OB, något har blivit fel
            return 0.0
        }

    }

    fun getOBHoursRest() : Double

    {
        val cal = Calendar.getInstance()
        cal.time.time = startTime!!
        val currentDay = cal[Calendar.DAY_OF_MONTH]


        if(weekday == dayType.notHoliday){
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, currentDay)
            cal.set(Calendar.HOUR_OF_DAY, 20)
            cal.set(Calendar.MINUTE, 0)

            val OBtimeStamp = cal.time.time
            var difference = (endTime!!-OBtimeStamp) / 1000

            if(difference < 0){
                // PERSONEN FÅR INGEN OB

            } else {

            }
            val OBHalfHours = difference / 1800 + 1
            val extraWage = OBHalfHours * 11.75

            return extraWage

            // En vardag tjänar man 11.75kr för varje påbörjad halvtimme
        }
        if(weekday == dayType.holidayEve){

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, currentDay)
            cal.set(Calendar.HOUR_OF_DAY, 16)
            cal.set(Calendar.MINUTE, 0)

            val OBtimeStamp = cal.time.time
            var difference = (endTime!!-OBtimeStamp) / 1000

            if(difference < 0){
                // PERSONEN FÅR INGEN OB
                return 0.0

            } else {

                val OBHalfHours = difference / 1800 + 1
                val extraWage = OBHalfHours * 11.75

                return extraWage

                // En helgafton tjänar man 11.75kr för varje påbörjad halvtimme från kl 16
            }
        }
        if(weekday == dayType.holidayDay){

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, currentDay)
            cal.set(Calendar.HOUR_OF_DAY, 6)
            cal.set(Calendar.MINUTE, 0)

            var diffTime = (endTime!!-startTime!!)/1000

            // Diff time är tiden man jobbar i sekunder

            val workingHalfHours = diffTime / 1800 + 1
            val extraWage = workingHalfHours * 11.75

            return extraWage

            // En helgdag tjänar man 11.75kr för varje påbörjad halvtimme
        }

        return 0.0
    }

}




    /*

Måndag–fredag 18.15–20.00      50%
Måndag–fredag efter 20.00      70%
Lördagar efter 12.00           100%
Helgdagar                      100%

     */

/*

23,53 kr

Måndag – fredag från kl. 20.00 till kl. 06.00 påföljande dag.
Lördag, midsommar-, jul- och nyårsafton från kl. 16.00 till kl. 06.00 påföljande dag.
Söndag och helgdag från kl. 06.00 till kl. 06.00 påföljande dag.

 */


