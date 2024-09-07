package com.example.mywell_being

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: MoodDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        db = MoodDBHelper(this)
        sharedPreferences = getSharedPreferences("MyWellBeingPrefs", MODE_PRIVATE)

        val moodButtons = listOf(
            findViewById(R.id.btn_mood_0),
            findViewById(R.id.btn_mood_1),
            findViewById(R.id.btn_mood_2),
            findViewById(R.id.btn_mood_3),
            findViewById<Button>(/* id = */ R.id.btn_mood_4)
        )

        moodButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!sharedPreferences.getBoolean("MoodCompleted", false)) {
                    saveMoodResult(index)
                } else {
                    Toast.makeText(this, "Ya has registrado tu estado de ánimo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveMoodResult(mood: Int) {
        if (!sharedPreferences.getBoolean("MoodCompleted", false)) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            GlobalScope.launch(Dispatchers.IO) {
                db.insertMoodResult(mood)
                sharedPreferences.edit().putBoolean("MoodCompleted", true).apply()
                showToastOnMainThread("Estado de ánimo registrado correctamente")
                navigateBackToMain()
            }
        } else {
            showToastOnMainThread("Ya has registrado tu estado de ánimo")
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
