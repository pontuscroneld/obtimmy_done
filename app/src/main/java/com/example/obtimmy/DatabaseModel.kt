package com.example.obtimmy


import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import java.util.*

class DatabaseModel(ctx: Context)
{
    lateinit var shiftDB : ShiftDatabase
    var liveDataShiftList = MutableLiveData<List<SingleShift2>>()

    init {
        shiftDB = Room.databaseBuilder(
                ctx,
                ShiftDatabase::class.java, "shifts-database"
        ).build()
    }

    @Entity
    data class SingleShift2(
            @PrimaryKey(autoGenerate = true) val uid: Int = 0,
            @ColumnInfo(name = "start_time") var startTime: Long? = null,
            @ColumnInfo(name = "end_time") var endTime: Long? = null,
            @ColumnInfo(name = "date") var date: String? = null,
            @ColumnInfo(name = "shift_duration") var shiftDuration: Long? = null,
            @ColumnInfo(name = "day_of_the_week") var dayOfTheWeek: String? = null,
            @ColumnInfo(name = "weekday") var weekday: dayType = dayType.notHoliday,
            @ColumnInfo(name = "shift_earnings") var shiftEarnings: Double? = null,
            @ColumnInfo(name = "ob_earnings") var obEarnings: Double? = null,
            @ColumnInfo(name = "readable_time") var readableTime: String? = null,
            @ColumnInfo(name = "break_time") var breakTime: Long? = null

    ) {

        fun getShiftEarnings(hourlyWage: Double): Double {
            var diffTime = (endTime!! - startTime!!) / 1000
            diffTime = diffTime - breakTime!!
            // Diff time är tiden man jobbar i sekunder

            var diffTimeInMinutes = diffTime / 60
            val diffTimeInHours = diffTime / 3600
            val minutesMinusHours = diffTimeInMinutes - (diffTimeInHours * 60)

            Log.d("timmydebug", "Timmar: " + diffTimeInHours + " Minuter: " + minutesMinusHours)

            var minuteWage = hourlyWage.toDouble() / 60
            var earnings = (diffTimeInHours * hourlyWage) + (minutesMinusHours * minuteWage)

            Log.d("timmydebug", "Timlön för " + diffTimeInHours + " timmar + " + minutesMinusHours + " minuter blir " + earnings + "kr")

            return earnings
        }

        fun getOBHoursHandels(hourlyWage: Double): Double {

            var minuteWage = hourlyWage.toDouble() / 60

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

                if (OBtimeStamp < startTime!!) {
                    difference = (endTime!! - startTime!!) / 1000

                } else {
                    difference = (endTime!! - OBtimeStamp) / 1000
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
            if (weekday == dayType.notHoliday) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, currentDay)
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 15)

                val OBtimeStamp = cal.time.time
                var difference = (endTime!! - OBtimeStamp) / 1000

                if (difference < 0) {
                    // PERSONEN FÅR INGEN OB
                    return 0.0

                } else {
                    val OBHours = difference / 3600
                    difference = difference - OBHours * 3600
                    val OBMinutes = difference / 60

                    var extraWage = (OBHours * hourlyWage + OBMinutes * minuteWage) / 2.toDouble()

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

        fun getOBHoursRest(): Double {
            val cal = Calendar.getInstance()
            cal.time.time = startTime!!
            val currentDay = cal[Calendar.DAY_OF_MONTH]


            if (weekday == dayType.notHoliday) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, currentDay)
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)

                val OBtimeStamp = cal.time.time
                var difference = (endTime!! - OBtimeStamp) / 1000

                if (difference < 0) {
                    // PERSONEN FÅR INGEN OB

                } else {

                }
                val OBHalfHours = difference / 1800 + 1
                val extraWage = OBHalfHours * 11.75

                return extraWage

                // En vardag tjänar man 11.75kr för varje påbörjad halvtimme
            }
            if (weekday == dayType.holidayEve) {

                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, currentDay)
                cal.set(Calendar.HOUR_OF_DAY, 16)
                cal.set(Calendar.MINUTE, 0)

                val OBtimeStamp = cal.time.time
                var difference = 0L

                if (OBtimeStamp < startTime!!) {
                    difference = (endTime!! - startTime!!) / 1000

                } else {
                    difference = (endTime!! - OBtimeStamp) / 1000
                }

                if (difference < 0) {
                    // PERSONEN FÅR INGEN OB
                    return 0.0

                } else {

                    val OBHalfHours = difference / 1800 + 1
                    val extraWage = OBHalfHours * 11.75

                    return extraWage

                    // En helgafton tjänar man 11.75kr för varje påbörjad halvtimme från kl 16
                }
            }
            if (weekday == dayType.holidayDay) {

                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, currentDay)
                cal.set(Calendar.HOUR_OF_DAY, 6)
                cal.set(Calendar.MINUTE, 0)

                var diffTime = (endTime!! - startTime!!) / 1000

                // Diff time är tiden man jobbar i sekunder

                val workingHalfHours = diffTime / 1800 + 1
                val extraWage = workingHalfHours * 11.75

                return extraWage

                // En helgdag tjänar man 11.75kr för varje påbörjad halvtimme
            }

            return 0.0
        }

        fun getReadableTimePeriod(sMon: Int, sD: Int, sH: Int, sMin: Int, eH: Int, eMin: Int): String {

            var sMonString = ""
            var sDayString = ""
            var sHourString = ""
            var sMinString = ""
            var eHourString = ""
            var eMinString = ""

            if (sMon < 10) {
                sMonString = "0" + sMon.toString()
            } else {
                sMonString = sMon.toString()
            }

            if (sD < 10) {
                sDayString = "0" + sD.toString()
            } else {
                sDayString = sD.toString()
            }

            if (sH < 10) {
                sHourString = "0" + sH.toString()
            } else {
                sHourString = sH.toString()
            }

            if (sMin < 10) {
                sMinString = "0" + sMin.toString()
            } else {
                sMinString = sMin.toString()
            }

            if (eH < 10) {
                eHourString = "0" + eH.toString()
            } else {
                eHourString = eH.toString()
            }

            if (eMin < 10) {
                eMinString = "0" + eMin.toString()
            } else {
                eMinString = eMin.toString()
            }

            var readableText = sDayString + "/" + sMonString + " " + sHourString + ":" + sMinString + " - " + eHourString + ":" + eMinString
            return readableText
        }

        fun calcBreakTime(): Long {

            if (shiftDuration!! < 305) {
                return 0
            }
            if (shiftDuration!! > 540) {
                return 60
            }
            return 30
        }

    }

    @Dao
    interface ShiftDao {

        @Query("SELECT * FROM singleshift2")
        fun loadAll(): List<SingleShift2>

        @Delete
        fun delete(shoppingitem: SingleShift2)

        @Update
        fun updateShopItem(shopitem: SingleShift2)

        @Insert
        fun insertAll(shift: SingleShift2)

        @Query("DELETE FROM singleshift2")
        fun nukeTable()
    }

    @Database(entities = arrayOf(SingleShift2::class), version = 4)
    @TypeConverters(Converters::class)
    abstract class ShiftDatabase : RoomDatabase() {
        abstract fun ShiftDao(): ShiftDao
    }


}