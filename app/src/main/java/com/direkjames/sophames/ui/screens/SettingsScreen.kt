package com.direkjames.sophames.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.direkjames.sophames.BuildConfig
import com.direkjames.sophames.data.ThemeMode
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.components.CircleIconButton
import com.direkjames.sophames.ui.components.PickerRow
import com.direkjames.sophames.ui.components.ScreenBackground
import com.direkjames.sophames.ui.components.SectionLabel
import com.direkjames.sophames.ui.components.SoftCard
import com.direkjames.sophames.ui.components.SophamesDatePickerDialog
import com.direkjames.sophames.ui.components.sophamesFieldColors
import com.direkjames.sophames.ui.components.sophamesSwitchColors
import com.direkjames.sophames.ui.fullLabel
import com.direkjames.sophames.ui.theme.SophamesTheme
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val s = settings ?: return
    val context = LocalContext.current

    var nameOne by rememberSaveable { mutableStateOf(s.nameOne) }
    var nameTwo by rememberSaveable { mutableStateOf(s.nameTwo) }
    var pickingDate by remember { mutableStateOf(false) }

    // Debounced save so typing doesn't write to disk on every key press.
    LaunchedEffect(nameOne, nameTwo) {
        delay(600)
        if (nameOne.isNotBlank() && nameTwo.isNotBlank() && (nameOne != s.nameOne || nameTwo != s.nameTwo)) {
            vm.setNames(nameOne, nameTwo)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
                Spacer(Modifier.width(14.dp))
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
            }

            SettingsSection("Appearance") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeOption("Light", Icons.Rounded.LightMode, s.themeMode == ThemeMode.LIGHT, { vm.setThemeMode(ThemeMode.LIGHT) }, Modifier.weight(1f))
                    ThemeOption("Dark", Icons.Rounded.DarkMode, s.themeMode == ThemeMode.DARK, { vm.setThemeMode(ThemeMode.DARK) }, Modifier.weight(1f))
                    ThemeOption("System", Icons.Rounded.PhoneAndroid, s.themeMode == ThemeMode.SYSTEM, { vm.setThemeMode(ThemeMode.SYSTEM) }, Modifier.weight(1f))
                }
            }

            SettingsSection("Our story") {
                OutlinedTextField(
                    value = nameOne,
                    onValueChange = { nameOne = it },
                    label = { Text("First name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = sophamesFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = nameTwo,
                    onValueChange = { nameTwo = it },
                    label = { Text("Second name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = sophamesFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                PickerRow(
                    label = "Together since",
                    value = s.startDate.fullLabel(),
                    icon = Icons.Rounded.Favorite,
                    onClick = { pickingDate = true },
                )
            }

            SettingsSection("Reminders") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Date reminders", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "A gentle nudge the day before and on the day, around 9 AM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = s.remindersEnabled,
                        onCheckedChange = { enabled ->
                            vm.setRemindersEnabled(enabled)
                            val needsPermission = enabled && Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            if (needsPermission) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        colors = sophamesSwitchColors(),
                    )
                }
            }

            SettingsSection("Welcome") {
                PickerRow(
                    label = "Welcome screen",
                    value = "Replay the reveal",
                    icon = Icons.Rounded.Favorite,
                    onClick = { vm.setOnboarded(false) },
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Sophames",
                    style = MaterialTheme.typography.headlineSmall.copy(brush = SophamesTheme.extras.numberBrush),
                )
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Made with love by direk james",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }

    if (pickingDate) {
        SophamesDatePickerDialog(
            initial = s.startDate,
            onDismiss = { pickingDate = false },
            onPicked = { vm.setStartDate(it) },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionLabel(title)
            content()
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = SophamesTheme.extras
    val shape = RoundedCornerShape(18.dp)
    val base = if (selected) Modifier.background(extras.roseBrush)
    else Modifier
        .background(extras.softFill)
        .border(1.dp, extras.hairline, shape)
    Column(
        modifier = modifier
            .clip(shape)
            .then(base)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}
