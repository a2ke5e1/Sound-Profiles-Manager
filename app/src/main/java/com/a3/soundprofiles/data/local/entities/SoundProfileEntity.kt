package com.a3.soundprofiles.data.local.entities

import android.media.AudioManager
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

data class StreamVolume(
    val current: Int,
    val min: Int,
    val max: Int
) {
    val percent: Int
        get() = if (max > min) ((current - min) * 100 / (max - min)) else 0
}

@Entity(tableName = "sound_profiles")
data class SoundProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val iconName: String? = null,
    @Embedded(prefix = "ringer_")
    val ringerVolume: StreamVolume,
    @Embedded(prefix = "notification_")
    val notificationVolume: StreamVolume,
    @Embedded(prefix = "alarm_")
    val alarmVolume: StreamVolume,
    @Embedded(prefix = "music_")
    val musicVolume: StreamVolume,
    @Embedded(prefix = "voice_")
    val voiceVolume: StreamVolume,
    val ringerMode: Int
) {
    fun profileSummary(): String {
        val r = ringerVolume.percent
        val m = musicVolume.percent
        val a = alarmVolume.percent
        val n = notificationVolume.percent
        val v = voiceVolume.percent

        // Case: Silent / DND (Example: All volumes at 0% • Do Not Disturb active)
        if (r == 0 && m == 0 && a == 0 && n == 0 && v == 0) {
            return "All volumes at 0% • Do Not Disturb active"
        }

        // Case: Alarms Only / Sleep (Example: Alarms 100% • All other sounds muted)
        if (a > 0 && r == 0 && m == 0 && n == 0 && v == 0) {
            return "Alarms $a% • All other sounds muted"
        }

        val parts = mutableListOf<String>()

        // Decide which volume to show first based on priority (Media vs Ring)
        if (m > r && m >= 70) {
            // "Music" style: Media 80% • Ringer 50%
            parts.add("Media $m%")
            parts.add("Ringer $r%")
        } else {
            // "Work" style: Ring 20% • Media 0% • Notifications Vibrate
            parts.add("Ring $r%")
            parts.add("Media $m%")
        }

        // Add Voice if it's not at maximum (since it's usually 100%)
        if (v < 100) {
            parts.add("Voice $v%")
        }

        // Add Ringer Mode context
        when (ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> parts.add("Notifications Vibrate") // RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_SILENT -> parts.add("Silent mode")           // RINGER_MODE_SILENT
        }

        return parts.joinToString(" • ")
    }

    fun applyRingerModeRules(mode: Int): SoundProfileEntity {
        return when (mode) {
            AudioManager.RINGER_MODE_SILENT,
            AudioManager.RINGER_MODE_VIBRATE -> {
                copy(
                    ringerMode = mode,
                    ringerVolume = ringerVolume.copy(current = 0),
                    notificationVolume = notificationVolume.copy(current = 0)
                )
            }

            AudioManager.RINGER_MODE_NORMAL -> {
                copy(ringerMode = mode)
            }

            else -> this
        }
    }

    fun sanitize(): SoundProfileEntity {
        return applyRingerModeRules(this.ringerMode)
    }

    fun applyToSystem(context: android.content.Context) {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                android.util.Log.w("SoundProfileEntity", "Missing Notification Policy Access, cannot change DND/ringer mode")
                return
            }
        }
        
        audioManager.ringerMode = ringerMode

        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, ringerVolume.current, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationVolume.current, 0)
        }
        
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolume.current, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume.current, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, voiceVolume.current, 0)
    }
}
