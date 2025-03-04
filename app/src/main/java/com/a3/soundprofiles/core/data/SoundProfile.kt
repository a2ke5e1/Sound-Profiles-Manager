package com.a3.soundprofiles.core.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.util.Log
import androidx.annotation.FloatRange
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.a3.soundprofiles.R
import java.util.Date

enum class DAY {
  SUNDAY,
  MONDAY,
  TUESDAY,
  WEDNESDAY,
  THURSDAY,
  FRIDAY,
  SATURDAY
}

@Entity(tableName = "sound_profiles")
data class SoundProfile(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "media_volume") @FloatRange(from = 0.0, to = 1.0) val mediaVolume: Float,
    @ColumnInfo(name = "notification_volume")
    @FloatRange(from = 0.0, to = 1.0)
    val notificationVolume: Float,
    @ColumnInfo(name = "ringer_volume") @FloatRange(from = 0.0, to = 1.0) val ringerVolume: Float,
    @ColumnInfo(name = "call_volume") @FloatRange(from = 0.0, to = 1.0) val callVolume: Float,
    @ColumnInfo(name = "alarm_volume") @FloatRange(from = 0.0, to = 1.0) val alarmVolume: Float,
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "repeat_everyday") val repeatEveryday: Boolean,
    @ColumnInfo(name = "repeat_days") val repeatDays: List<DAY>
) {
  fun applyProfile(context: Context) {
    val volumeSettings = this
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val minMediaVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
    val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val minRingerVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_RING)
    val maxRingerVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
    val minCallVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_VOICE_CALL)
    val maxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
    val minAlarmVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
    val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
    val minNotificationVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_NOTIFICATION)
    val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)

    val mediaVolume =
        (volumeSettings.mediaVolume * (maxMediaVolume - minMediaVolume) + minMediaVolume).toInt()
    val ringerVolume =
        (volumeSettings.ringerVolume * (maxRingerVolume - minRingerVolume) + minRingerVolume)
            .toInt()
    val callVolume =
        (volumeSettings.callVolume * (maxCallVolume - minCallVolume) + minCallVolume).toInt()
    val alarmVolume =
        (volumeSettings.alarmVolume * (maxAlarmVolume - minAlarmVolume) + minAlarmVolume).toInt()
    val notificationVolume =
        (volumeSettings.notificationVolume * (maxNotificationVolume - minNotificationVolume) +
                minNotificationVolume)
            .toInt()

    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolume, 0)
    audioManager.setStreamVolume(AudioManager.STREAM_RING, ringerVolume, 0)
    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0)
    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolume, 0)
    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationVolume, 0)

    sendNotification(
        context,
        this.id,
        context.getString(R.string.profile_applied),
        context.getString(R.string.profile_applied_message, this.title))
    Log.d("SoundProfile", context.getString(R.string.profile_applied_message, this.title))
  }

  companion object {
    fun sendNotification(context: Context, notificationId: Int, title: String, message: String) {
      val channelId = "profile_status_channel"
      val channelName = context.getString(R.string.profile_status_notifications)

      // Create the notification channel if it doesn't exist
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel =
          NotificationChannel(channelId, channelName, importance).apply {
            description = context.getString(R.string.notifications_for_profile_application)
          }
      val notificationManager: NotificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)

      // Build the notification
      val builder =
          NotificationCompat.Builder(context, channelId)
              .setSmallIcon(
                  R.drawable.app_notification_icon) // Replace with your notification icon
              .setContentTitle(title)
              .setContentText(message)
              .setPriority(NotificationCompat.PRIORITY_DEFAULT)

      // Send the notification
      with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
          return
        }
        notify(notificationId, builder.build())
      }
    }
  }
}
