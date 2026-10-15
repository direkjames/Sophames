package com.direkjames.sophames.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.direkjames.sophames.data.MemoryEntity
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.components.CircleIconButton
import com.direkjames.sophames.ui.components.GradientButton
import com.direkjames.sophames.ui.components.ScreenTitle
import com.direkjames.sophames.ui.components.SectionLabel
import com.direkjames.sophames.ui.components.SoftCard
import com.direkjames.sophames.ui.components.tabContentPadding
import com.direkjames.sophames.ui.fullLabel
import com.direkjames.sophames.ui.monthYearLabel
import java.time.LocalDate
import java.time.YearMonth

private sealed interface TimelineItem {
    data class Header(val label: String) : TimelineItem
    data class Entry(val memory: MemoryEntity) : TimelineItem
}

private fun buildTimeline(memories: List<MemoryEntity>): List<TimelineItem> {
    val out = mutableListOf<TimelineItem>()
    var lastMonth: YearMonth? = null
    for (memory in memories) {
        val date = LocalDate.ofEpochDay(memory.epochDay)
        val month = YearMonth.from(date)
        if (month != lastMonth) {
            out += TimelineItem.Header(date.monthYearLabel())
            lastMonth = month
        }
        out += TimelineItem.Entry(memory)
    }
    return out
}

@Composable
fun MemoriesScreen(
    vm: MainViewModel,
    onOpenMemory: (String) -> Unit,
    onNewMemory: () -> Unit,
) {
    val memories by vm.memories.collectAsStateWithLifecycle()
    val timeline = remember(memories) { buildTimeline(memories) }
    val lineColor = MaterialTheme.colorScheme.outline
    val dotColor = MaterialTheme.colorScheme.primary
    val ringColor = MaterialTheme.colorScheme.background

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = tabContentPadding(),
    ) {
        item {
            Column {
                ScreenTitle(
                    title = "Memories",
                    subtitle = when (memories.size) {
                        0 -> "Nothing here yet"
                        1 -> "1 moment, kept forever"
                        else -> "${memories.size} moments, kept forever"
                    },
                    trailing = {
                        CircleIconButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "New memory",
                            onClick = onNewMemory,
                            filled = true,
                        )
                    },
                )
                Spacer(Modifier.height(18.dp))
            }
        }

        if (memories.isEmpty()) {
            item {
                SoftCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("The first page is blank", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Pick a photo, write a few words, and it lands here on your timeline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(20.dp))
                        GradientButton("Add a memory", onClick = onNewMemory, icon = Icons.Rounded.Add)
                    }
                }
            }
        } else {
            itemsIndexed(
                items = timeline,
                key = { _, entry ->
                    when (entry) {
                        is TimelineItem.Header -> "header-${entry.label}"
                        is TimelineItem.Entry -> entry.memory.id
                    }
                },
            ) { index, entry ->
                when (entry) {
                    is TimelineItem.Header -> MonthHeaderRow(entry.label, isFirst = index == 0, lineColor = lineColor)
                    is TimelineItem.Entry -> MemoryRow(
                        vm = vm,
                        memory = entry.memory,
                        isLast = index == timeline.lastIndex,
                        lineColor = lineColor,
                        dotColor = dotColor,
                        ringColor = ringColor,
                        onClick = { onOpenMemory(entry.memory.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeaderRow(label: String, isFirst: Boolean, lineColor: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 12.dp.toPx()
                drawLine(
                    color = lineColor,
                    start = Offset(x, if (isFirst) size.height / 2f else 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx(),
                )
            }
            .padding(top = 10.dp, bottom = 12.dp),
    ) {
        Spacer(Modifier.width(32.dp))
        Text(label, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun MemoryRow(
    vm: MainViewModel,
    memory: MemoryEntity,
    isLast: Boolean,
    lineColor: Color,
    dotColor: Color,
    ringColor: Color,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 12.dp.toPx()
                val dotY = 34.dp.toPx()
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, if (isLast) dotY else size.height),
                    strokeWidth = 2.dp.toPx(),
                )
                drawCircle(ringColor, radius = 9.dp.toPx(), center = Offset(x, dotY))
                drawCircle(dotColor, radius = 5.dp.toPx(), center = Offset(x, dotY))
            },
    ) {
        Spacer(Modifier.width(32.dp))
        SoftCard(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 18.dp),
            onClick = onClick,
        ) {
            if (memory.photoFile != null) {
                AsyncImage(
                    model = vm.photoFile(memory.photoFile),
                    contentDescription = memory.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                )
            }
            Column(Modifier.padding(16.dp)) {
                SectionLabel(LocalDate.ofEpochDay(memory.epochDay).fullLabel())
                if (memory.title.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(memory.title, style = MaterialTheme.typography.titleLarge)
                }
                if (memory.note.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        memory.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
