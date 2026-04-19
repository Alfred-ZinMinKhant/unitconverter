package com.sideproject.unitconverter.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.sideproject.unitconverter.data.*
import com.sideproject.unitconverter.ui.components.*
import com.sideproject.unitconverter.ui.theme.*

@Composable
fun UnitConverterScreen(
    onBack: () -> Unit,
    injectedValue: String? = null,
    onInjectConsumed: () -> Unit = {},
) {
    val colors = LocalInstrumentColors.current
    val unitCats = remember { CATEGORIES.filter { it.id != "currency" } }

    var catId by remember { mutableStateOf("length") }
    var fromI by remember { mutableIntStateOf(0) }
    var toI by remember { mutableIntStateOf(7) }
    var value by remember { mutableStateOf("1") }
    var activeSide by remember { mutableStateOf("from") }
    var picker by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var history by remember { mutableStateOf(listOf<HistoryEntry>()) }
    var favorites by remember {
        mutableStateOf(
            listOf(
                FavEntry("length", "km", "mi"),
                FavEntry("mass", "kg", "lb"),
                FavEntry("temp", "°C", "°F"),
                FavEntry("volume", "L", "gal"),
            )
        )
    }
    var showFormula by remember { mutableStateOf(false) }

    val units = UNITS[catId] ?: UNITS["length"]!!

    // Clamp indices when category changes
    LaunchedEffect(catId) {
        if (fromI >= units.size) fromI = 0
        if (toI >= units.size) toI = (units.size - 1).coerceAtLeast(0)
    }

    // External injection from Calculator
    LaunchedEffect(injectedValue) {
        if (injectedValue != null) {
            value = injectedValue
            activeSide = "from"
            tab = 0
            onInjectConsumed()
        }
    }

    val from = units[fromI.coerceIn(0, units.lastIndex)]
    val to = units[toI.coerceIn(0, units.lastIndex)]
    val parsed = value.toDoubleOrNull()
    val isValid = parsed != null && value.isNotEmpty() && value != "-"

    val fromValue = if (activeSide == "from") value else if (isValid) fmt(convert(to, from, parsed!!)) else ""
    val toValue = if (activeSide == "to") value else if (isValid) fmt(convert(from, to, parsed!!)) else ""
    val baseValue = if (isValid) {
        if (activeSide == "from") toBase(from, parsed!!) else toBase(to, parsed!!)
    } else 0.0

    // Auto-add to history
    LaunchedEffect(value, fromI, toI, catId) {
        if (!isValid) return@LaunchedEffect
        kotlinx.coroutines.delay(900)
        val entry = HistoryEntry(
            catId = catId,
            fromSym = from.sym,
            toSym = to.sym,
            inVal = if (activeSide == "from") parsed!! else convert(to, from, parsed!!),
            outVal = if (activeSide == "from") convert(from, to, parsed!!) else parsed!!,
            ts = System.currentTimeMillis(),
        )
        history = (listOf(entry) + history.filter {
            !(it.catId == entry.catId && it.fromSym == entry.fromSym && it.toSym == entry.toSym
                    && abs(it.inVal - entry.inVal) < 1e-9)
        }).take(30)
    }

    val cat = unitCats.find { it.id == catId } ?: unitCats[0]
    val comparison = comparisonFor(catId, baseValue)
    val formula = buildFormula(catId, from, to, parsed ?: 0.0, isValid)
    val clipboardManager = LocalClipboardManager.current

    Box(modifier = Modifier.fillMaxSize().background(colors.bgPage)) {
        Column(modifier = Modifier.fillMaxSize()) {
            InstrumentTopBar(
                title = "UNIT CONVERTER",
                subtitle = "${cat.label.lowercase()} · ${units.size} units",
                onBack = onBack,
            )

            InstrumentTabs(
                tabs = listOf("CONVERT", "HISTORY", "PINNED"),
                selected = tab,
                onSelect = { tab = it },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (tab) {
                    0 -> ConvertTab(
                        unitCats = unitCats,
                        catId = catId,
                        onCatChange = { catId = it },
                        from = from,
                        to = to,
                        fromValue = fromValue,
                        toValue = toValue,
                        activeSide = activeSide,
                        isValid = isValid,
                        baseValue = baseValue,
                        comparison = comparison,
                        formula = formula,
                        showFormula = showFormula,
                        onToggleFormula = { showFormula = !showFormula },
                        units = units,
                        fromI = fromI,
                        toI = toI,
                        onEdit = { side, v ->
                            value = v
                            activeSide = side
                        },
                        onSwap = {
                            val oldFromI = fromI
                            fromI = toI
                            toI = oldFromI
                            value = if (activeSide == "from") toValue else fromValue
                        },
                        onPickUnit = { picker = it },
                        onAddFav = {
                            val entry = FavEntry(catId, from.sym, to.sym)
                            if (favorites.none { it.catId == entry.catId && it.fromSym == entry.fromSym && it.toSym == entry.toSym }) {
                                favorites = (listOf(entry) + favorites).take(12)
                            }
                        },
                        onCopy = { clipboardManager.setText(AnnotatedString(toValue)) },
                        onReset = {
                            value = "1"
                            activeSide = "from"
                        },
                    )
                    1 -> HistoryTab(
                        history = history,
                        onLoad = { h ->
                            catId = h.catId
                            val us = UNITS[h.catId] ?: return@HistoryTab
                            fromI = us.indexOfFirst { it.sym == h.fromSym }.coerceAtLeast(0)
                            toI = us.indexOfFirst { it.sym == h.toSym }.coerceAtLeast(0)
                            value = fmt(h.inVal)
                            activeSide = "from"
                            tab = 0
                        },
                        onClear = { history = emptyList() },
                    )
                    2 -> PinnedTab(
                        favorites = favorites,
                        onLoad = { f ->
                            catId = f.catId
                            val us = UNITS[f.catId] ?: return@PinnedTab
                            fromI = us.indexOfFirst { it.sym == f.fromSym }.coerceAtLeast(0)
                            toI = us.indexOfFirst { it.sym == f.toSym }.coerceAtLeast(0)
                            tab = 0
                        },
                        onRemove = { i -> favorites = favorites.filterIndexed { idx, _ -> idx != i } },
                        onAddCurrent = { tab = 0 },
                    )
                }
            }
        }

        // Unit picker overlay
        UnitPickerSheet(
            visible = picker != null,
            units = units,
            currentIndex = if (picker == "from") fromI else toI,
            title = if (picker == "from") "INPUT UNIT" else "OUTPUT UNIT",
            onPick = { i ->
                if (picker == "from") fromI = i else toI = i
            },
            onDismiss = { picker = null },
        )
    }
}

