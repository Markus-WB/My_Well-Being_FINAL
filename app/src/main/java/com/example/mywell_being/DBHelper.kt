package com.example.mywell_being

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_TEST_RESULTS)
        insertPredefinedTestResultsIntoDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TEST_RESULTS")
        onCreate(db)
    }

    fun insertTestResult(result: Int, date: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RESULT, result)
            put(COLUMN_DATE, date)
        }
        return db.insert(TABLE_TEST_RESULTS, null, values)
    }

    private fun insertPredefinedTestResultsIntoDatabase(db: SQLiteDatabase) {
        val testData = listOf(
            TestResult(1, 25, "2024-06-02"),
            TestResult(2, 30, "2024-06-03"),
            TestResult(3, 20, "2024-06-04"),
            TestResult(4, 35, "2024-06-05"),
            TestResult(5, 28, "2024-06-06"),
            TestResult(6, 32, "2024-06-07"),
            TestResult(7, 27, "2024-06-08")
        )
        db.beginTransaction()
        try {
            for (result in testData) {
                val values = ContentValues().apply {
                    put(COLUMN_RESULT, result.result)
                    put(COLUMN_DATE, result.date)
                }
                db.insert(TABLE_TEST_RESULTS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "test_results_database"
        private const val TABLE_TEST_RESULTS = "test_results"
        private const val COLUMN_ID = "id"
        private const val COLUMN_RESULT = "result"
        private const val COLUMN_DATE = "date"
        private const val CREATE_TABLE_TEST_RESULTS = ("CREATE TABLE "
                + TABLE_TEST_RESULTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_RESULT + " INTEGER,"
                + COLUMN_DATE + " TEXT"
                + ")")
    }
}

