package com.example.mywell_being

import android.app.DatePickerDialog
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.example.mywell_being.MoodDBHelper.Companion.COLUMN_MOOD
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class HistoryActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnExportCSV: Button

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val testResults = mutableListOf<TestResult>()
    private val moodResults = mutableListOf<MoodResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        lineChart = findViewById(R.id.lineChart)
        btnStartDate = findViewById(R.id.btn_start_date)
        btnEndDate = findViewById(R.id.btn_end_date)
        btnExportCSV = findViewById(R.id.btn_export_csv)
        loadTestResults()
        loadMoodResults()

        // Obtener la fecha actual y restarle 6 días para la fecha de inicio
        val startDateCalendar = Calendar.getInstance()
        startDateCalendar.add(Calendar.DAY_OF_MONTH, -6)
        btnStartDate.text = dateFormat.format(startDateCalendar.time)

        // La fecha de fin será la fecha actual
        val endDateCalendar = Calendar.getInstance()
        btnEndDate.text = dateFormat.format(endDateCalendar.time)

        btnStartDate.setOnClickListener { showDatePicker(btnStartDate) }
        btnEndDate.setOnClickListener { showDatePicker(btnEndDate) }
        btnExportCSV.setOnClickListener { exportChartToCSV() }

        setupChart()
        setupLimitLines()
        filterAndDisplayData()
    }

    private fun showDatePicker(button: Button) {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            button.text = dateFormat.format(calendar.time)
            filterAndDisplayData()
        }

        DatePickerDialog(
            this,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    private fun saveCSVToFile(testResultsCSV: String, moodResultsCSV: String, fileName: String) {
        try {
            val storageDir = getExternalFilesDir(null) // Directorio de almacenamiento externo de la aplicación
            val filePath = File(storageDir, fileName) // Crear el archivo en el directorio de almacenamiento externo
            val fileOutputStream = FileOutputStream(filePath)
            fileOutputStream.write((testResultsCSV + moodResultsCSV).toByteArray())
            fileOutputStream.close()

            // Mostrar el nombre del archivo en el primer Toast
            Toast.makeText(this, "Archivo CSV exportado: $fileName", Toast.LENGTH_SHORT).show()

            // Mostrar la ruta en el segundo Toast
            Toast.makeText(this, "Ha sido guardado en: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error exportando archivos CSV", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    // Permiso otorgado, procede con la exportación del archivo CSV
                    exportChartToCSV()
                } else {
                    // Permiso denegado, muestra un mensaje de que la exportación no puede continuar
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun exportChartDataToCSV(data: List<TestResult>, startDate: String, endDate: String): String {
        val filteredResults = data.filter { it.date in startDate..endDate }
        val minValue = filteredResults.minByOrNull { it.result }?.result ?: 0

        val csvContent = StringBuilder()
        csvContent.append("Date,Test Result\n")
        filteredResults.forEach { testResult ->
            val adjustedValue = testResult.result - minValue
            csvContent.append("${testResult.date},$adjustedValue\n")
        }
        return csvContent.toString()
    }

    private fun exportMoodDataToCSV(data: Cursor?, startDate: String, endDate: String): String {
        val csvContent = StringBuilder()
        csvContent.append("Date,Mood\n")

        data?.use { cursor ->
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val mood = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOOD))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                if (date in startDate..endDate) {
                    csvContent.append("$date,$mood\n")
                }
                cursor.moveToNext()
            }
        }
        return csvContent.toString()
    }

    private fun exportChartToCSV() {
        val startDate = btnStartDate.text.toString()
        val endDate = btnEndDate.text.toString()
        val testResultsCSV = exportChartDataToCSV(testResults, startDate, endDate)
        val moodDBHelper = MoodDBHelper(this)
        val moodResultsCursor = moodDBHelper.getAllMoodResults()
        val moodResultsCSV = exportMoodDataToCSV(moodResultsCursor, startDate, endDate)

        val csvFileName = "Resultados_${startDate}_$endDate.csv"
        saveCSVToFile(testResultsCSV, moodResultsCSV, csvFileName)
    }

    private fun setupChart() {
        lineChart.description = null
        lineChart.axisLeft.axisMinimum = 0f // Establecer el valor mínimo del eje y en 0
        val entries = ArrayList<Entry>()
        // Populate entries with test data
        val dataSet = LineDataSet(entries, "Test Results")
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun filterAndDisplayData() {
        val startDate = btnStartDate.text.toString()
        val endDate = btnEndDate.text.toString()

        val filteredResults = testResults.filter { it.date in startDate..endDate }

        // Update LineChart with filtered results
        updateLineChart(filteredResults)
    }

    private fun updateLineChart(data: List<TestResult>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>() // Lista para almacenar las etiquetas de fecha en formato dd/MM
        data.forEachIndexed { index, testResult ->
            entries.add(Entry(index.toFloat(), testResult.result.toFloat()))
            // Formatear la fecha a dd/MM y agregarla a la lista de etiquetas
            val formattedDate =
                dateFormat.parse(testResult.date)
                    ?.let { SimpleDateFormat("dd/MM", Locale.getDefault()).format(it) }
            if (formattedDate != null) {
                labels.add(formattedDate)
            }
        }

        val dataSet = LineDataSet(entries, "Resultado Test") // Cambiar el nombre de la serie
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Configurar el eje X con las etiquetas de fecha
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.invalidate()
    }

    private fun setupLimitLines() {
        val yAxis = lineChart.axisLeft
        yAxis.addLimitLine(LimitLine(1f, "Ansiedad muy baja").apply {
            lineColor = resources.getColor(android.R.color.holo_orange_light)
            lineWidth = 2f
        })
        yAxis.addLimitLine(LimitLine(22f, "Ansiedad Moderada").apply {
            lineColor = resources.getColor(android.R.color.holo_orange_dark)
            lineWidth = 2f
        })
        yAxis.addLimitLine(LimitLine(36f, "Ansiedad severa").apply {
            lineColor = resources.getColor(android.R.color.holo_red_dark)
            lineWidth = 2f
        })
    }

    private fun loadTestResults() {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            TABLE_TEST_RESULTS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        testResults.clear() // Limpiar la lista antes de cargar los resultados

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
                val result = getInt(getColumnIndexOrThrow(COLUMN_RESULT))
                val date = getString(getColumnIndexOrThrow(COLUMN_DATE))
                testResults.add(TestResult(id, result, date))
            }
        }
        cursor.close()
    }

    private fun loadMoodResults() {
        val moodDBHelper = MoodDBHelper(this)
        val moodResultsCursor = moodDBHelper.getAllMoodResults()

        moodResults.clear() // Limpiar la lista antes de cargar los resultados

        moodResultsCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(MoodDBHelper.COLUMN_ID))
                val mood = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOOD))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(MoodDBHelper.COLUMN_DATE))
                moodResults.add(MoodResult(id, mood, date))
            }
        }
    }

    companion object {
        // Constantes para acceder a la base de datos
        private const val TABLE_TEST_RESULTS = "test_results"
        private const val COLUMN_ID = "id"
        private const val COLUMN_RESULT = "result"
        private const val COLUMN_DATE = "date"
        private const val REQUEST_STORAGE_PERMISSION = 101
    }
}