@Composable
private fun ConvertTab(
    unitCats: List<CategoryDef>,
    catId: String,
    onCatChange: (String) -> Unit,
    from: UnitDef,
    to: UnitDef,
    fromValue: String,
    toValue: String,
    activeSide: String,
    isValid: Boolean,
    baseValue: Double,
    comparison: String,
    formula: FormulaResult,
    showFormula: Boolean,
    onToggleFormula: () -> Unit,
    units: List<UnitDef>,
    fromI: Int,
    toI: Int,
    onEdit: (String, String) -> Unit,
    onSwap: () -> Unit,
    onPickUnit: (String) -> Unit,
    onAddFav: () -> Unit,
    onCopy: () -> Unit,
    onReset: () -> Unit,
) {
    val colors = LocalInstrumentColors.current

    // Category rail
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        unitCats.forEach { c ->
            CategoryChip(
                label = c.label,
                base = c.base,
                isActive = c.id == catId,
                onClick = { onCatChange(c.id) },
            )
        }
    }

    // Main converter panel
    InstrumentPanel {
        PanelLabel("INSTRUMENT · DUAL READOUT", "ch.01")

        UnitField(
            label = "INPUT",
            unit = from,
            value = fromValue,
            active = activeSide == "from",
            onChange = { onEdit("from", it) },
            onPickUnit = { onPickUnit("from") },
        )

        SwapButton(onClick = onSwap)

        UnitField(
            label = "OUTPUT",
            unit = to,
            value = toValue,
            active = activeSide == "to",
            onChange = { onEdit("to", it) },
            onPickUnit = { onPickUnit("to") },
        )

        ActionStrip(
            actions = listOf(
                "PIN" to onAddFav,
                "COPY" to onCopy,
                "RESET" to onReset,
            )
        )
    }

    // Scale comparison
    if (isValid && comparison.isNotEmpty()) {
        ModuleSection(kicker = "REF · SCALE INTUITION", sub = "approximate") {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("≈", style = MaterialTheme.typography.displaySmall, color = colors.accent)
                Text(comparison, style = MaterialTheme.typography.headlineSmall, color = colors.ink100)
            }
            // Scale bar
            ScaleBarComposable(catId, baseValue)
        }
    }

    // Multi-unit readout
    if (isValid) {
        ModuleSection(kicker = "MULTI · ALL UNITS", sub = "${unitCats.find { it.id == catId }?.label?.lowercase()} · live") {
            Column {
                units.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { u ->
                            val i = units.indexOf(u)
                            val isFrom = i == fromI
                            val isTo = i == toI
                            val bg = when {
                                isFrom -> colors.accentSoft
                                isTo -> SignalSoft
                                else -> colors.bgPanel
                            }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bg)
                                    .border(0.5.dp, colors.hairSoft)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    u.sym,
                                    modifier = Modifier.width(48.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                    color = when {
                                        isFrom -> colors.accent
                                        isTo -> Signal
                                        else -> colors.ink70
                                    },
                                )
                                Column {
                                    Text(
                                        fmt(fromBase(u, baseValue)),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                                        color = colors.ink100,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(u.name, style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Formula
    ModuleSection(kicker = "FX · FORMULA", sub = if (showFormula) "▾ hide" else "▸ show") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleFormula)
                .padding(0.dp)
        ) {}
        if (showFormula) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormulaLine("rule", formula.rule)
                FormulaLine("subst", formula.subst)
                FormulaLine("result", formula.result, isOutput = true)
            }
        }
    }
}

