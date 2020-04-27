package com.ahmad.stopwatch.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.ahmad.stopwatch.R
import com.ahmad.stopwatch.model.Milestone
import com.ahmad.stopwatch.utils.Constants
import com.google.android.material.button.MaterialButton
import org.joda.time.Period
import java.util.*

class SelectTimerActivity : AppCompatActivity(){

    lateinit var secondsNumberPicker:NumberPicker
    lateinit var minutesNumberPicker: NumberPicker
    lateinit var hoursNumberPicker: NumberPicker

    lateinit var nameEditText: AppCompatEditText
    lateinit var addButton:MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_timer)
        activityInit()
    }

    fun activityInit(){
        secondsNumberPicker = findViewById(R.id.np_sec)
        minutesNumberPicker = findViewById(R.id.np_min)
        hoursNumberPicker = findViewById(R.id.np_hour)
        nameEditText = findViewById(R.id.et_name)
        addButton = findViewById(R.id.btn_add)

        secondsNumberPicker.minValue = 0
        secondsNumberPicker.maxValue = 59

        minutesNumberPicker.minValue = 0
        minutesNumberPicker.maxValue = 59

        hoursNumberPicker.minValue = 0
        hoursNumberPicker.maxValue = 99

        addButton.setOnClickListener {
            val milestone = compriseMilestone()
            val returnIntent = Intent()
            returnIntent.putExtra("milestone",milestone)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    fun compriseMilestone():Milestone{
        val period = Period(hoursNumberPicker.value,minutesNumberPicker.value,secondsNumberPicker.value,0)
        return Milestone(UUID.randomUUID().toString().replace("-",""),
            if (nameEditText.text.toString().isEmpty()) {
                ""
            }else {
                nameEditText.text.toString()
            },
            period, false)
    }

    companion object{
        const val TAG = "SelectTimerActivity"
    }
}