package com.sideproject.unitconverter.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sideproject.unitconverter.data.fmt
import com.sideproject.unitconverter.ui.components.*
import com.sideproject.unitconverter.ui.theme.*

@Composable
fun CalculatorScreen(
    onBack: () -> Unit,
    onSendToConverter: (String) -> Unit,
) {
    val colors = LocalInstrumentColors.current

    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var memory by remember { mutableDoubleStateOf(0.0) }
    var tape by remember { mutableStateOf(listOf<TapeEntry>()) }
    var scientific by remember { mutableStateOf(false) }

    // Evaluate expression
    LaunchedEffect(expr) {
        if (expr.isEmpty()) {
            result = ""
            error = ""
            return@LaunchedEffect
        }
        val v = evalSafe(expr)
        if (v.isNaN()) {
            result = ""
            error = "SYNTAX"
        } else {
            result = fmt(v)
            error = ""
        }
    }

    val push: (String) -> Unit = { s -> expr += s }
    val clear: () -> Unit = { expr = ""; error = "" }
    val bksp: () -> Unit = { if (expr.isNotEmpty()) expr = expr.dropLast(1) }
    val equals: () -> Unit = {
        val v = evalSafe(expr)
        if (!v.isNaN()) {
            tape = (listOf(TapeEntry(expr, fmt(v), System.currentTimeMillis())) + tape).take(10)
            expr = fmt(v)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPage),
    ) {
        InstrumentTopBar(
            title = "CALCULATOR",
            subtitle = if (scientific) "scientific mode" else "standard",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Display
            CalcDisplay(
                expr = expr,
                result = result,
                error = error,
                memory = memory,
                tapeCount = tape.size,
                scientific = scientific,
                onToggleSci = { scientific = !scientific },
            )

            // Memory row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                listOf(
                    "MC" to { memory = 0.0 },
                    "MS" to {
                        val v = evalSafe(expr)
                        if (!v.isNaN()) memory = v
                    },
                    "MR" to { expr += fmt(memory) },
                    "M+" to {
                        val v = evalSafe(expr)
                        if (!v.isNaN()) memory += v
                    },
                    "M−" to {
                        val v = evalSafe(expr)
                        if (!v.isNaN()) memory -= v
                    },
                ).forEach { (label, action) ->
                    CalcKey(
                        label = label,
                        modifier = Modifier.weight(1f),
                        kind = "mem",
                        onClick = action,
                    )
                }
            }

            // Scientific row
            if (scientific) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    listOf(
                        "sin" to { push("sin(") },
                        "cos" to { push("cos(") },
                        "tan" to { push("tan(") },
                        "log" to { push("log(") },
                        "ln" to { push("ln(") },
                    ).forEach { (label, action) ->
                        CalcKey(label = label, modifier = Modifier.weight(1f), kind = "sci", onClick = action)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    listOf(
                        "√" to { push("√(") },
                        "x²" to { push("**2") },
                        "xʸ" to { push("**") },
                        "π" to { push("π") },
                        "e" to { push("e") },
                    ).forEach { (label, action) ->
                        CalcKey(label = label, modifier = Modifier.weight(1f), kind = "sci", onClick = action)
                    }
                }
            }

            // Main keypad - 4 columns
            val keyRows = listOf(
                listOf(
                    KeyDef("C", "op", clear),
                    KeyDef("( )", "op") {
                        push(if (expr.count { it == '(' } > expr.count { it == ')' }) ")" else "(")
                    },
                    KeyDef("%", "op") { push("%") },
                    KeyDef("÷", "op") { push("÷") },
                ),
                listOf(
                    KeyDef("7", "num") { push("7") },
                    KeyDef("8", "num") { push("8") },
                    KeyDef("9", "num") { push("9") },
                    KeyDef("×", "op") { push("×") },
                ),
                listOf(
                    KeyDef("4", "num") { push("4") },
                    KeyDef("5", "num") { push("5") },
                    KeyDef("6", "num") { push("6") },
                    KeyDef("−", "op") { push("−") },
                ),
                listOf(
                    KeyDef("1", "num") { push("1") },
                    KeyDef("2", "num") { push("2") },
                    KeyDef("3", "num") { push("3") },
                    KeyDef("+", "op") { push("+") },
                ),
            )

            keyRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { key ->
                        CalcKey(
                            label = key.label,
                            modifier = Modifier.weight(1f),
                            kind = key.kind,
                            onClick = key.action,
                        )
                    }
                }
            }

            // Bottom row: 0, ., =, ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CalcKey("0", Modifier.weight(2f), "num") { push("0") }
                CalcKey(".", Modifier.weight(1f), "num") { push(".") }
                CalcKey("=", Modifier.weight(1f), "eq", equals)
            }

            // Backspace
            CalcKey("⌫", Modifier.fillMaxWidth(), "op", bksp)

            // Send to converter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgPanel, RoundedCornerShape(6.dp))
                    .border(
                        1.dp,
                        if (result.isNotEmpty()) colors.accentHair else colors.hairSoft,
                        RoundedCornerShape(6.dp),
                    )
                    .clickable(enabled = result.isNotEmpty()) { onSendToConverter(result) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("↗", fontSize = 14.sp, color = if (result.isNotEmpty()) colors.accent else colors.ink50)
                Text(
                    "SEND TO UNIT CONVERTER",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (result.isNotEmpty()) colors.accent else colors.ink50,
                )
                Text(
                    result.ifEmpty { "—" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                    color = colors.ink100,
                )
            }

            // Tape
            if (tape.isNotEmpty()) {
                ModuleSection(kicker = "TAPE · RECENT", sub = "× clear") {
                    Column {
                        tape.forEach { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expr = t.result }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
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
                                    t.expr,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.ink70,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text("=", style = MaterialTheme.typography.bodyMedium, color = colors.ink50, modifier = Modifier.padding(horizontal = 8.dp))
                                Text(
                                    t.result,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                    color = colors.ink100,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CalcDisplay(
    expr: String,
    result: String,
    error: String,
    memory: Double,
    tapeCount: Int,
    scientific: Boolean,
    onToggleSci: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))

    InstrumentPanel {
        // Meta row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .drawBehind {
                    drawLine(
                        colors.hairSoft,
                        Offset(0f, size.height),
                        Offset(size.width, size.height),
                        pathEffect = dashEffect,
                    )
                }
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("EXPRESSION", style = MaterialTheme.typography.titleSmall, color = colors.ink50)
            Box(
                modifier = Modifier
                    .background(colors.bgPanel2, RoundedCornerShape(3.dp))
                    .border(1.dp, colors.hairSoft, RoundedCornerShape(3.dp))
                    .clickable(onClick = onToggleSci)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    if (scientific) "STD" else "SCI",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
            }
        }

        // Expression
        Text(
            text = expr.ifEmpty { "0" },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = if (expr.isEmpty()) colors.ink30 else colors.ink70,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
        )

        // Divider
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = colors.hairSoft)
        Spacer(Modifier.height(6.dp))

        // Result
        Text(
            text = if (error.isNotEmpty()) error
            else "= ${result.ifEmpty { "0" }}",
            style = if (error.isNotEmpty())
                MaterialTheme.typography.displayMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
            else
                MaterialTheme.typography.displayLarge,
            color = if (error.isNotEmpty()) Warn else colors.ink100,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Info
        Spacer(Modifier.height(10.dp))
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
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("MEM", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                Text(fmt(memory), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = colors.ink100)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("TAPE", style = MaterialTheme.typography.labelSmall, color = colors.ink50)
                Text(tapeCount.toString().padStart(2, '0'), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = colors.ink100)
            }
        }
    }
}

