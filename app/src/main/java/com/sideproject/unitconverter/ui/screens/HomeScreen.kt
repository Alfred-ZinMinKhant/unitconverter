package com.sideproject.unitconverter.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sideproject.unitconverter.ui.components.StatusPill
import com.sideproject.unitconverter.ui.components.TickRuler
import com.sideproject.unitconverter.ui.theme.*

data class ToolEntry(
    val id: String,
    val label: String,
    val desc: String,
    val count: String,
)

@Composable
fun HomeScreen(onOpen: (String) -> Unit) {
    val colors = LocalInstrumentColors.current
    val tools = listOf(
        ToolEntry("unit", "Unit Converter",
            "Length · Mass · Temp · Area · Volume · Speed · Time · Data · Energy · Pressure",
            "10 categories · 80+ units"),
        ToolEntry("currency", "Currency Converter",
            "Real-world rates · fiat + crypto · offline cache",
            "17 currencies"),
        ToolEntry("calc", "Calculator",
            "Scientific pad · parentheses · send result to converter",
            "expression eval"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPage)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgChrome)
                .drawBehind {
                    drawLine(colors.hair, Offset(0f, size.height), Offset(size.width, size.height))
                }
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("◉", color = colors.accent, fontSize = 10.sp)
                    Text("CONVERTER", style = MaterialTheme.typography.titleLarge, color = colors.ink100)
                    Text("v2.4", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                }
                StatusPill("READY")
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Instrument-grade\nconversion suite.",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink100,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Precision tools for measurement, money, and math.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.ink70,
            )

            Spacer(Modifier.height(16.dp))
            TickRuler(count = 50, majorEvery = 7, height = 14)

            Spacer(Modifier.height(14.dp))

            // Meta grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.hairSoft, RoundedCornerShape(0.dp)),
            ) {
                listOf(
                    "TOOLS" to "03",
                    "UNITS" to "80+",
                    "PRECISION" to "1e-9",
                    "BUILD" to "04·19",
                ).forEach { (k, v) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.bgChrome)
                            .border(0.5.dp, colors.hairSoft)
                            .padding(8.dp),
                    ) {
                        Text(k, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = colors.ink50)
                        Spacer(Modifier.height(2.dp))
                        Text(v, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.ink100)
                    }
                }
            }
        }

        // Tool list
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("SELECT · TOOL", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                Text("03 modules", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.accent)
            }

            tools.forEachIndexed { index, tool ->
                ToolCard(
                    index = index,
                    tool = tool,
                    onClick = { onOpen(tool.id) },
                )
            }

            // Footer
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.hairSoft)
                Text("UNIT CONVERTER · MODULE REVIEW", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.ink50)
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.hairSoft)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolCard(index: Int, tool: ToolEntry, onClick: () -> Unit) {
    val colors = LocalInstrumentColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPanel, RoundedCornerShape(6.dp))
            .border(1.dp, colors.hair, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Index
        Text(
            String.format("%02d", index + 1),
            style = MaterialTheme.typography.labelSmall,
            color = colors.ink50,
        )
        // Glyph
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(colors.bgPanel2, RoundedCornerShape(4.dp))
                .border(1.dp, colors.hairSoft, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when (tool.id) {
                    "unit" -> "⚖"
                    "currency" -> "$"
                    "calc" -> "fx"
                    else -> "?"
                },
                fontSize = if (tool.id == "calc") 16.sp else 20.sp,
                color = colors.ink90,
            )
        }
        // Body
        Column(modifier = Modifier.weight(1f)) {
            Text(tool.label, style = MaterialTheme.typography.headlineMedium, color = colors.ink100)
            Text(
                tool.desc,
                style = MaterialTheme.typography.labelSmall,
                color = colors.ink70,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(tool.count, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colors.ink50)
        }
        // Arrow
        Text(">", style = MaterialTheme.typography.bodyMedium, color = colors.ink50)
    }
}
