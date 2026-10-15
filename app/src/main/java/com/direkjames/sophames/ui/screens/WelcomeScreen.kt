package com.direkjames.sophames.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.direkjames.sophames.data.Settings
import com.direkjames.sophames.ui.components.GradientButton
import com.direkjames.sophames.ui.components.ambientGlow
import com.direkjames.sophames.ui.fullLabel
import com.direkjames.sophames.ui.theme.Brand
import com.direkjames.sophames.ui.theme.DisplayFont
import com.direkjames.sophames.ui.theme.SophamesTheme
import com.direkjames.sophames.domain.togetherFor
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * The reveal: wordmark fades in, then a membership-style card ("Licensed to Sophia & James")
 * with the day counter ticking up, then the button. Shown on first launch and when replayed.
 */
@Composable
fun WelcomeScreen(settings: Settings, today: LocalDate, onEnter: () -> Unit) {
    val days = togetherFor(settings.startDate, today).days
    var stage by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(350); stage = 1
        delay(1200); stage = 2
        delay(1500); stage = 3
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onEnter()
    }
    val extras = SophamesTheme.extras

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .ambientGlow(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Reveal(visible = stage >= 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(width = 104.dp, height = 64.dp)) {
                        val r = 26.dp.toPx()
                        val gap = 22.dp.toPx()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        drawCircle(Brand.Rose, r, Offset(cx - gap, cy), style = Stroke(3.dp.toPx()))
                        drawCircle(Brand.Champagne, r, Offset(cx + gap, cy), style = Stroke(3.dp.toPx()))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Sophames",
                        style = MaterialTheme.typography.displaySmall.copy(letterSpacing = 1.sp, brush = extras.numberBrush),
                    )
                    Text(
                        "Every day, ours.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = DisplayFont,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = extras.labelColor,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Reveal(visible = stage >= 2) {
                MembershipCard(
                    nameOne = settings.nameOne,
                    nameTwo = settings.nameTwo,
                    since = settings.startDate,
                    days = days,
                    counting = stage >= 2,
                )
            }

            Spacer(Modifier.height(40.dp))

            Reveal(visible = stage >= 3) {
                GradientButton(
                    text = "Open Sophames",
                    enabled = stage >= 3,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        if (needsPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onEnter()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun Reveal(visible: Boolean, content: @Composable () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "reveal",
    )
    Box(
        Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 24.dp.toPx()
        },
    ) { content() }
}

@Composable
private fun MembershipCard(
    nameOne: String,
    nameTwo: String,
    since: LocalDate,
    days: Long,
    counting: Boolean,
) {
    val shape = RoundedCornerShape(28.dp)
    val counter = remember { Animatable(0f) }
    LaunchedEffect(counting, days) {
        if (counting) counter.animateTo(days.toFloat(), tween(2000, easing = FastOutSlowInEasing))
    }
    val white = Color.White

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = Brand.Rose.copy(alpha = 0.30f),
                spotColor = Brand.Rose.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(Color(0xFFE8799F), Color(0xFFB93D68), Color(0xFF8E2D52))),
            ),
    ) {
        // Faint watermark rings
        Canvas(Modifier.matchParentSize()) {
            val r = size.height * 0.42f
            val stroke = Stroke(2.dp.toPx())
            drawCircle(white.copy(alpha = 0.10f), r, Offset(size.width * 0.78f, size.height * 0.5f), style = stroke)
            drawCircle(white.copy(alpha = 0.10f), r, Offset(size.width * 0.94f, size.height * 0.5f), style = stroke)
        }
        Box(Modifier.matchParentSize().border(1.dp, Brand.Champagne.copy(alpha = 0.55f), shape))

        Column(
            Modifier
                .matchParentSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SOPHAMES", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp), color = white.copy(alpha = 0.85f))
                Text("EST. ${since.year}", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = Brand.Champagne)
            }
            Column {
                Text("Licensed to", style = MaterialTheme.typography.labelMedium, color = white.copy(alpha = 0.75f))
                Text("$nameOne & $nameTwo", style = MaterialTheme.typography.headlineMedium, color = white)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Since ${since.fullLabel()}", style = MaterialTheme.typography.bodySmall, color = white.copy(alpha = 0.85f))
                Text(
                    "Day ${NumberFormat.getIntegerInstance().format(counter.value.roundToInt())}",
                    style = MaterialTheme.typography.titleLarge,
                    color = white,
                )
            }
        }
    }
}
