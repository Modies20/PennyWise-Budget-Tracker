// File: com/pennywise/budgettracker/utils/DateUtils.kt
// Reference: Java Community (2024) 'Calendar and Date handling best practices'

package com.pennywise.budgettracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun getStartOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
    fun getEndOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
    fun now(): Long = System.currentTimeMillis()
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
}