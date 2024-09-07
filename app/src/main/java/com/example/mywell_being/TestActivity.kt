package com.example.mywell_being

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestActivity : AppCompatActivity() {

    private lateinit var submitButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: DBHelper
    private var radioGroups = arrayListOf<RadioGroup>()
    private var totalResult: Int = 0

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        db = DBHelper(this)
        sharedPreferences = getSharedPreferences("MyWellBeingPrefs", MODE_PRIVATE)
        submitButton = findViewById(R.id.submit_button)

        // Add references to all RadioGroups
        for (i in 1..21) {
            val radioGroupId = resources.getIdentifier("radioGroup$i", "id", packageName)
            val radioGroup = findViewById<RadioGroup>(radioGroupId)
            radioGroups.add(radioGroup)
        }

        submitButton.setOnClickListener {
            if (areAllQuestionsAnswered()) {
                calculateTotalResult()
                saveTestResults()
            } else {
                Toast.makeText(this, "Responde a todas las preguntas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun areAllQuestionsAnswered(): Boolean {
        return radioGroups.none { radioGroup ->
            radioGroup.checkedRadioButtonId == -1
        }
    }


    private fun calculateTotalResult() {
        totalResult = 0
        radioGroups.forEach { radioGroup ->
            val radioButtonId = radioGroup.checkedRadioButtonId
            if (radioButtonId != -1) {
                val radioButton = findViewById<RadioButton>(radioButtonId)
                val tagValue = radioButton.tag.toString().toInt()
                if (tagValue != 0) {
                    totalResult += tagValue
                }
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun saveTestResults() {
        if (!sharedPreferences.getBoolean("TestCompleted", false)) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            GlobalScope.launch(Dispatchers.IO) {
                if (!sharedPreferences.getBoolean("TestCompleted", false)) {
                    db.insertTestResult(totalResult, currentDate)
                    sharedPreferences.edit().putBoolean("TestCompleted", true).apply()
                    showToastOnMainThread("Test registrado correctamente")
                    navigateBackToMain()
                } else {
                    showToastOnMainThread("Ya has completado el test")
                }
            }
        } else {
            showToastOnMainThread("Ya has completado el test")
        }
    }


    private fun navigateBackToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showToastOnMainThread(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
