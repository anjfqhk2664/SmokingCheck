package com.lsang.smokingcheck.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lsang.smokingcheck.databinding.FragmentStatsBinding
import com.lsang.smokingcheck.util.Prefs
import com.lsang.smokingcheck.util.prettyMmSs
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.max

class StatsFragment : Fragment() {
    private var _b: FragmentStatsBinding? = null
    private val b get() = _b!!

    private val zone: ZoneId by lazy { ZoneId.systemDefault() }
    private var pickedDate: LocalDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentStatsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Tab UI: 일별만 동작(주/월은 UI용)
        b.tabLayout.getTabAt(0)?.select()

        // CalendarView 초기값을 오늘로
        val todayMs = Date().time
        b.calendar.date = todayMs

        // 날짜 선택 리스너
        b.calendar.setOnDateChangeListener { _: CalendarView, year, month, dayOfMonth ->
            pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
            renderDay()
        }

        // 최초 렌더
        renderDay()
    }

    private fun renderDay() {
        val ctx = requireContext()

        // count/avg/total
        val count = Prefs.getDayCount(ctx, pickedDate)
        val totalMs = Prefs.getDayTotalMs(ctx, pickedDate)
        val avgMs = Prefs.getDayAvgMs(ctx, pickedDate)

        b.tvCount.text = "${count}회"
        b.tvTotal.text = "${(totalMs / 60000L)}분" // 총 시간: 분 단위 간단 표기
        val avgSec = (avgMs / 1000L).toInt()
        val avgMin = avgSec / 60
        val avgS = avgSec % 60
        b.tvAvg.text = "${avgMin}분"

        // 상세 리스트
        b.listContainer.removeAllViews()
        val sessions = Prefs.getDaySessions(ctx, pickedDate)
        val timeFmt = SimpleDateFormat("a hh:mm", Locale.KOREAN)

        sessions.forEach { s ->
            // item_session inflate
            val item = layoutInflater.inflate(
                com.lsang.smokingcheck.R.layout.item_session,
                b.listContainer,
                false
            )
            val tvTime = item.findViewById<TextView>(com.lsang.smokingcheck.R.id.tvStartTime)
            val tvDur = item.findViewById<TextView>(com.lsang.smokingcheck.R.id.tvDur)

            // start time
            val date = Date(s.startMs)
            tvTime.text = timeFmt.format(date)

            // duration -> "m분 s초"
            val durMs = max(0L, s.durMs)
            val totalSec = (durMs / 1000L).toInt()
            val m = totalSec / 60
            val sec = totalSec % 60
            tvDur.text = if (m > 0) "${m}분 ${sec}초" else "${sec}초"

            b.listContainer.addView(item)
        }

        // 리스트가 비어있을 때 한 줄 표시(선택)
        if (sessions.isEmpty()) {
            val empty = TextView(ctx).apply {
                text = "기록이 없습니다"
                setTextColor(0xFF888888.toInt())
                setPadding(16, 12, 16, 12)
            }
            b.listContainer.addView(empty)
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
