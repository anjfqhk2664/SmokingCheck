package com.lsang.smokingcheck.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lsang.smokingcheck.R
import com.lsang.smokingcheck.databinding.FragmentHomeBinding
import com.lsang.smokingcheck.util.Prefs
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.math.max

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            renderElapsed()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 날짜(Idle 화면에 표시)
        b.tvDate.text = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(Date())

        // 버튼 글씨색은 검정
        b.btnTogglePill.setTextColor(Color.BLACK)
        b.btnStop.setTextColor(Color.BLACK)

        // 시작
        b.btnTogglePill.setOnClickListener {
            Prefs.start(requireContext())
            switchUi(active = true)
            startTimer()
        }

        // 종료: 내부적으로 오늘 count+1, total 갱신 + 세션 목록 추가
        b.btnStop.setOnClickListener {
            Prefs.stop(requireContext())
            stopTimer()
            switchUi(active = false)
            renderStatsAndList() // ✅ Idle로 돌아오면 카드/리스트 즉시 갱신
        }

        // 초기 상태 반영
        val active = Prefs.isActive(requireContext())
        switchUi(active)
        if (active) startTimer() else renderStatsAndList()
    }

    override fun onResume() {
        super.onResume()
        val active = Prefs.isActive(requireContext())
        if (active) {
            startTimer()
        } else {
            renderStatsAndList()
        }
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onDestroyView() {
        stopTimer()
        _b = null
        super.onDestroyView()
    }

    // ===== helpers =====

    private fun switchUi(active: Boolean) {
        b.groupActive.visibility = if (active) View.VISIBLE else View.GONE
        b.groupIdle.visibility   = if (active) View.GONE   else View.VISIBLE
        if (active) renderElapsed()
    }

    private fun startTimer() {
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    private fun stopTimer() {
        handler.removeCallbacks(tick)
    }

    /** 진행 중 경과시간 m:ss */
    private fun renderElapsed() {
        val start = Prefs.getActiveStart(requireContext())
        val now = System.currentTimeMillis()
        val ms = max(0L, now - start)
        val sec = (ms / 1000).toInt()
        val m = sec / 60
        val s = sec % 60
        b.tvElapsed.text = String.format("%d:%02d", m, s)
    }

    /** ✅ 오늘 카드값 + 상세 리스트 갱신 */
    private fun renderStatsAndList() {
        val ctx = requireContext()

        // 카드: 횟수/평균
        val count = Prefs.getTodayCount(ctx)
        b.tvCountBig.text = "${count}회"

        val avgMs = Prefs.getTodayAvgMs(ctx)
        val totalSec = (avgMs / 1000L).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        b.tvAvgMin.text = "${min}분"
        b.tvAvgSec.text = "${sec}초"

        // 오늘의 상세 기록 리스트
        b.listTodayContainer.removeAllViews()
        val sessions = Prefs.getDaySessions(ctx, LocalDate.now())
        val timeFmt = SimpleDateFormat("a hh:mm", Locale.KOREAN)

        sessions.forEach { s ->
            val item = layoutInflater.inflate(R.layout.item_session, b.listTodayContainer, false)
            val tvTime = item.findViewById<TextView>(R.id.tvStartTime)
            val tvDur  = item.findViewById<TextView>(R.id.tvDur)

            tvTime.text = timeFmt.format(Date(s.startMs))

            val durSec = (s.durMs / 1000L).toInt()
            val dm = durSec / 60
            val ds = durSec % 60
            tvDur.text = if (dm > 0) "${dm}분 ${ds}초" else "${ds}초"

            b.listTodayContainer.addView(item)
        }

        if (sessions.isEmpty()) {
            val empty = TextView(ctx).apply {
                text = "기록이 없습니다"
                setTextColor(0xFF888888.toInt())
                setPadding(16, 12, 16, 12)
            }
            b.listTodayContainer.addView(empty)
        }
    }
}
