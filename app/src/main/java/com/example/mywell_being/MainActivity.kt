package com.example.mywell_being

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnTestBAI: Button
    private lateinit var btnMood: Button
    private lateinit var btnHistory: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTestBAI = findViewById(R.id.btn_test_bai)
        btnMood = findViewById(R.id.btn_mood)
        btnHistory = findViewById(R.id.btn_history)
        sharedPreferences = getSharedPreferences("MyWellBeingPrefs", Context.MODE_PRIVATE)

        // Verificar si el test está completado y actualizar el color del botón en consecuencia
        val isTestCompleted = sharedPreferences.getBoolean("TestCompleted", false)
        if (isTestCompleted) {
            btnTestBAI.setBackgroundColor(getColor(android.R.color.darker_gray))
        }

        // Verificar si el estado de ánimo está completado y actualizar el color del botón en consecuencia
        val isMoodCompleted = sharedPreferences.getBoolean("MoodCompleted", false)
        if (isMoodCompleted) {
            btnMood.setBackgroundColor(getColor(android.R.color.darker_gray))
        }

        // Configurar el OnClickListener para el botón de TestActivity
        btnTestBAI.setOnClickListener {
            if (isTestCompleted) {
                Toast.makeText(this, getString(R.string.test_completed), Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, TestActivity::class.java)
                startActivity(intent)
            }
        }

        // Configurar el OnClickListener para el botón de MoodActivity
        btnMood.setOnClickListener {
            if (isMoodCompleted) {
                Toast.makeText(this, getString(R.string.mood_completed), Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MoodActivity::class.java)
                startActivity(intent)
            }
        }

        // Configurar el OnClickListener para el botón de HistoryActivity
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Obtener el estado de completado del test y del estado de ánimo actualizados
        val isTestCompleted = sharedPreferences.getBoolean("TestCompleted", false)
        val isMoodCompleted = sharedPreferences.getBoolean("MoodCompleted", false)

        // Actualizar el color del botón de TestActivity según el estado de completado del test
        if (isTestCompleted) {
            btnTestBAI.setBackgroundColor(getColor(android.R.color.darker_gray))
        }

        // Actualizar el color del botón de MoodActivity según el estado de completado del estado de ánimo
        if (isMoodCompleted) {
            btnMood.setBackgroundColor(getColor(android.R.color.darker_gray))
        }
    }
}
