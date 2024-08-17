import com.a3.soundprofiles.core.SoundProfileScheduler
import com.a3.soundprofiles.core.data.DAY
import com.a3.soundprofiles.core.data.SoundProfile
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class SoundProfileSchedulerTest {

  @Test
  fun testCreateNewSoundProfile_repeatEveryday() {
    val calendar = Calendar.getInstance()

    calendar.set(2023, Calendar.OCTOBER, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val startDate = calendar.time

    calendar.set(2023, Calendar.OCTOBER, 2)
    calendar.set(Calendar.HOUR_OF_DAY, 16)
    calendar.set(Calendar.MINUTE, 30)

    val endDate = calendar.time

    val soundProfile =
        SoundProfile(
            id = 1,
            title = "Test Profile",
            startTime = startDate,
            endTime = endDate,
            repeatEveryday = true,
            repeatDays = emptyList(),
            notificationVolume = 0.5f,
            ringerVolume = 0.5f,
            mediaVolume = 0.5f,
            alarmVolume = 0.5f,
            callVolume = 0.5f,
            description = "Test Description",
            isActive = true)

    val newSoundProfile = SoundProfileScheduler.createNewSoundProfile(0, soundProfile)

    calendar.set(2023, Calendar.OCTOBER, 2)
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val expectedStartDate = calendar.time

    calendar.set(2023, Calendar.OCTOBER, 3)
    calendar.set(Calendar.HOUR_OF_DAY, 16)
    calendar.set(Calendar.MINUTE, 30)
    val expectedEndDate = calendar.time

    assertEquals(expectedStartDate, newSoundProfile?.startTime)
    assertEquals(expectedEndDate, newSoundProfile?.endTime)
  }

  @Test
  fun testCreateNewSoundProfile_repeatDays() {
    val calendar = Calendar.getInstance()

    calendar.set(2024, Calendar.AUGUST, 17) // Saturday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val startDate = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val endDate = calendar.time

    val soundProfile =
        SoundProfile(
            id = 1,
            title = "Test Profile",
            startTime = startDate,
            endTime = endDate,
            repeatEveryday = false,
            repeatDays = listOf(DAY.SUNDAY, DAY.WEDNESDAY),
            notificationVolume = 0.5f,
            ringerVolume = 0.5f,
            mediaVolume = 0.5f,
            alarmVolume = 0.5f,
            callVolume = 0.5f,
            description = "Test Description",
            isActive = true)

    val newCal = Calendar.getInstance()
    newCal.set(2024, Calendar.AUGUST, 17)
    newCal.set(Calendar.HOUR_OF_DAY, 20)
    newCal.set(Calendar.MINUTE, 30)

    val newSoundProfile =
        SoundProfileScheduler.createNewSoundProfile(
            newCal.get(Calendar.DAY_OF_WEEK) - 1, soundProfile)

    calendar.set(2024, Calendar.AUGUST, 18) // Next Sunday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val expectedStartDate = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val expectedEndDate = calendar.time

    assertEquals(expectedStartDate, newSoundProfile?.startTime)
    assertEquals(expectedEndDate, newSoundProfile?.endTime)

    newCal.set(2024, Calendar.AUGUST, 18)
    newCal.set(Calendar.HOUR_OF_DAY, 20)
    newCal.set(Calendar.MINUTE, 30)
    val newSoundProfile2 =
        SoundProfileScheduler.createNewSoundProfile(
            newCal.get(Calendar.DAY_OF_WEEK) - 1, newSoundProfile!!)

    calendar.set(2024, Calendar.AUGUST, 21) // Next Sunday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val expectedStartDate2 = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val expectedEndDate2 = calendar.time

    assertEquals(expectedStartDate2, newSoundProfile2?.startTime)
    assertEquals(expectedEndDate2, newSoundProfile2?.endTime)
  }

  @Test
  fun testCreateNewSoundProfile_repeatDays2() {
    val calendar = Calendar.getInstance()

    calendar.set(2024, Calendar.AUGUST, 17) // Saturday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val startDate = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val endDate = calendar.time

    val soundProfile =
        SoundProfile(
            id = 1,
            title = "Test Profile",
            startTime = startDate,
            endTime = endDate,
            repeatEveryday = false,
            repeatDays = listOf(DAY.MONDAY, DAY.FRIDAY, DAY.SUNDAY),
            notificationVolume = 0.5f,
            ringerVolume = 0.5f,
            mediaVolume = 0.5f,
            alarmVolume = 0.5f,
            callVolume = 0.5f,
            description = "Test Description",
            isActive = true)

    val newCal = Calendar.getInstance()
    newCal.set(2024, Calendar.AUGUST, 17)
    newCal.set(Calendar.HOUR_OF_DAY, 20)
    newCal.set(Calendar.MINUTE, 30)

    val newSoundProfile =
        SoundProfileScheduler.createNewSoundProfile(
            newCal.get(Calendar.DAY_OF_WEEK) - 1, soundProfile)

    calendar.set(2024, Calendar.AUGUST, 18) // Next Sunday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val expectedStartDate = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val expectedEndDate = calendar.time

    assertEquals(expectedStartDate, newSoundProfile?.startTime)
    assertEquals(expectedEndDate, newSoundProfile?.endTime)

    newCal.set(2024, Calendar.AUGUST, 19)
    newCal.set(Calendar.HOUR_OF_DAY, 20)
    newCal.set(Calendar.MINUTE, 30)
    val newSoundProfile2 =
        SoundProfileScheduler.createNewSoundProfile(
            newCal.get(Calendar.DAY_OF_WEEK) - 1, newSoundProfile!!)

    calendar.set(2024, Calendar.AUGUST, 19) // Next Monday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val expectedStartDate2 = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val expectedEndDate2 = calendar.time

    assertEquals(expectedStartDate2, newSoundProfile2?.startTime)
    assertEquals(expectedEndDate2, newSoundProfile2?.endTime)

    newCal.set(2024, Calendar.AUGUST, 23)
    newCal.set(Calendar.HOUR_OF_DAY, 20)
    newCal.set(Calendar.MINUTE, 30)
    val newSoundProfile3 =
        SoundProfileScheduler.createNewSoundProfile(
            newCal.get(Calendar.DAY_OF_WEEK) - 1, newSoundProfile2!!)

    calendar.set(2024, Calendar.AUGUST, 23) // Next Monday
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 0)
    val expectedStartDate3 = calendar.time

    calendar.set(Calendar.HOUR_OF_DAY, 20)
    calendar.set(Calendar.MINUTE, 30)
    val expectedEndDate3 = calendar.time

    assertEquals(expectedStartDate3, newSoundProfile3?.startTime)
    assertEquals(expectedEndDate3, newSoundProfile3?.endTime)
  }
}
