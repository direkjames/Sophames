package com.direkjames.sophames.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.components.CircleIconButton
import com.direkjames.sophames.ui.components.ScreenBackground
import com.direkjames.sophames.ui.components.SectionLabel
import com.direkjames.sophames.ui.fullLabel
import com.direkjames.sophames.ui.theme.SophamesTheme
import java.time.LocalDate

@Composable
fun MemoryDetailScreen(
    vm: MainViewModel,
    memoryId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val flow = remember(memoryId) { vm.observeMemory(memoryId) }
    val memory by flow.collectAsStateWithLifecycle(initialValue = null)
    var confirmDelete by remember { mutableStateOf(false) }
    val extras = SophamesTheme.extras
    val headerShape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    ScreenBackground {
        val m = memory
        if (m != null) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (m.photoFile != null) {
                        AsyncImage(
                            model = vm.photoFile(m.photoFile),
                            contentDescription = m.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.85f)
                                .clip(headerShape),
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(headerShape)
                                .background(extras.roseBrush),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(56.dp),
                            )
                        }
                    }

                    Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                        SectionLabel(LocalDate.ofEpochDay(m.epochDay).fullLabel())
                        if (m.title.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(m.title, style = MaterialTheme.typography.headlineLarge)
                        }
                        if (m.note.isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                m.note,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Spacer(Modifier.navigationBarsPadding().height(24.dp))
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircleIconButton(Icons.Rounded.Edit, "Edit memory", onClick = { onEdit(m.id) })
                        CircleIconButton(Icons.Rounded.Delete, "Delete memory", onClick = { confirmDelete = true })
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this memory?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("The photo and note will be removed from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        vm.deleteMemory(memoryId)
                        onBack()
                    },
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep") } },
        )
    }
}
