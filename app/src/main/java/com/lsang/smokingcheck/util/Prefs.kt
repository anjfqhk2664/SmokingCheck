package com.lsang.smokingcheck.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

object Prefs {
    private const val FILE = "smoking_prefs"
    private const val KEY_ACTIVE = "active"
    private const val KEY_ACTIVE_START = "active_start_ms"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private fun dayKey(date: LocalDate): String = "%04d%02d%02d".format(date.year, date.monthValue, date.dayOfMonth)
    private fun todayKey(): String = dayKey(LocalDate.now(ZoneId.systemDefault()))

    // per-day keys
    private fun kCount(day: String)   = "count_$day"
    private fun kTotal(day: String)   = "total_ms_$day"
    private fun kLast(day: String)    = "last_ms_$day"
    private fun kSessions(day: String)= "sessions_$day"   // JSON Array string: [{start, dur}]

    // 진행중 세션
    fun isActive(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_ACTIVE, false)
    fun getActiveStart(ctx: Context): Long = sp(ctx).getLong(KEY_ACTIVE_START, -1L)

    fun start(ctx: Context, nowMs: Long = System.currentTimeMillis()) {
        sp(ctx).edit()
            .putBoolean(KEY_ACTIVE, true)
            .putLong(KEY_ACTIVE_START, nowMs)
            .apply()
    }

    /** stop: 이번 세션 지속(ms) 반환 + 오늘 카운트/총합/세션목록 갱신 */
    fun stop(ctx: Context, nowMs: Long = System.currentTimeMillis()): Long {
        val s = sp(ctx)
        val start = s.getLong(KEY_ACTIVE_START, -1L)
        val dur = if (start > 0) (nowMs - start).coerceAtLeast(0) else 0L
        val day = todayKey()

        // counters
        val count = s.getInt(kCount(day), 0) + 1
        val total = s.getLong(kTotal(day), 0L) + dur

        // sessions append
        val arr = JSONArray(s.getString(kSessions(day), "[]"))
        val obj = JSONObject().apply {
            put("start", start)
            put("dur", dur)
        }
        arr.put(obj)

        s.edit()
            .putBoolean(KEY_ACTIVE, false)
            .putLong(KEY_ACTIVE_START, -1L)
            .putInt(kCount(day), count)
            .putLong(kTotal(day), total)
            .putLong(kLast(day), dur)
            .putString(kSessions(day), arr.toString())
            .apply()

        return dur
    }

    // ===== 오늘 통계 =====
    fun getTodayCount(ctx: Context): Int = sp(ctx).getInt(kCount(todayKey()), 0)
    fun getTodayTotalMs(ctx: Context): Long = sp(ctx).getLong(kTotal(todayKey()), 0L)
    fun getTodayAvgMs(ctx: Context): Long {
        val c = getTodayCount(ctx)
        return if (c == 0) 0L else getTodayTotalMs(ctx) / c
    }

    // ===== 특정 날짜 통계/세션 =====
    data class Session(val startMs: Long, val durMs: Long)

    fun getDayCount(ctx: Context, date: LocalDate): Int =
        sp(ctx).getInt(kCount(dayKey(date)), 0)

    fun getDayTotalMs(ctx: Context, date: LocalDate): Long =
        sp(ctx).getLong(kTotal(dayKey(date)), 0L)

    fun getDayAvgMs(ctx: Context, date: LocalDate): Long {
        val c = getDayCount(ctx, date)
        return if (c == 0) 0L else getDayTotalMs(ctx, date) / c
    }

    fun getDaySessions(ctx: Context, date: LocalDate): List<Session> {
        val arr = JSONArray(sp(ctx).getString(kSessions(dayKey(date)), "[]"))
        val out = ArrayList<Session>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Session(o.optLong("start"), o.optLong("dur")))
        }
        return out
    }
}
