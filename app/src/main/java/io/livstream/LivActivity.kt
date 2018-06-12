package io.livstream

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.*
import io.livstream.model.Liv
import java.io.Serializable
import java.util.*
import android.widget.CheckBox






class LivActivity : AppCompatActivity() {

    val myCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_liv)

        if (intent.hasExtra("liv_obj")) {
            val liv = intent.getSerializableExtra("liv_obj") as Liv
            Liv.writeToView(liv, this)
        }

        val startDateEditText = findViewById<View>(R.id.livStart) as EditText
        initializeDatePicker(startDateEditText, myCalendar)

        val endDateEditText = findViewById<View>(R.id.livEnd) as EditText
        initializeDatePicker(endDateEditText, myCalendar)

        val context = applicationContext
        val tableLayout = findViewById<TableLayout>(R.id.livDuringTable);
        val addButton = findViewById<Button>(R.id.addButton);
        val removeButton = findViewById<Button>(R.id.removeButton);

        addButton.setOnClickListener { v ->
            addDuringRow("From...", "To...", getString(R.string.any_day), tableLayout, context)
        }

        removeButton.setOnClickListener { v ->
            val rowCount = tableLayout.childCount

            // Save delete row number list.
            val deleteRowNumberList = ArrayList<Int>()

            for (i in 0 until rowCount) {
                // Get table row.
                val rowView = tableLayout.getChildAt(i)
                if (rowView is TableRow) {
                    val columnView = rowView.getChildAt(0)
                    if (columnView is CheckBox) {
                        if (columnView.isChecked) {
                            deleteRowNumberList.add(i)
                        }
                    }
                }
            }
            // Remove all rows by the selected row number.
            var numRemoved = 0
            for (rowNumber in deleteRowNumberList) {
                tableLayout.removeViewAt(rowNumber-numRemoved)
                numRemoved++
            }
        }
    }

    fun addDuringRow(fromVal: String, toVal: String, dow: String, tableLayout: TableLayout, context: Context) {
        // Create a new table row.
        val tableRow = TableRow(context)
        // Set new table row layout parameters.
        tableRow.setLayoutParams(TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT))

        val checkBox = CheckBox(context)
        tableRow.addView(checkBox, 0)

        val textFromView = TextView(context)
        textFromView.text = fromVal
        textFromView.setPadding(3, 3, 3, 3);
        initializeTimePicker(textFromView, myCalendar)
        tableRow.addView(textFromView, 1)

        val hyphenView = TextView(context)
        hyphenView.text = "➜ "
        hyphenView.setPadding(8, 3, 16, 3);
        tableRow.addView(hyphenView, 2)

        val textToView = TextView(context)
        textToView.text = toVal
        textFromView.setPadding(3, 3, 3, 3);
        initializeTimePicker(textToView, myCalendar)
        tableRow.addView(textToView, 3)

        val spaceView = TextView(context)
        spaceView.text = "  "
        spaceView.setPadding(8, 3, 16, 3);
        tableRow.addView(spaceView, 4)

        val textDowView = TextView(context)
        textDowView.text = dow
        textDowView.setPadding(3, 3, 3, 3);
        initializeDowPicker(textDowView)
        tableRow.addView(textDowView, 5)

        tableLayout.addView(tableRow)
    }

    private fun initializeTimePicker(timeText: TextView, myCalendar: Calendar) {
        val timeListener = TimePickerDialog.OnTimeSetListener { view, hour: Int, min: Int ->
            timeText.setText(java.lang.String.format("%02d:%02d", hour, min))
        }
        timeText.setOnClickListener { v ->
            var timeList: List<String> = timeText.text.split(":")
            TimePickerDialog(this, timeListener,
                    if (timeList.size != 2) myCalendar.get(Calendar.HOUR_OF_DAY) else timeList[0].toInt(),
                    if (timeList.size != 2) myCalendar.get(Calendar.MINUTE) else timeList[1].toInt(),
                    false).show()
        }
    }

    private fun initializeDatePicker(dateEditText: EditText, myCalendar: Calendar) {
        val dateListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            dateEditText.setText("${monthOfYear+1}/$dayOfMonth/$year")
        }
        dateEditText.setOnClickListener { v ->
            var dateList: List<String> = ArrayList()
            if (dateEditText.text != null) {
                dateList = dateEditText.text.split("/")
            }
            DatePickerDialog(this, dateListener,
                    if (dateList.size != 3) myCalendar.get(Calendar.YEAR) else dateList[2].toInt(),
                    if (dateList.size != 3) myCalendar.get(Calendar.MONTH) else dateList[0].toInt() - 1,
                    if (dateList.size != 3) myCalendar.get(Calendar.DAY_OF_MONTH) else dateList[1].toInt()).show()
        }
        dateEditText.setOnLongClickListener {
            dateEditText.text.clear()
            true
        }
    }

    private fun initializeDowPicker(dowText: TextView) {
        val dowArray = BooleanArray(7)
        dowText.text.split(",").forEach { s ->
            if (!s.equals(getString(R.string.any_day))) {
                getResources().getStringArray(R.array.days_of_week).forEachIndexed { i, dow -> if (dow.startsWith(s)) dowArray[i] = true }
            }
        }
        dowText.setOnClickListener { v ->
            val mSelectedItems = ArrayList<String>() // Where we track the selected items
            val builder = AlertDialog.Builder(this)
            // Set the dialog title
            builder.setTitle(R.string.pick_dow)
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setMultiChoiceItems(R.array.days_of_week, dowArray) { _, which, isChecked ->
                        dowArray[which] = isChecked
                    }

                    .setPositiveButton(R.string.ok) { _, which ->
                        val n = dowArray.filter { b -> b}.count()
                        if (n == 0 || n == dowArray.size) {
                            dowText.text = getString(R.string.any_day)
                        } else {
                            dowText.text = getResources().getStringArray(R.array.days_of_week).filterIndexed { i, _ ->
                                dowArray[i]
                            }.map {
                                dow -> dow.substring(0, 3)
                            }.joinToString(",")
                        }
                    }

            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        println("onOptionsItemSelected")
        if (item?.itemId == 16908332) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        println("onBackPressed")
        val data = Intent()
        if (findViewById<EditText>(R.id.livTitle).text.toString().trim().length > 0) {
            data.putExtra("liv_obj", Liv.createFromView(this) as Serializable)
            setResult(Activity.RESULT_OK, data)
        } else {
            setResult(Activity.RESULT_CANCELED, data)
        }
        finish()
    }

}
