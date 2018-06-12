package io.livstream.model

import android.app.Activity
import android.widget.*
import io.livstream.LivActivity
import io.livstream.R
import java.io.Serializable

data class Liv (
        val title: String,
        val importance: Int = 5,
        val startDate: String = "",
        val endDate: String = "",
        val units: Int = 1,
        val unitDurationMins: Int = 60,
        val unitFrequency: Int = 0,
        val during: Array<LivDuring>

) : Serializable {
    companion object {
        fun createFromView(activity: Activity) : Liv? {
            val tableLayout = activity.findViewById<TableLayout>(R.id.livDuringTable);
            val duringArray:Array<LivDuring> = Array(tableLayout.childCount, {
                val rowView = tableLayout.getChildAt(it) as TableRow
                val fromVal = rowView.getChildAt(1) as TextView
                val toVal = rowView.getChildAt(3) as TextView
                val dowVal = rowView.getChildAt(5) as TextView
                LivDuring(
                fromTime = fromVal.text.toString(),
                toTime = toVal.text.toString(),
                dow = dowVal.text.toString()
                )
            })

            return Liv(
                    title = activity.findViewById<EditText>(R.id.livTitle).text.toString(),
                    importance = activity.findViewById<SeekBar>(R.id.livImportance).progress,
                    startDate = activity.findViewById<EditText>(R.id.livStart).text.toString(),
                    endDate = activity.findViewById<EditText>(R.id.livEnd).text.toString(),
                    units = activity.findViewById<EditText>(R.id.livRepeat).text.toString().toIntOrNull()
                            ?: 1,
                    unitDurationMins = activity.findViewById<EditText>(R.id.livDuration).text.toString().toIntOrNull()
                            ?: 60,
                    unitFrequency = activity.findViewById<Spinner>(R.id.livPeriod).selectedItemPosition,
                    during = duringArray
            )
        }
        fun writeToView(liv: Liv, activity: LivActivity) {
            activity.findViewById<EditText>(R.id.livTitle).setText(liv.title)
            activity.findViewById<SeekBar>(R.id.livImportance).progress = liv.importance
            activity.findViewById<EditText>(R.id.livStart).setText(liv.startDate)
            activity.findViewById<EditText>(R.id.livEnd).setText(liv.endDate)
            activity.findViewById<EditText>(R.id.livRepeat).setText(liv.units.toString())
            activity.findViewById<EditText>(R.id.livDuration).setText(liv.unitDurationMins.toString())
            activity.findViewById<Spinner>(R.id.livPeriod).setSelection(liv.unitFrequency)
            initDuringTable(activity.findViewById<TableLayout>(R.id.livDuringTable), liv.during, activity)
        }

        private fun initDuringTable(tableLayout: TableLayout, duringArray: Array<LivDuring>, activity: LivActivity) {
            val context = activity.applicationContext
            duringArray.forEach { d ->
                activity.addDuringRow(d.fromTime, d.toTime, d.dow, tableLayout, context)
            }
        }
    }
}