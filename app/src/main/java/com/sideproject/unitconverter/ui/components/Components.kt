package com.sideproject.unitconverter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sideproject.unitconverter.data.UnitDef
import com.sideproject.unitconverter.ui.theme.*

// ─── Instrument Panel ───
@Composable
fun InstrumentPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalInstrumentColors.current
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgPanel, RoundedCornerShape(6.dp))
                .border(1.dp, colors.hair, RoundedCornerShape(6.dp))
                .padding(18.dp),
        ) {
            content()
        }
        // Crosshair corners
        Crosshairs()
    }
}

@Composable
private fun BoxScope.Crosshairs() {
    val colors = LocalInstrumentColors.current
    val size = 8.dp
    // Top-left
    Box(
        Modifier
            .align(Alignment.TopStart)
            .size(size)
            .border(BorderStroke(1.dp, colors.ink50), RoundedCornerShape(0.dp))
    )
    // Top-right
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .size(size)
            .border(BorderStroke(1.dp, colors.ink50), RoundedCornerShape(0.dp))
    )
    // Bottom-left
    Box(
        Modifier
            .align(Alignment.BottomStart)
            .size(size)
            .border(BorderStroke(1.dp, colors.ink50), RoundedCornerShape(0.dp))
    )
    // Bottom-right
    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .size(size)
            .border(BorderStroke(1.dp, colors.ink50), RoundedCornerShape(0.dp))
    )
}

// ─── Panel Label (section header) ───
@Composable
fun PanelLabel(
    label: String,
    addr: String = "",
    modifier: Modifier = Modifier,
) {
    val colors = LocalInstrumentColors.current
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .drawBehind {
                drawLine(
                    color = colors.hairSoft,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
            }
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.ink50)
        if (addr.isNotEmpty()) {
            Text(addr, style = MaterialTheme.typography.labelMedium, color = colors.accent)
        }
    }
}

// ─── Top Bar ───
@Composable
fun InstrumentTopBar(
    title: String,
    subtitle: String = "",
    onBack: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgChrome)
            .border(width = 1.dp, color = colors.hair, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, colors.hair, RoundedCornerShape(4.dp))
                .background(colors.bgPanel, RoundedCornerShape(4.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("<", style = MaterialTheme.typography.bodyMedium, color = colors.ink90)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink100,
            )
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = colors.ink50)
            }
        }
        StatusPill("LIVE")
    }
}

// ─── Status Pill ───
@Composable
fun StatusPill(text: String) {
    val colors = LocalInstrumentColors.current
    Row(
        modifier = Modifier
            .background(colors.bgPanel, RoundedCornerShape(3.dp))
            .border(1.dp, colors.hair, RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Signal, CircleShape)
        )
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink90)
    }
}

// ─── Tick Ruler ───
@Composable
fun TickRuler(count: Int = 40, majorEvery: Int = 5, height: Int = 10) {
    val colors = LocalInstrumentColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(count) { i ->
            val major = i % majorEvery == 0
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(if (major) height.dp else (height * 0.45).toInt().dp)
                    .background(colors.ink50.copy(alpha = if (major) 0.9f else 0.35f))
            )
        }
    }
}

// ─── Unit Field ───
@Composable
fun UnitField(
    label: String,
    unit: UnitDef,
    value: String,
    active: Boolean,
    onChange: (String) -> Unit,
    onPickUnit: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    val borderColor = if (active) colors.accentHair else colors.hairSoft

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgInput, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
                color = if (active) colors.accent else colors.ink70,
            )
            // Unit selector button
            Column(
                modifier = Modifier
                    .background(colors.bgPanel, RoundedCornerShape(3.dp))
                    .border(1.dp, colors.hair, RoundedCornerShape(3.dp))
                    .clickable(onClick = onPickUnit)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        unit.sym,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = colors.ink100,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("▾", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                }
                Text(
                    unit.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.ink50,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' || c == '-' || c == 'e' }) },
            textStyle = TextStyle(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                fontSize = 34.sp,
                color = if (active) colors.accent else colors.ink100,
                letterSpacing = (-0.5).sp,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        "0",
                        style = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Medium,
                            fontSize = 34.sp,
                            color = colors.ink30,
                        ),
                    )
                }
                inner()
            }
        )
    }
}