@Composable
private fun CalcKey(
    label: String,
    modifier: Modifier = Modifier,
    kind: String = "num",
    onClick: () -> Unit,
) {
    val colors = LocalInstrumentColors.current
    val (bg, borderColor, textColor) = when (kind) {
        "op" -> Triple(colors.bgChrome, colors.hairSoft, colors.accent)
        "eq" -> Triple(colors.accentSoft, colors.accentHair, colors.accent)
        "sci" -> Triple(colors.bgChrome, colors.hairSoft, colors.accent)
        "mem" -> Triple(colors.bgPanel, colors.hairSoft, colors.ink70)
        else -> Triple(colors.bgPanel, colors.hairSoft, colors.ink100)
    }

    val fontSize = when (kind) {
        "sci" -> 12.sp
        "mem" -> 11.sp
        "eq" -> 22.sp
        else -> 18.sp
    }

    Box(
        modifier = modifier
            .height(if (kind == "mem" || kind == "sci") 40.dp else 52.dp)
            .background(bg, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontFamily = JetBrainsMono,
            fontWeight = if (kind == "eq") FontWeight.Bold else FontWeight.Medium,
            fontSize = fontSize,
            color = textColor,
        )
    }
}

private data class KeyDef(val label: String, val kind: String, val action: () -> Unit)
private data class TapeEntry(val expr: String, val result: String, val ts: Long)

