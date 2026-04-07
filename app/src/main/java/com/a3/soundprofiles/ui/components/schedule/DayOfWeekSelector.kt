package com.a3.soundprofiles.ui.components.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.a3.soundprofiles.data.local.entities.DAY

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DayOfWeekSelector(
    selectedDays: List<DAY>,
    onDaysChanged: (List<DAY>) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        DAY.SUNDAY to "S",
        DAY.MONDAY to "M",
        DAY.TUESDAY to "T",
        DAY.WEDNESDAY to "W",
        DAY.THURSDAY to "T",
        DAY.FRIDAY to "F",
        DAY.SATURDAY to "S"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        days.forEachIndexed { index, (day, label) ->
            val isSelected = day in selectedDays

            ToggleButton(
                checked = isSelected,
                onCheckedChange = {  onDaysChanged(
                    if (isSelected) selectedDays - day
                    else selectedDays + day
                ) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        days.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Text(label)
            }
        }
    }
}
