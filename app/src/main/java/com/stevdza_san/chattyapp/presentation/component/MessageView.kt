package com.stevdza_san.chattyapp.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun MessageView(
    text: String,
    timestamp: Long,
    arrangement: Arrangement.Horizontal = Arrangement.Start,
    contentColor: Color,
    containerColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = arrangement
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.45f),
            horizontalArrangement = arrangement
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = if (arrangement == Arrangement.Start) Alignment.Start else Alignment.End
            ) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(containerColor)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = text,
                    textAlign = if (arrangement == Arrangement.Start) TextAlign.Start
                    else TextAlign.End,
                    color = contentColor
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.38f),
                    text = formatTimestamp(timestamp),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    textAlign = if (arrangement == Arrangement.Start) TextAlign.Start
                    else TextAlign.End
                )
            }
        }
    }
}

private fun formatTimestamp(timestampMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestampMillis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = dateTime.hour % 12
    val formattedHour = if (hour == 0) 12 else hour
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    return "$formattedHour:$minute $amPm"
}