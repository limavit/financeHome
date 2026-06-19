package com.vitor.controlefinanceiro.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

object DateUtils {
    val zone: ZoneId = ZoneId.systemDefault()

    fun today(): LocalDate = LocalDate.now(zone)

    fun nowMillis(): Long = System.currentTimeMillis()

    fun LocalDate.toMillis(): Long = atStartOfDay(zone).toInstant().toEpochMilli()

    fun millisToDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun safeDate(year: Int, month: Int, day: Int): LocalDate {
        val ym = YearMonth.of(year, month)
        return LocalDate.of(year, month, day.coerceIn(1, ym.lengthOfMonth()))
    }

    fun monthStartMillis(year: Int, month: Int): Long = LocalDate.of(year, month, 1).toMillis()

    fun monthEndMillis(year: Int, month: Int): Long {
        val ym = YearMonth.of(year, month)
        return LocalDate.of(year, month, ym.lengthOfMonth()).toMillis()
    }
}
