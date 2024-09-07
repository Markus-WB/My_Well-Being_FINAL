package com.example.mywell_being

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class MoodDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_MOOD_RESULTS)
        insertPredefinedMoodResultsIntoDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOOD_RESULTS")
        onCreate(db)
    }

    fun insertMoodResult(mood: Int): Long {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MOOD, mood)
            put(COLUMN_DATE, currentDate)
        }
        return db.insert(TABLE_MOOD_RESULTS, null, values)
    }

    fun getAllMoodResults(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_MOOD_RESULTS", null)
    }
    private fun insertPredefinedMoodResultsIntoDatabase(db: SQLiteDatabase) {
        val moodData = listOf(
            MoodResult(1, 3, "2024-06-02"),
            MoodResult(2, 4, "2024-06-03"),
            MoodResult(3, 2, "2024-06-04"),
            MoodResult(4, 5, "2024-06-05"),
            MoodResult(5, 3, "2024-06-06"),
            MoodResult(6, 4, "2024-06-07"),
            MoodResult(7, 3, "2024-06-08")
        )
        db.beginTransaction()
        try {
            for (mood in moodData) {
                val values = ContentValues().apply {
                    put(COLUMN_MOOD, mood.mood)
                    put(COLUMN_DATE, mood.date)
                }
                db.insert(TABLE_MOOD_RESULTS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "mood_results_database"
        private const val TABLE_MOOD_RESULTS = "mood_results"
        internal const val COLUMN_ID = "id"
        internal const val COLUMN_MOOD = "mood"
        internal const val COLUMN_DATE = "date"
        private const val CREATE_TABLE_MOOD_RESULTS = ("CREATE TABLE "
                + TABLE_MOOD_RESULTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MOOD + " INTEGER,"
                + COLUMN_DATE + " TEXT"
                + ")")
    }
}