private fun evalSafe(s: String): Double {
    if (s.isBlank()) return Double.NaN
    var x = s
        .replace("×", "*")
        .replace("÷", "/")
        .replace("−", "-")
        .replace("π", Math.PI.toString())
        .replace(Regex("e(?![0-9.])"), Math.E.toString())

    // Handle functions
    x = x.replace("√(", "Math.sqrt(")
        .replace("sin(", "Math.sin(")
        .replace("cos(", "Math.cos(")
        .replace("tan(", "Math.tan(")
        .replace("log(", "Math.log10(")
        .replace("ln(", "Math.log(")

    // Percent
    x = x.replace(Regex("(\\d+(?:\\.\\d+)?)%")) { "(${it.groupValues[1]}/100)" }

    // Power
    x = x.replace("**", "^")

    return try {
        evaluateExpression(x)
    } catch (_: Exception) {
        Double.NaN
    }
}

// Simple recursive-descent expression evaluator
private fun evaluateExpression(expr: String): Double {
    val tokens = tokenize(expr)
    val parser = Parser(tokens)
    val result = parser.parseExpression()
    return result
}

private fun tokenize(expr: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < expr.length) {
        val c = expr[i]
        when {
            c.isWhitespace() -> i++
            c.isDigit() || c == '.' -> {
                val sb = StringBuilder()
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || (expr[i] == '-' && i > 0 && expr[i - 1] == 'E'))) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
            }
            c.isLetter() -> {
                val sb = StringBuilder()
                while (i < expr.length && (expr[i].isLetterOrDigit() || expr[i] == '.')) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
            }
            else -> {
                tokens.add(c.toString())
                i++
            }
        }
    }
    return tokens
}

private class Parser(private val tokens: List<String>) {
    private var pos = 0

    fun parseExpression(): Double {
        var left = parseTerm()
        while (pos < tokens.size && tokens[pos] in listOf("+", "-")) {
            val op = tokens[pos++]
            val right = parseTerm()
            left = if (op == "+") left + right else left - right
        }
        return left
    }

    private fun parseTerm(): Double {
        var left = parsePower()
        while (pos < tokens.size && tokens[pos] in listOf("*", "/")) {
            val op = tokens[pos++]
            val right = parsePower()
            left = if (op == "*") left * right else left / right
        }
        return left
    }

    private fun parsePower(): Double {
        var base = parseUnary()
        while (pos < tokens.size && tokens[pos] == "^") {
            pos++
            val exp = parseUnary()
            base = Math.pow(base, exp)
        }
        return base
    }

    private fun parseUnary(): Double {
        if (pos < tokens.size && tokens[pos] == "-") {
            pos++
            return -parseAtom()
        }
        if (pos < tokens.size && tokens[pos] == "+") {
            pos++
        }
        return parseAtom()
    }

    private fun parseAtom(): Double {
        if (pos >= tokens.size) return 0.0

        val token = tokens[pos]

        // Parenthesized expression
        if (token == "(") {
            pos++
            val value = parseExpression()
            if (pos < tokens.size && tokens[pos] == ")") pos++
            return value
        }

        // Functions
        if (token.startsWith("Math.")) {
            pos++
            val funcName = token
            if (pos < tokens.size && tokens[pos] == "(") {
                pos++
                val arg = parseExpression()
                if (pos < tokens.size && tokens[pos] == ")") pos++
                return when (funcName) {
                    "Math.sqrt" -> Math.sqrt(arg)
                    "Math.sin" -> Math.sin(arg)
                    "Math.cos" -> Math.cos(arg)
                    "Math.tan" -> Math.tan(arg)
                    "Math.log10" -> Math.log10(arg)
                    "Math.log" -> Math.log(arg)
                    else -> Double.NaN
                }
            }
            return Double.NaN
        }

        // Number
        return try {
            pos++
            token.toDouble()
        } catch (_: NumberFormatException) {
            Double.NaN
        }
    }
}
