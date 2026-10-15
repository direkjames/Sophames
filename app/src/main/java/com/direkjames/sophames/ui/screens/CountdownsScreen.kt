package com.direkjames.sophames.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.direkjames.sophames.domain.Countdown
import com.direkjames.sophames.domain.buildCustomList
import com.direkjames.sophames.domain.buildUpcoming
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.components.CircleIconButton
import com.direkjames.sophames.ui.components.CountdownCard
import com.direkjames.sophames.ui.components.PickerRow
import com.direkjames.sophames.ui.components.ScreenTitle
import com.direkjames.sophames.ui.components.SectionHeader
import com.direkjames.sophames.ui.components.SoftCard
import com.direkjames.sophames.ui.components.SoftProgress
import com.direkjames.sophames.ui.components.SophamesDatePickerDialog
import com.direkjames.sophames.ui.components.sophamesFieldColors
import com.direkjames.sophames.ui.components.sophamesSwitchColors
import com.direkjames.sophames.ui.components.tabContentPadding
import com.direkjames.sophames.ui.shortLabel
import com.direkjames.sophames.ui.theme.SophamesTheme
import java.text.NumberFormat
import java.time.LocalDate

@Composable
fun CountdownsScreen(vm: MainViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val s = settings ?: return

    val upcoming = remember(s.startDate, today, events) { buildUpcoming(s.startDate, today, events) }
    val custom = remember(events, today) { buildCustomList(events, today) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Countdown?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = tabContentPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenTitle(
                title = "Countdowns",
                subtitle = "Every date worth waiting for",
                trailing = {
                    CircleIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = "Add a date",
                        onClick = { showAdd = true },
                        filled = true,
                    )
                },
            )
        }

        item { NextUpHero(upcoming.first()) }

        item { SectionHeader("Coming up") }
        items(upcoming.drop(1), key = { it.id }) { countdown ->
            CountdownCard(countdown)
        }

        item { SectionHeader("Your dates") }
        if (custom.isEmpty()) {
            item {
                SoftCard(Modifier.fillMaxWidth()) {
                    Text(
                        "Birthdays, trips, the first time you met. Tap + to add a date and count down to it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                }
            }
        } else {
            items(custom, key = { it.id }) { countdown ->
                CountdownCard(
                    countdown,
                    trailing = {
                        IconButton(onClick = { pendingDelete = countdown }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete ${countdown.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        }
    }

    if (showAdd) {
        AddDateDialog(
            onDismiss = { showAdd = false },
            onAdd = { title, date, repeat -> vm.addEvent(title, date, repeat) },
        )
    }

    pendingDelete?.let { countdown ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this date?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("\"${countdown.title}\" will be removed from your countdowns.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        countdown.customId?.let { vm.deleteEvent(it) }
                        pendingDelete = null
                    },
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Keep") } },
        )
    }
}

@Composable
private fun NextUpHero(countdown: Countdown) {
    val white = Color.White
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        brush = SophamesTheme.extras.roseBrush,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(26.dp),
        ) {
            Text(
                "NEXT UP",
                style = MaterialTheme.typography.labelSmall,
                color = white.copy(alpha = 0.80f),
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (countdown.daysLeft == 0L) "Today" else NumberFormat.getIntegerInstance().format(countdown.daysLeft),
                    style = MaterialTheme.typography.displayMedium,
                    color = white,
                )
                if (countdown.daysLeft > 0) {
                    Text(
                        if (countdown.daysLeft == 1L) "day" else "days",
                        style = MaterialTheme.typography.titleMedium,
                        color = white.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 10.dp, bottom = 12.dp),
                    )
                }
            }
            Text(
                countdown.title.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                color = white,
            )
            Text(
                countdown.date.shortLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = white.copy(alpha = 0.80f),
            )
            Spacer(Modifier.height(18.dp))
            SoftProgress(
                progress = countdown.progress,
                track = white.copy(alpha = 0.25f),
                fill = SolidColor(white),
            )
        }
    }
}

@Composable
private fun AddDateDialog(onDismiss: () -> Unit, onAdd: (String, LocalDate, Boolean) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var epoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var repeat by rememberSaveable { mutableStateOf(true) }
    var picking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a date", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What are we counting down to?") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = sophamesFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                PickerRow(
                    label = "Date",
                    value = LocalDate.ofEpochDay(epoch).shortLabel(),
                    icon = Icons.Rounded.Event,
                    onClick = { picking = true },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Repeat every year",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = repeat, onCheckedChange = { repeat = it }, colors = sophamesSwitchColors())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onAdd(title, LocalDate.ofEpochDay(epoch), repeat)
                    onDismiss()
                },
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (picking) {
        SophamesDatePickerDialog(
            initial = LocalDate.ofEpochDay(epoch),
            onDismiss = { picking = false },
            onPicked = { epoch = it.toEpochDay() },
        )
    }
}
