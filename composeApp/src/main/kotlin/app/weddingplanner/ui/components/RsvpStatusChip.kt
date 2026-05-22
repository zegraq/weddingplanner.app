package app.weddingplanner.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.RsvpStatus

private val AttendingColor = Color(0xFF2E7D32)
private val DeclinedColor = Color(0xFFC62828)
private val PendingColor = Color(0xFF757575)

fun RsvpStatus.label(): String = when (this) {
    RsvpStatus.Pending -> "Väntar"
    RsvpStatus.Attending -> "Kommer"
    RsvpStatus.Declined -> "Kommer inte"
}

fun RsvpStatus.color(): Color = when (this) {
    RsvpStatus.Pending -> PendingColor
    RsvpStatus.Attending -> AttendingColor
    RsvpStatus.Declined -> DeclinedColor
}

@Composable
fun RsvpStatusDot(status: RsvpStatus, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(10.dp),
        color = status.color(),
        shape = CircleShape,
        content = { Spacer(Modifier.height(0.dp)) },
    )
}

@Composable
fun RsvpStatusChip(status: RsvpStatus, modifier: Modifier = Modifier) {
    AssistChip(
        modifier = modifier,
        onClick = {},
        enabled = false,
        label = { androidx.compose.material3.Text(status.label()) },
        colors = AssistChipDefaults.assistChipColors(
            disabledLabelColor = status.color(),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}
