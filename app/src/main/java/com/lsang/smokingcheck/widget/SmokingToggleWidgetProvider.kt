package com.lsang.smokingcheck.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.lsang.smokingcheck.R
import com.lsang.smokingcheck.util.Prefs
import com.lsang.smokingcheck.util.prettyMmSs

class SmokingToggleWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_TOGGLE = "com.lsang.smokingcheck.widget.ACTION_TOGGLE"
    }

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> mgr.updateAppWidget(id, buildViews(ctx)) }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        if (intent.action == ACTION_TOGGLE) {
            if (Prefs.isActive(ctx)) {
                val dur = Prefs.stop(ctx)
                Toast.makeText(ctx, "종료: ${dur.prettyMmSs()} / 오늘 ${Prefs.getTodayCount(ctx)}회", Toast.LENGTH_SHORT).show()
            } else {
                Prefs.start(ctx)
                Toast.makeText(ctx, "시작", Toast.LENGTH_SHORT).show()
            }
            // 모든 인스턴스 갱신
            val mgr = AppWidgetManager.getInstance(ctx)
            val cn = ComponentName(ctx, SmokingToggleWidgetProvider::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            onUpdate(ctx, mgr, ids)
        }
    }

    private fun buildViews(ctx: Context): RemoteViews {
        val active = Prefs.isActive(ctx)
        val label = if (active) "흡연 종료" else "흡연 시작"

        val rv = RemoteViews(ctx.packageName, R.layout.widget_smoking_toggle)
        rv.setTextViewText(R.id.btnToggle, label)

        val intent = Intent(ctx, SmokingToggleWidgetProvider::class.java).apply { action = ACTION_TOGGLE }
        val pi = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        rv.setOnClickPendingIntent(R.id.btnToggle, pi)
        return rv
    }
}
