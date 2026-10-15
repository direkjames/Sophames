package com.direkjames.sophames.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.components.CircleIconButton
import com.direkjames.sophames.ui.components.GradientButton
import com.direkjames.sophames.ui.components.PickerRow
import com.direkjames.sophames.ui.components.ScreenBackground
import com.direkjames.sophames.ui.components.SophamesDatePickerDialog
import com.direkjames.sophames.ui.components.sophamesFieldColors
import com.direkjames.sophames.ui.fullLabel
import com.direkjames.sophames.ui.theme.SophamesTheme
import java.time.LocalDate

/** Used for both "new memory" (memoryId == null) and "edit memory". */
@Composable
fun MemoryEditorScreen(vm: MainViewModel, memoryId: String?, onClose: () -> Unit) {
    var loaded by rememberSaveable { mutableStateOf(memoryId == null) }
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var epoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var existingPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var pickedUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var removeExisting by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf(false) }

    LaunchedEffect(memoryId) {
        if (!loaded && memoryId != null) {
            vm.getMemory(memoryId)?.let {
                title = it.title
                note = it.note
                epoch = it.epochDay
                existingPhoto = it.photoFile
            }
            loaded = true
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pickedUri = uri
            removeExisting = false
        }
    }

    val previewModel: Any? = pickedUri ?: existingPhoto?.takeIf { !removeExisting }?.let { vm.photoFile(it) }
    val canSave = (title.isNotBlank() || previewModel != null) && !saving
    val extras = SophamesTheme.extras
    val outline = MaterialTheme.colorScheme.outline

    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleIconButton(Icons.Rounded.Close, "Close", onClick = onClose)
                Text(
                    if (memoryId == null) "New memory" else "Edit memory",
                    style = MaterialTheme.typography.headlineSmall,
                )
                GradientButton(
                    text = "Save",
                    enabled = canSave,
                    onClick = {
                        saving = true
                        vm.saveMemory(
                            id = memoryId,
                            title = title,
                            note = note,
                            date = LocalDate.ofEpochDay(epoch),
                            newPhoto = pickedUri,
                            removePhoto = removeExisting,
                            onDone = onClose,
                        )
                    },
                )
            }

            // Photo
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(extras.softFill)
                    .drawBehind {
                        if (previewModel == null) {
                            drawRoundRect(
                                color = outline,
                                cornerRadius = CornerRadius(24.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
                                ),
                            )
                        }
                    }
                    .clickable {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Add a photo", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Optional, but they make it feel real",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (previewModel != null) {
                TextButton(
                    onClick = {
                        pickedUri = null
                        if (existingPhoto != null) removeExisting = true
                    },
                ) { Text("Remove photo") }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = sophamesFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            PickerRow(
                label = "Date",
                value = LocalDate.ofEpochDay(epoch).fullLabel(),
                icon = Icons.Rounded.Event,
                onClick = { pickingDate = true },
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("What do you want to remember?") },
                minLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = sophamesFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.navigationBarsPadding().height(24.dp))
        }
    }

    if (pickingDate) {
        SophamesDatePickerDialog(
            initial = LocalDate.ofEpochDay(epoch),
            onDismiss = { pickingDate = false },
            onPicked = { epoch = it.toEpochDay() },
        )
    }
}
