package com.sideproject.unitconverter

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

private const val BANNER_AD_UNIT_ID = "ca-app-pub-9922040295495075/9550568487"

enum class Category(val label: String, val icon: String) {
    LENGTH("Length", "📏"),
    WEIGHT("Weight", "⚖️"),
    TEMPERATURE("Temp", "🌡️"),
    AREA("Area", "⬜"),
    VOLUME("Volume", "🧪"),
    SPEED("Speed", "💨"),
}

data class UnitDef(val name: String, val toBase: (Double) -> Double, val fromBase: (Double) -> Double)

val unitsByCategory: Map<Category, List<UnitDef>> = mapOf(
    Category.LENGTH to listOf(
        UnitDef("Meter",      { it },            { it }),
        UnitDef("Kilometer",  { it * 1000 },     { it / 1000 }),
        UnitDef("Centimeter", { it / 100 },      { it * 100 }),
        UnitDef("Millimeter", { it / 1000 },     { it * 1000 }),
        UnitDef("Mile",       { it * 1609.344 }, { it / 1609.344 }),
        UnitDef("Yard",       { it * 0.9144 },   { it / 0.9144 }),
        UnitDef("Foot",       { it * 0.3048 },   { it / 0.3048 }),
        UnitDef("Inch",       { it * 0.0254 },   { it / 0.0254 }),
    ),
    Category.WEIGHT to listOf(
        UnitDef("Kilogram",  { it },            { it }),
        UnitDef("Gram",      { it / 1000 },     { it * 1000 }),
        UnitDef("Milligram", { it / 1e6 },      { it * 1e6 }),
        UnitDef("Pound",     { it * 0.453592 }, { it / 0.453592 }),
        UnitDef("Ounce",     { it * 0.0283495 },{ it / 0.0283495 }),
        UnitDef("Ton",       { it * 1000 },     { it / 1000 }),
    ),
    Category.TEMPERATURE to listOf(
        UnitDef("Celsius",    { it },                 { it }),
        UnitDef("Fahrenheit", { (it - 32) * 5 / 9 }, { it * 9 / 5 + 32 }),
        UnitDef("Kelvin",     { it - 273.15 },        { it + 273.15 }),
    ),
    Category.AREA to listOf(
        UnitDef("Square Meter",     { it },             { it }),
        UnitDef("Square Kilometer", { it * 1e6 },       { it / 1e6 }),
        UnitDef("Square Foot",      { it * 0.092903 },  { it / 0.092903 }),
        UnitDef("Square Inch",      { it * 0.00064516 },{ it / 0.00064516 }),
        UnitDef("Acre",             { it * 4046.86 },   { it / 4046.86 }),
        UnitDef("Hectare",          { it * 10000 },     { it / 10000 }),
    ),
    Category.VOLUME to listOf(
        UnitDef("Liter",       { it },            { it }),
        UnitDef("Milliliter",  { it / 1000 },     { it * 1000 }),
        UnitDef("Cubic Meter", { it * 1000 },     { it / 1000 }),
        UnitDef("Gallon (US)", { it * 3.78541 },  { it / 3.78541 }),
        UnitDef("Quart",       { it * 0.946353 }, { it / 0.946353 }),
        UnitDef("Cup",         { it * 0.236588 }, { it / 0.236588 }),
        UnitDef("Fluid Oz",    { it * 0.0295735 },{ it / 0.0295735 }),
    ),
    Category.SPEED to listOf(
        UnitDef("m/s",  { it },            { it }),
        UnitDef("km/h", { it / 3.6 },      { it * 3.6 }),
        UnitDef("mph",  { it * 0.44704 },  { it / 0.44704 }),
        UnitDef("knot", { it * 0.514444 }, { it / 0.514444 }),
        UnitDef("ft/s", { it * 0.3048 },   { it / 0.3048 }),
    ),
)

fun convert(value: Double, from: UnitDef, to: UnitDef): Double = to.fromBase(from.toBase(value))

fun formatResult(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.6f".format(value).trimEnd('0').trimEnd('.')
    }
}

val GradientStart = Color(0xFF6650A4)
val GradientEnd   = Color(0xFF9C4EB8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterApp() {
    var selectedCategory by remember { mutableStateOf(Category.LENGTH) }
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf(0) }
    var toUnit by remember { mutableStateOf(1) }

    val units = unitsByCategory[selectedCategory] ?: emptyList()

    LaunchedEffect(selectedCategory) {
        fromUnit = 0
        toUnit = 1
        inputValue = ""
    }

    val result = remember(inputValue, fromUnit, toUnit, selectedCategory) {
        val v = inputValue.toDoubleOrNull()
        if (v != null && units.isNotEmpty()) formatResult(convert(v, units[fromUnit], units[toUnit]))
        else ""
    }

    Scaffold(
        bottomBar = {
            AndroidView(
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                },
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column {
                    Text(
                        text = "Unit Converter",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fast & accurate conversions",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Category selector
                CategorySelector(selected = selectedCategory, onSelect = { selectedCategory = it })

                // Converter card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // FROM section
                        Text(
                            "FROM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        UnitDropdown(
                            label = "Select unit",
                            units = units,
                            selected = fromUnit,
                            onSelect = { fromUnit = it }
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            placeholder = { Text("0", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )

                        // Swap button
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            FilledIconButton(
                                onClick = {
                                    val tmp = fromUnit
                                    fromUnit = toUnit
                                    toUnit = tmp
                                },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = GradientStart,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("⇅", fontSize = 18.sp, color = Color.White)
                            }
                        }

                        // TO section
                        Text(
                            "TO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        UnitDropdown(
                            label = "Select unit",
                            units = units,
                            selected = toUnit,
                            onSelect = { toUnit = it }
                        )
                        Spacer(Modifier.height(10.dp))

                        // Result display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientStart.copy(alpha = 0.1f), GradientEnd.copy(alpha = 0.1f))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 18.dp)
                        ) {
                            AnimatedContent(
                                targetState = result,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "result"
                            ) { res ->
                                Text(
                                    text = if (res.isEmpty()) "—" else res,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isEmpty())
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else
                                        GradientStart,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CategorySelector(selected: Category, onSelect: (Category) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Category.entries.forEach { cat ->
            val isSelected = cat == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected)
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        else
                            Brush.linearGradient(listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            ))
                    )
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(cat.icon, fontSize = 20.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        cat.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    label: String,
    units: List<UnitDef>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = units.getOrNull(selected)?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { index, unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}
