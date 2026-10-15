package com.direkjames.sophames.reminders

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.direkjames.sophames.MainActivity
import com.direkjames.sophames.R
import com.direkjames.sophames.SophamesApp
import com.direkjames.sophames.domain.buildUpcoming
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object Reminders {
    const val CHANNEL_ID = "sophames_dates"
    private const val WORK_NAME = "sophames_daily_reminder"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Dates & anniversaries",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Gentle reminders for anniversaries, monthsaries and your own dates." }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Runs about once a day, aiming for 9:00 AM. Android may shift it a little to save battery. */
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(9, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMinutes = Duration.between(now, next).toMinutes()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val app = applicationContext as SophamesApp
        val settings = app.container.settings.settings.first()
        if (!settings.remindersEnabled) return Result.success()

        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return Result.success()

        val events = app.container.database.eventDao().getAll()
        val due = buildUpcoming(settings.startDate, LocalDate.now(), events)
            .filter { it.daysLeft == 0L || it.daysLeft == 1L }

        val tap = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        due.forEach { countdown ->
            val text = if (countdown.daysLeft == 0L) "Today: ${countdown.title} 💖"
            else "Tomorrow: ${countdown.title}"
            val notification = NotificationCompat.Builder(applicationContext, Reminders.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_heart)
                .setColor(0xFFD6567F.toInt())
                .setContentTitle("Sophames")
                .setContentText(text)
                .setContentIntent(tap)
                .setAutoCancel(true)
                .build()
            manager.notify(countdown.id.hashCode() + countdown.daysLeft.toInt(), notification)
        }
        return Result.success()
    }
}
