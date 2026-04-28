// File: com/pennywise/budgettracker/utils/DateUtils.kt
// Reference: Java Community (2024) 'Calendar and Date handling best practices'

package com.pennywise.budgettracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    /**
     * Convert timestamp to formatted date string
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Convert timestamp to formatted date-time string
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * Get start of month timestamp
     */
    fun getStartOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of month timestamp
     */
    fun getEndOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Get start of day timestamp
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of day timestamp
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Get current timestamp
     */
    fun now(): Long = System.currentTimeMillis()

    /**
     * Get current year and month
     */
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
}