// ─── Swap Button ───
@Composable
fun SwapButton(onClick: () -> Unit) {
    val colors = LocalInstrumentColors.current
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .drawBehind {
                    drawLine(
                        colors.hairSoft,
                        Offset(0f, 0f),
                        Offset(size.width, 0f),
                        pathEffect = dashEffect
                    )
                }
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .border(1.dp, colors.hair, CircleShape)
                .background(colors.bgChrome, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("⇅", fontSize = 18.sp, color = colors.ink90)
        }
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .drawBehind {
                    drawLine(
                        colors.hairSoft,
                        Offset(0f, 0f),
                        Offset(size.width, 0f),
                        pathEffect = dashEffect
                    )
                }
        )
    }
}

// ─── Action Strip ───
@Composable
fun ActionStrip(
    actions: List<Pair<String, () -> Unit>>,
) {
    val colors = LocalInstrumentColors.current
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    Column {
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .drawBehind {
                    drawLine(
                        colors.hairSoft,
                        Offset(0f, 0f),
                        Offset(size.width, 0f),
                        pathEffect = dashEffect
                    )
                }
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            actions.forEach { (label, action) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(colors.bgPanel2, RoundedCornerShape(3.dp))
                        .border(1.dp, colors.hairSoft, RoundedCornerShape(3.dp))
                        .clickable(onClick = action)
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.ink90,
                    )
                }
            }
        }
    }
}

// ─── Module Section ───
@Composable
fun ModuleSection(
    kicker: String,
    sub: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalInstrumentColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPanel, RoundedCornerShape(6.dp))
            .border(1.dp, colors.hair, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgChrome)
                .border(width = 1.dp, color = colors.hair, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(kicker, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.accent)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = colors.ink50)
        }
        content()
    }
}

// ─── Unit Picker Bottom Sheet ───
@Composable
fun UnitPickerSheet(
    visible: Boolean,
    units: List<UnitDef>,
    currentIndex: Int,
    title: String,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .background(
                        colors.bgPanel,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    )
                    .border(
                        1.dp,
                        colors.hair,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    )
                    .clickable(enabled = false) {} // block click-through
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "SELECT · $title",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent,
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", fontSize = 24.sp, color = colors.ink70)
                    }
                }
                HorizontalDivider(color = colors.hair, thickness = 1.dp)
                // List
                LazyColumn {
                    itemsIndexed(units) { index, unit ->
                        val isActive = index == currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isActive) colors.accentSoft else Color.Transparent)
                                .clickable {
                                    onPick(index)
                                    onDismiss()
                                }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                unit.sym,
                                modifier = Modifier.width(70.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                ),
                                color = if (isActive) colors.accent else colors.ink100,
                            )
                            Text(
                                unit.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.ink70,
                            )
                            Text(
                                "→",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isActive) colors.accent else colors.ink50,
                            )
                        }
                        if (index < units.lastIndex) {
                            HorizontalDivider(color = colors.hairSoft, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Tab Row ───
@Composable
fun InstrumentTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = LocalInstrumentColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgChrome),
    ) {
        tabs.forEachIndexed { index, tab ->
            val isActive = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .then(
                        if (isActive)
                            Modifier.background(colors.bgPanel)
                        else
                            Modifier
                    )
                    .then(
                        if (index < tabs.lastIndex)
                            Modifier.border(
                                width = 1.dp,
                                color = colors.hairSoft,
                                shape = RoundedCornerShape(0.dp)
                            )
                        else Modifier
                    )
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        tab,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isActive) colors.ink100 else colors.ink50,
                    )
                }
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(colors.accent)
                    )
                }
            }
        }
    }
    HorizontalDivider(color = colors.hair, thickness = 1.dp)
}

// ─── Category Chip ───
@Composable
fun CategoryChip(
    label: String,
    base: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .background(
                if (isActive) colors.accentSoft else colors.bgPanel,
                RoundedCornerShape(3.dp),
            )
            .border(
                1.dp,
                if (isActive) colors.accentHair else colors.hairSoft,
                RoundedCornerShape(3.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = colors.ink100,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(
            base,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) colors.accent else colors.ink50,
        )
    }
}
