package com.example.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class CooldownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userName = intent.getStringExtra("USER_NAME") ?: "Donor"
        val gender = intent.getStringExtra("USER_GENDER") ?: "Male"
        val interval = if (gender.equals("Female", ignoreCase = true)) "120" else "90"
        
        NotificationScheduler.showCooldownExpiredNotification(context, userName, interval)
    }
}

object NotificationScheduler {
    const val CHANNEL_ID = "lifestream_cooldown_alerts"
    const val NOTIFICATION_ID = 404
    
    const val SMS_CHANNEL_ID = "lifestream_sms_gateway"
    const val SMS_NOTIFICATION_ID = 505
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cooldown Expiration Alerts"
            val descriptionText = "Informs you when your donation cooldown (90 or 120 days) has expired."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createSmsNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "LifeStream SMS Gateway"
            val descriptionText = "Simulated SMS verification codes transmitted securely."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(SMS_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showSmsOtpNotification(context: Context, phone: String, otp: String) {
        createSmsNotificationChannel(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, SMS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle("SMS: OTP Sent to $phone")
            .setContentText("Your secure verification OTP is $otp. Do not share this code.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(SMS_NOTIFICATION_ID, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Failed to post SMS notification", e)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showCooldownExpiredNotification(context: Context, userName: String, intervalDays: String) {
        createNotificationChannel(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Clinical Cooldown Expired! 🩸")
            .setContentText("Hello $userName, your $intervalDays-day cooldown period has fully expired. You are now eligible to pledge and save lives again!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Failed to post notification", e)
        }
    }
    
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleCooldownAlert(context: Context, userName: String, gender: String, expirationTimeMs: Long) {
        val intent = Intent(context, CooldownReceiver::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_GENDER", gender)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        try {
            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        expirationTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        expirationTimeMs,
                        pendingIntent
                    )
                }
                Log.d("NotificationScheduler", "Scheduled exact alarm for cooldown alert at $expirationTimeMs")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        expirationTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        expirationTimeMs,
                        pendingIntent
                    )
                }
                Log.d("NotificationScheduler", "Scheduled non-exact alarm for cooldown alert at $expirationTimeMs")
            }
        } catch (e: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        expirationTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        expirationTimeMs,
                        pendingIntent
                    )
                }
                Log.d("NotificationScheduler", "Scheduled fallback alarm due to exception: ${e.message}")
            } catch (err: Exception) {
                Log.e("NotificationScheduler", "Failed to schedule any alarm", err)
            }
        }
    }

    fun cancelCooldownAlert(context: Context) {
        val intent = Intent(context, CooldownReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
