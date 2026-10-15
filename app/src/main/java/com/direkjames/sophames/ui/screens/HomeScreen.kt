package com.direkjames.sophames.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.direkjames.sophames.data.MemoryEntity
import com.direkjames.sophames.domain.buildUpcoming
import com.direkjames.sophames.domain.nextDaysMilestone
import com.direkjames.sophames.domain.togetherFor
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.breakdownLabel
import com.direkjames.sophames.ui.components.CircleIconButton
import com.direkjames.sophames.ui.components.CountdownCard
import com.direkjames.sophames.ui.components.GradientButton
import com.direkjames.sophames.ui.components.SectionHeader
import com.direkjames.sophames.ui.components.SectionLabel
import com.direkjames.sophames.ui.components.SoftCard
import com.direkjames.sophames.ui.components.SoftProgress
import com.direkjames.sophames.ui.components.tabContentPadding
import com.direkjames.sophames.ui.fullLabel
import com.direkjames.sophames.ui.theme.DisplayFont
import com.direkjames.sophames.ui.theme.SophamesTheme
import java.text.NumberFormat
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    vm: MainViewModel,
    isDark: Boolean,
    onOpenSettings: () -> Unit,
    onOpenCountdowns: () -> Unit,
    onOpenMemories: () -> Unit,
    onOpenMemory: (String) -> Unit,
    onNewMemory: () -> Unit,
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val memories by vm.memories.collectAsStateWithLifecycle()
    val s = settings ?: return
    val extras = SophamesTheme.extras

    val together = remember(s.startDate, today) { togetherFor(s.startDate, today) }
    val milestone = remember(s.startDate, today) { nextDaysMilestone(s.startDate, today) }
    val upcoming = remember(s.startDate, today, events) { buildUpcoming(s.startDate, today, events) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = tabContentPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Sophames",
                    style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 0.5.sp, brush = extras.numberBrush),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircleIconButton(
                        icon = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = "Toggle dark mode",
                        onClick = { vm.toggleTheme(isDark) },
                    )
                    CircleIconButton(
                        icon = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        onClick = onOpenSettings,
                    )
                }
            }
        }

        // Hero
        item {
            SoftCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                brush = extras.heroBrush,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SectionLabel("${s.nameOne} & ${s.nameTwo}")
                    Spacer(Modifier.height(10.dp))

                    val counter = remember { Animatable(0f) }
                    LaunchedEffect(together.days) {
                        counter.animateTo(together.days.toFloat(), tween(1400, easing = FastOutSlowInEasing))
                    }
                    Text(
                        NumberFormat.getIntegerInstance().format(counter.value.roundToInt()),
                        style = MaterialTheme.typography.displayLarge.copy(brush = extras.numberBrush),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        if (together.days == 1L) "day together" else "days together",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = DisplayFont),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        together.breakdownLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Since ${s.startDate.fullLabel()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (milestone != null) {
                        Spacer(Modifier.height(24.dp))
                        SoftProgress(milestone.progress)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (milestone.daysLeft == 0L) "Today: ${milestone.title}"
                            else "${milestone.daysLeft} days until ${milestone.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Coming up
        item { SectionHeader("Coming up", action = "See all", onAction = onOpenCountdowns) }
        items(upcoming.take(2), key = { it.id }) { countdown ->
            CountdownCard(countdown)
        }

        // Latest memory
        item { SectionHeader("Latest memory", action = "See all", onAction = onOpenMemories) }
        item {
            val latest = memories.firstOrNull()
            if (latest != null) {
                LatestMemoryCard(vm, latest, onClick = { onOpenMemory(latest.id) })
            } else {
                SoftCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Your story starts here", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Add a photo and a few words from a day you never want to forget.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(18.dp))
                        GradientButton("Add a memory", onClick = onNewMemory, icon = Icons.Rounded.Add)
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestMemoryCard(vm: MainViewModel, memory: MemoryEntity, onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    val extras = SophamesTheme.extras
    Box(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
            .shadow(
                elevation = if (extras.isDark) 0.dp else 16.dp,
                shape = shape,
                ambientColor = extras.shadowTint.copy(alpha = 0.12f),
                spotColor = extras.shadowTint.copy(alpha = 0.20f),
            )
            .clip(shape)
            .background(extras.roseBrush)
            .clickable(onClick = onClick),
    ) {
        if (memory.photoFile != null) {
            AsyncImage(
                model = vm.photoFile(memory.photoFile),
                contentDescription = memory.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))),
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        ) {
            Text(
                LocalDate.ofEpochDay(memory.epochDay).fullLabel().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
            )
            if (memory.title.isNotBlank()) {
                Text(
                    memory.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