@Composable
private fun FormulaLine(key: String, value: String, isOutput: Boolean = false) {
    val colors = LocalInstrumentColors.current
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isOutput) Modifier
                    .padding(top = 6.dp)
                    .drawBehind {
                        drawLine(
                            colors.hairSoft,
                            Offset(0f, 0f),
                            Offset(size.width, 0f),
                            pathEffect = dashEffect,
                        )
                    }
                    .padding(top = 6.dp)
                else Modifier
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            key,
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.ink50,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isOutput) colors.accent else colors.ink90,
            fontWeight = if (isOutput) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ScaleBarComposable(catId: String, baseValue: Double) {
    val colors = LocalInstrumentColors.current
    val pct = scaleBarPosition(catId, baseValue)
    Column(modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(colors.bgInput, RoundedCornerShape(2.dp))
                .border(1.dp, colors.hairSoft, RoundedCornerShape(2.dp)),
        ) {
            // Tick marks
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { p ->
                Box(
                    modifier = Modifier
                        .offset(x = (p * 300).dp) // approximate
                        .width(1.dp)
                        .height(8.dp)
                        .background(colors.ink50)
                        .align(Alignment.CenterStart),
                )
            }
            // Marker
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colors.accent, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("min", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
            Text("log scale", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
            Text("max", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
        }
    }
}

data class HistoryEntry(
    val catId: String,
    val fromSym: String,
    val toSym: String,
    val inVal: Double,
    val outVal: Double,
    val ts: Long,
)

data class FavEntry(val catId: String, val fromSym: String, val toSym: String)

@Composable
private fun HistoryTab(
    history: List<HistoryEntry>,
    onLoad: (HistoryEntry) -> Unit,
    onClear: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    InstrumentPanel {
        PanelLabel("LOG · RECENT", "${history.size.toString().padStart(2, '0')} entries")
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No conversions yet.", style = MaterialTheme.typography.bodySmall, color = colors.ink50)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                history.forEach { h ->
                    val cat = CATEGORIES.find { it.id == h.catId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bgPanel2, RoundedCornerShape(0.dp))
                            .border(1.dp, colors.hairSoft, RoundedCornerShape(0.dp))
                            .clickable { onLoad(h) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            cat?.label ?: "",
                            modifier = Modifier.width(72.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.accent,
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(fmt(h.inVal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink100)
                            Text(h.fromSym, style = MaterialTheme.typography.labelSmall, color = colors.ink70)
                            Text("→", style = MaterialTheme.typography.bodyMedium, color = colors.ink50)
                            Text(fmt(h.outVal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.ink100)
                            Text(h.toSym, style = MaterialTheme.typography.labelSmall, color = colors.ink70)
                        }
                        Text(
                            timeAgo(h.ts),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.ink50,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgPanel2, RoundedCornerShape(3.dp))
                    .border(1.dp, colors.hairSoft, RoundedCornerShape(3.dp))
                    .clickable(onClick = onClear)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("CLEAR LOG", style = MaterialTheme.typography.labelMedium, color = colors.ink90)
            }
        }
    }
}

@Composable
private fun PinnedTab(
    favorites: List<FavEntry>,
    onLoad: (FavEntry) -> Unit,
    onRemove: (Int) -> Unit,
    onAddCurrent: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    InstrumentPanel {
        PanelLabel("PINNED · QUICK PAIRS", "${favorites.size.toString().padStart(2, '0')}/12")
        // Grid of favorites
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            favorites.chunked(2).forEachIndexed { rowIdx, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEachIndexed { colIdx, fav ->
                        val i = rowIdx * 2 + colIdx
                        val cat = CATEGORIES.find { it.id == fav.catId }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(colors.bgPanel2, RoundedCornerShape(4.dp))
                                .border(1.dp, colors.hairSoft, RoundedCornerShape(4.dp))
                                .clickable { onLoad(fav) }
                                .padding(12.dp),
                        ) {
                            Column {
                                Text(
                                    cat?.label?.uppercase() ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.accent,
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(fav.fromSym, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = colors.ink100)
                                    Text("→", color = colors.ink50, fontSize = 14.sp)
                                    Text(fav.toSym, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = colors.ink100)
                                }
                            }
                            // Remove button
                            Text(
                                "×",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clickable { onRemove(i) }
                                    .padding(2.dp),
                                fontSize = 16.sp,
                                color = colors.ink50,
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            // Add button
            if (favorites.size < 12) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgPanel2, RoundedCornerShape(4.dp))
                        .border(
                            width = 1.dp,
                            color = colors.hairSoft,
                            shape = RoundedCornerShape(4.dp),
                        )
                        .clickable(onClick = onAddCurrent)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("+", fontSize = 20.sp, color = colors.ink50)
                        Text("pin current pair", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                    }
                }
            }
        }
    }
}
