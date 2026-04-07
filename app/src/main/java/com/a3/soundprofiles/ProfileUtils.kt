package com.a3.soundprofiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class to represent the profile data.
 */
data class ProfileData(val name: String, val iconName: String)

val profileIconMap: Map<String, ImageVector> = mapOf(
    "work" to Icons.Outlined.Work,
    "home" to Icons.Outlined.Home,
    "star" to Icons.Outlined.Star,
    "notifications" to Icons.Outlined.Notifications,
    "person" to Icons.Outlined.Person,
    "settings" to Icons.Outlined.Settings,
    "call" to Icons.Outlined.Call,
    "email" to Icons.Outlined.Email,
    "favorite" to Icons.Outlined.Favorite,
    "shopping_cart" to Icons.Outlined.ShoppingCart,
    "alarm" to Icons.Outlined.Alarm,
    "build" to Icons.Outlined.Build,
    "camera" to Icons.Outlined.CameraAlt,
    "directions_car" to Icons.Outlined.DirectionsCar,
    "event" to Icons.Outlined.Event,
    "face" to Icons.Outlined.Face,
    "flight" to Icons.Outlined.Flight,
    "lightbulb" to Icons.Outlined.Lightbulb,
    "lock" to Icons.Outlined.Lock,
    "map" to Icons.Outlined.Map,
    "music_note" to Icons.Outlined.MusicNote,
    "palette" to Icons.Outlined.Palette,
    "pets" to Icons.Outlined.Pets,
    "phone" to Icons.Outlined.Phone,
    "place" to Icons.Outlined.Place,
    "school" to Icons.Outlined.School,
    "search" to Icons.Outlined.Search,
    "send" to Icons.AutoMirrored.Outlined.Send,
    "videocam" to Icons.Outlined.Videocam,
    "volume_up" to Icons.AutoMirrored.Outlined.VolumeUp,
    "volume_off" to Icons.AutoMirrored.Outlined.VolumeOff,
    "volume_mute" to Icons.AutoMirrored.Outlined.VolumeMute,
    "schedule" to Icons.Outlined.Schedule,
    "timer" to Icons.Outlined.Timer,
    "nightlight" to Icons.Outlined.Nightlight,
    "wb_sunny" to Icons.Outlined.WbSunny,
    "beach_access" to Icons.Outlined.BeachAccess,
    "coffee" to Icons.Outlined.Coffee,
    "restaurant" to Icons.Outlined.Restaurant,
    "sports_esports" to Icons.Outlined.SportsEsports,
    "fitness_center" to Icons.Outlined.FitnessCenter,
    "directions_run" to Icons.AutoMirrored.Outlined.DirectionsRun,
    "directions_bike" to Icons.AutoMirrored.Outlined.DirectionsBike,
    "local_library" to Icons.Outlined.LocalLibrary,
    "medical_services" to Icons.Outlined.MedicalServices,
    "business_center" to Icons.Outlined.BusinessCenter,
    "headphones" to Icons.Outlined.Headphones,
    "mic" to Icons.Outlined.Mic,
    "tv" to Icons.Outlined.Tv,
    "laptop" to Icons.Outlined.Laptop,
    "smartphone" to Icons.Outlined.Smartphone,
    "directions_bus" to Icons.Outlined.DirectionsBus,
    "event_busy" to Icons.Outlined.EventBusy,
)

/**
 * Function to map string icon names to Compose ImageVectors.
 */
fun mapNameToIcon(iconName: String): ImageVector {
    return profileIconMap[iconName] ?: Icons.Outlined.Star
}
