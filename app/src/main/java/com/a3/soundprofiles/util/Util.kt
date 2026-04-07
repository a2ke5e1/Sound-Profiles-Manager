package com.a3.soundprofiles.util

import android.app.NotificationManager
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.a3.soundprofiles.data.local.entities.StreamVolume

/**
 * Extension function to safely extract volume stats into a StreamVolume data class.
 */
fun AudioManager?.getVolumeStats(streamType: Int): StreamVolume {
    if (this == null) {
        return StreamVolume(current = 0, min = 0, max = 0)
    }
    val current = this.getStreamVolume(streamType)
    val max = this.getStreamMaxVolume(streamType)
    val min = this.getStreamMinVolume(streamType)
    return StreamVolume(current = current, min = min, max = max)
}


@Composable
fun rememberAudioManager(): AudioManager? {
    val context = LocalContext.current
    return remember(context) {
        ContextCompat.getSystemService(context, AudioManager::class.java)
    }
}

@Composable
fun rememberNotificationManager(): NotificationManager? {
    val context = LocalContext.current
    return remember(context) {
        ContextCompat.getSystemService(context, NotificationManager::class.java)
    }
}

