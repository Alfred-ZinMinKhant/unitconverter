package com.sideproject.unitconverter.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sideproject.unitconverter.data.*
import com.sideproject.unitconverter.ui.components.*
import com.sideproject.unitconverter.ui.theme.*

@Composable
fun CurrencyScreen(onBack: () -> Unit) {
    val colors = LocalInstrumentColors.current
    val units = UNITS["currency"] ?: return

    var fromI by remember { mutableIntStateOf(0) }
    var toI by remember { mutableIntStateOf(1) }
    var value by remember { mutableStateOf("100") }
    var activeSide by remember { mutableStateOf("from") }
    var picker by remember { mutableStateOf<String?>(null) }

    val from = units[fromI.coerceIn(0, units.lastIndex)]
    val to = units[toI.coerceIn(0, units.lastIndex)]
    val parsed = value.toDoubleOrNull()
    val valid = parsed != null && value.isNotEmpty()
    val fromVal = if (activeSide == "from") value else if (valid) fmt(convert(to, from, parsed!!)) else ""
    val toVal = if (activeSide == "to") value else if (valid) fmt(convert(from, to, parsed!!)) else ""

    val rate = convert(from, to, 1.0)
    val invRate = convert(to, from, 1.0)

    val quickAmounts = listOf(1, 10, 100, 1000, 10000)
    val popular = listOf("USD", "EUR", "GBP", "JPY", "CNY", "MMK", "THB", "SGD")
        .filter { it != from.sym }
        .take(6)

    Box(modifier = Modifier.fillMaxSize().background(colors.bgPage)) {
        Column(modifier = Modifier.fillMaxSize()) {
            InstrumentTopBar(
                title = "CURRENCY",
                subtitle = "${units.size} currencies · offline cache",
                onBack = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Rate hero
                RateHero(from, to, rate, invRate)

                // Convert panel
                InstrumentPanel {
                    PanelLabel("CONVERT · DUAL READOUT", "ch.FX")

                    UnitField(
                        label = "FROM",
                        unit = from,
                        value = fromVal,
                        active = activeSide == "from",
                        onChange = { value = it; activeSide = "from" },
                        onPickUnit = { picker = "from" },
                    )

                    SwapButton {
                        val oldFrom = fromI
                        fromI = toI
                        toI = oldFrom
                        value = if (activeSide == "from") toVal else fromVal
                    }

                    UnitField(
                        label = "TO",
                        unit = to,
                        value = toVal,
                        active = activeSide == "to",
                        onChange = { value = it; activeSide = "to" },
                        onPickUnit = { picker = "to" },
                    )

                    // Quick amounts
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
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
                                    pathEffect = dashEffect,
                                )
                            }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        quickAmounts.forEach { q ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.bgPanel2, RoundedCornerShape(3.dp))
                                    .border(1.dp, colors.hairSoft, RoundedCornerShape(3.dp))
                                    .clickable {
                                        value = q.toString()
                                        activeSide = "from"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    q.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.ink90,
                                )
                            }
                        }
                    }
                }

                // Popular rates table
                ModuleSection(kicker = "TABLE · FROM ${from.sym}", sub = "per 1 unit") {
                    Column {
                        popular.forEach { sym ->
                            val u = units.find { it.sym == sym } ?: return@forEach
                            val r = convert(from, u, 1.0)
                            val amt = if (valid) convert(from, u, parsed!!) else 0.0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { toI = units.indexOfFirst { it.sym == sym }.coerceAtLeast(0) }
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                                    .drawBehind {
                                        drawLine(
                                            colors.hairSoft,
                                            Offset(0f, size.height),
                                            Offset(size.width, size.height),
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    sym,
                                    modifier = Modifier.width(52.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = colors.ink100,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(u.name, style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        fmt(amt),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                        color = colors.ink100,
                                    )
                                    Text(
                                        "@ ${fmt(r)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.ink50,
                                    )
                                }
                            }
                        }
                    }
                }

                // Offline note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = colors.hairSoft,
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Warn, CircleShape)
                    )
                    Text(
                        "offline cache · rates may drift · tap sync when online",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.ink50,
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // Unit picker overlay
        UnitPickerSheet(
            visible = picker != null,
            units = units,
            currentIndex = if (picker == "from") fromI else toI,
            title = if (picker == "from") "SOURCE CURRENCY" else "TARGET CURRENCY",
            onPick = { i ->
                if (picker == "from") fromI = i else toI = i
            },
            onDismiss = { picker = null },
        )
    }
}

@Composable
private fun RateHero(from: UnitDef, to: UnitDef, rate: Double, invRate: Double) {
    val colors = LocalInstrumentColors.current
    InstrumentPanel {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${from.sym} / ${to.sym}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp),
                    color = colors.ink100,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(Modifier.size(5.dp).background(Warn, CircleShape))
                    Text("cache · 23m ago", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Big rate display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("1", style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp), color = colors.ink70)
                Text(from.sym, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.ink70)
                Text("=", fontSize = 18.sp, color = colors.ink50)
                Text(
                    fmt(rate),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 36.sp),
                    color = colors.accent,
                )
                Text(to.sym, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.ink70)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "1 ${to.sym} = ${fmt(invRate)} ${from.sym}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.ink50,
            )
    }
}
