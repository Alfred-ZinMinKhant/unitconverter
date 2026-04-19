package com.sideproject.unitconverter.data

import kotlin.math.abs
import kotlin.math.log10

data class CategoryDef(
    val id: String,
    val label: String,
    val base: String,
)

data class UnitDef(
    val sym: String,
    val name: String,
    val factor: Double = 1.0,
    val toBaseFn: ((Double) -> Double)? = null,
    val fromBaseFn: ((Double) -> Double)? = null,
)

data class Comparison(val max: Double, val ref: String)

val CATEGORIES = listOf(
    CategoryDef("length", "Length", "m"),
    CategoryDef("mass", "Mass", "kg"),
    CategoryDef("temp", "Temp", "°C"),
    CategoryDef("area", "Area", "m²"),
    CategoryDef("volume", "Volume", "L"),
    CategoryDef("speed", "Speed", "m/s"),
    CategoryDef("time", "Time", "s"),
    CategoryDef("data", "Data", "B"),
    CategoryDef("energy", "Energy", "J"),
    CategoryDef("pressure", "Pressure", "Pa"),
)

val CURRENCY_CATEGORY = CategoryDef("currency", "Currency", "USD")

val UNITS: Map<String, List<UnitDef>> = mapOf(
    "length" to listOf(
        UnitDef("m", "Meter", 1.0),
        UnitDef("km", "Kilometer", 1000.0),
        UnitDef("cm", "Centimeter", 0.01),
        UnitDef("mm", "Millimeter", 0.001),
        UnitDef("µm", "Micrometer", 1e-6),
        UnitDef("mi", "Mile", 1609.344),
        UnitDef("yd", "Yard", 0.9144),
        UnitDef("ft", "Foot", 0.3048),
        UnitDef("in", "Inch", 0.0254),
        UnitDef("nmi", "Naut. mile", 1852.0),
        UnitDef("ly", "Light-year", 9.461e15),
    ),
    "mass" to listOf(
        UnitDef("kg", "Kilogram", 1.0),
        UnitDef("g", "Gram", 0.001),
        UnitDef("mg", "Milligram", 1e-6),
        UnitDef("µg", "Microgram", 1e-9),
        UnitDef("t", "Tonne", 1000.0),
        UnitDef("lb", "Pound", 0.45359237),
        UnitDef("oz", "Ounce", 0.028349523125),
        UnitDef("st", "Stone", 6.35029318),
        UnitDef("ct", "Carat", 0.0002),
    ),
    "temp" to listOf(
        UnitDef("°C", "Celsius", toBaseFn = { it }, fromBaseFn = { it }),
        UnitDef("°F", "Fahrenheit", toBaseFn = { (it - 32) * 5.0 / 9.0 }, fromBaseFn = { it * 9.0 / 5.0 + 32 }),
        UnitDef("K", "Kelvin", toBaseFn = { it - 273.15 }, fromBaseFn = { it + 273.15 }),
        UnitDef("°R", "Rankine", toBaseFn = { (it - 491.67) * 5.0 / 9.0 }, fromBaseFn = { it * 9.0 / 5.0 + 491.67 }),
    ),
    "area" to listOf(
        UnitDef("m²", "Square meter", 1.0),
        UnitDef("km²", "Square kilometer", 1e6),
        UnitDef("cm²", "Square centimeter", 1e-4),
        UnitDef("mm²", "Square millimeter", 1e-6),
        UnitDef("ft²", "Square foot", 0.09290304),
        UnitDef("in²", "Square inch", 0.00064516),
        UnitDef("yd²", "Square yard", 0.83612736),
        UnitDef("ac", "Acre", 4046.8564224),
        UnitDef("ha", "Hectare", 10000.0),
    ),
    "volume" to listOf(
        UnitDef("L", "Liter", 1.0),
        UnitDef("mL", "Milliliter", 0.001),
        UnitDef("m³", "Cubic meter", 1000.0),
        UnitDef("cm³", "Cubic cm", 0.001),
        UnitDef("gal", "Gallon (US)", 3.785411784),
        UnitDef("galUK", "Gallon (UK)", 4.54609),
        UnitDef("qt", "Quart", 0.946352946),
        UnitDef("pt", "Pint", 0.473176473),
        UnitDef("cup", "Cup", 0.2365882365),
        UnitDef("floz", "Fluid ounce", 0.0295735295625),
        UnitDef("tbsp", "Tablespoon", 0.01478676478125),
        UnitDef("tsp", "Teaspoon", 0.00492892159375),
    ),
    "speed" to listOf(
        UnitDef("m/s", "Meters/sec", 1.0),
        UnitDef("km/h", "Km/hour", 0.2777777778),
        UnitDef("mph", "Miles/hour", 0.44704),
        UnitDef("kn", "Knot", 0.5144444444),
        UnitDef("ft/s", "Feet/sec", 0.3048),
        UnitDef("c", "Speed of light", 299792458.0),
        UnitDef("Mach", "Mach (sea lvl)", 343.0),
    ),
    "time" to listOf(
        UnitDef("s", "Second", 1.0),
        UnitDef("ms", "Millisecond", 0.001),
        UnitDef("µs", "Microsecond", 1e-6),
        UnitDef("ns", "Nanosecond", 1e-9),
        UnitDef("min", "Minute", 60.0),
        UnitDef("h", "Hour", 3600.0),
        UnitDef("d", "Day", 86400.0),
        UnitDef("wk", "Week", 604800.0),
        UnitDef("mo", "Month (30d)", 2592000.0),
        UnitDef("yr", "Year (365d)", 31536000.0),
    ),
    "data" to listOf(
        UnitDef("B", "Byte", 1.0),
        UnitDef("bit", "Bit", 0.125),
        UnitDef("KB", "Kilobyte", 1000.0),
        UnitDef("MB", "Megabyte", 1e6),
        UnitDef("GB", "Gigabyte", 1e9),
        UnitDef("TB", "Terabyte", 1e12),
        UnitDef("PB", "Petabyte", 1e15),
        UnitDef("KiB", "Kibibyte", 1024.0),
        UnitDef("MiB", "Mebibyte", 1048576.0),
        UnitDef("GiB", "Gibibyte", 1073741824.0),
        UnitDef("TiB", "Tebibyte", 1.0995e12),
    ),
    "energy" to listOf(
        UnitDef("J", "Joule", 1.0),
        UnitDef("kJ", "Kilojoule", 1000.0),
        UnitDef("cal", "Calorie", 4.184),
        UnitDef("kcal", "Kilocalorie", 4184.0),
        UnitDef("Wh", "Watt-hour", 3600.0),
        UnitDef("kWh", "Kilowatt-hour", 3.6e6),
        UnitDef("eV", "Electronvolt", 1.602176634e-19),
        UnitDef("BTU", "BTU", 1055.05585),
        UnitDef("ftlb", "Foot-pound", 1.35581795),
    ),
    "pressure" to listOf(
        UnitDef("Pa", "Pascal", 1.0),
        UnitDef("kPa", "Kilopascal", 1000.0),
        UnitDef("MPa", "Megapascal", 1e6),
        UnitDef("bar", "Bar", 100000.0),
        UnitDef("atm", "Atmosphere", 101325.0),
        UnitDef("psi", "PSI", 6894.757293),
        UnitDef("mmHg", "mmHg / Torr", 133.322387),
        UnitDef("inHg", "inHg", 3386.389),
    ),
    "currency" to listOf(
        UnitDef("USD", "US Dollar", 1.0),
        UnitDef("EUR", "Euro", 1.0870),
        UnitDef("GBP", "Pound Sterling", 1.2650),
        UnitDef("JPY", "Japanese Yen", 0.00667),
        UnitDef("CNY", "Chinese Yuan", 0.1380),
        UnitDef("INR", "Indian Rupee", 0.01200),
        UnitDef("CAD", "Canadian Dollar", 0.7350),
        UnitDef("AUD", "Australian Dollar", 0.6580),
        UnitDef("CHF", "Swiss Franc", 1.1280),
        UnitDef("MMK", "Myanmar Kyat", 0.000476),
        UnitDef("THB", "Thai Baht", 0.02950),
        UnitDef("SGD", "Singapore Dollar", 0.7420),
        UnitDef("KRW", "South Korean Won", 0.000725),
        UnitDef("BRL", "Brazilian Real", 0.2030),
        UnitDef("MXN", "Mexican Peso", 0.05900),
        UnitDef("BTC", "Bitcoin", 68000.0),
        UnitDef("ETH", "Ethereum", 3500.0),
    ),
)

val COMPARISONS: Map<String, List<Comparison>> = mapOf(
    "length" to listOf(
        Comparison(1e-9, "an atomic radius"),
        Comparison(1e-6, "a virus"),
        Comparison(1e-3, "a human hair"),
        Comparison(0.01, "a grain of rice"),
        Comparison(0.1, "a credit card length"),
        Comparison(1.0, "an arm span"),
        Comparison(5.0, "a car length"),
        Comparison(30.0, "a blue whale"),
        Comparison(300.0, "the Eiffel Tower"),
        Comparison(8849.0, "Mt. Everest"),
        Comparison(1e6, "crossing a small country"),
        Comparison(1.2756e7, "Earth's diameter"),
        Comparison(1.496e11, "1 astronomical unit"),
        Comparison(Double.MAX_VALUE, "interstellar distance"),
    ),
    "mass" to listOf(
        Comparison(1e-6, "a dust particle"),
        Comparison(1e-3, "a grain of sand"),
        Comparison(0.1, "a AA battery"),
        Comparison(1.0, "a pineapple"),
        Comparison(80.0, "an adult human"),
        Comparison(600.0, "a grand piano"),
        Comparison(1500.0, "a small car"),
        Comparison(6000.0, "an elephant"),
        Comparison(150000.0, "a blue whale"),
        Comparison(Double.MAX_VALUE, "a cargo ship"),
    ),
    "temp" to listOf(
        Comparison(-100.0, "deep space"),
        Comparison(-40.0, "Antarctic winter"),
        Comparison(0.0, "water freezing"),
        Comparison(20.0, "a cool room"),
        Comparison(37.0, "body temperature"),
        Comparison(60.0, "a hot tub limit"),
        Comparison(100.0, "boiling water"),
        Comparison(232.0, "a pizza oven"),
        Comparison(1538.0, "molten iron"),
        Comparison(Double.MAX_VALUE, "surface of the Sun"),
    ),
    "area" to listOf(
        Comparison(1e-4, "a postage stamp"),
        Comparison(0.1, "a laptop screen"),
        Comparison(10.0, "a parking space"),
        Comparison(100.0, "a studio apartment"),
        Comparison(500.0, "a basketball court"),
        Comparison(10000.0, "a city block"),
        Comparison(1e6, "Central Park"),
        Comparison(Double.MAX_VALUE, "a small country"),
    ),
    "volume" to listOf(
        Comparison(0.005, "a teaspoon"),
        Comparison(0.25, "a coffee cup"),
        Comparison(2.0, "a soda bottle"),
        Comparison(60.0, "a bathtub"),
        Comparison(1000.0, "a shipping container"),
        Comparison(Double.MAX_VALUE, "an Olympic pool"),
    ),
    "speed" to listOf(
        Comparison(0.1, "a walking snail"),
        Comparison(1.5, "a strolling walk"),
        Comparison(10.0, "a sprinting human"),
        Comparison(30.0, "city driving"),
        Comparison(70.0, "a cheetah"),
        Comparison(250.0, "an F1 car"),
        Comparison(343.0, "the speed of sound"),
        Comparison(1e6, "a rocket re-entering"),
        Comparison(Double.MAX_VALUE, "relativistic speed"),
    ),
    "time" to listOf(
        Comparison(1e-6, "a CPU clock tick"),
        Comparison(0.1, "a blink"),
        Comparison(10.0, "a deep breath"),
        Comparison(3600.0, "an hour-long meeting"),
        Comparison(86400.0, "one full day"),
        Comparison(604800.0, "a work week"),
        Comparison(2.6e6, "a month"),
        Comparison(3.15e7, "one orbit around the Sun"),
        Comparison(Double.MAX_VALUE, "a geological era"),
    ),
    "data" to listOf(
        Comparison(1.0, "one character"),
        Comparison(1000.0, "a short SMS"),
        Comparison(5e6, "a high-res photo"),
        Comparison(5e9, "a movie download"),
        Comparison(1e12, "a hard drive"),
        Comparison(Double.MAX_VALUE, "a small data center"),
    ),
    "energy" to listOf(
        Comparison(1.0, "lifting an apple 1m"),
        Comparison(1000.0, "a hand clap"),
        Comparison(1e6, "running a marathon"),
        Comparison(4.184e6, "a slice of pizza"),
        Comparison(3.6e6, "running a hairdryer 1h"),
        Comparison(1e9, "a lightning strike"),
        Comparison(Double.MAX_VALUE, "a small earthquake"),
    ),
    "currency" to listOf(
        Comparison(5.0, "a cup of coffee"),
        Comparison(20.0, "a fast-food meal"),
        Comparison(100.0, "a nice dinner"),
        Comparison(1000.0, "a weekend trip"),
        Comparison(30000.0, "a new car"),
        Comparison(500000.0, "a starter home"),
        Comparison(Double.MAX_VALUE, "a seed round"),
    ),
    "pressure" to listOf(
        Comparison(100.0, "a whisper"),
        Comparison(1000.0, "deep breathing"),
        Comparison(101325.0, "sea-level atmosphere"),
        Comparison(1e6, "a car tire"),
        Comparison(1e7, "a scuba tank"),
        Comparison(Double.MAX_VALUE, "the deep ocean"),
    ),
)

fun toBase(unit: UnitDef, value: Double): Double =
    unit.toBaseFn?.invoke(value) ?: (value * unit.factor)

fun fromBase(unit: UnitDef, value: Double): Double =
    unit.fromBaseFn?.invoke(value) ?: (value / unit.factor)

fun convert(from: UnitDef, to: UnitDef, value: Double): Double =
    fromBase(to, toBase(from, value))

fun comparisonFor(catId: String, baseValue: Double): String {
    val list = COMPARISONS[catId] ?: return ""
    val v = abs(baseValue)
    for (c in list) if (v <= c.max) return c.ref
    return list.lastOrNull()?.ref ?: ""
}

fun fmt(value: Double): String {
    if (!value.isFinite()) return "—"
    if (value == 0.0) return "0"
    val a = abs(value)
    if (a >= 1e9 || a < 1e-4) {
        return value.toBigDecimal().toEngineeringString().let {
            // Fallback to scientific notation
            "%.4e".format(value)
                .replace("e+0", "e")
                .replace("e+", "e")
                .replace("e-0", "e-")
                .replace("e0", "")
        }
    }
    val s = "%.7g".format(value)
    return if ('.' in s) s.trimEnd('0').trimEnd('.') else s
}

fun scaleBarPosition(catId: String, baseValue: Double): Float {
    val comps = COMPARISONS[catId] ?: return 0.5f
    val v = abs(baseValue)
    if (v <= 0 || comps.size < 2) return 0.5f
    val logMin = log10(comps[0].max.coerceAtLeast(1e-12))
    val logMax = log10(comps[comps.size - 2].max.coerceAtLeast(1e-12))
    val logV = log10(v.coerceAtLeast(1e-12))
    return ((logV - logMin) / (logMax - logMin)).toFloat().coerceIn(0f, 1f)
}

fun timeAgo(ts: Long): String {
    val s = (System.currentTimeMillis() - ts) / 1000
    return when {
        s < 60 -> "${s}s"
        s < 3600 -> "${s / 60}m"
        s < 86400 -> "${s / 3600}h"
        else -> "${s / 86400}d"
    }
}

data class FormulaResult(val rule: String, val subst: String, val result: String)

fun buildFormula(catId: String, from: UnitDef, to: UnitDef, parsed: Double, isValid: Boolean): FormulaResult {
    if (catId == "temp") {
        val map = mapOf(
            "°C" to ("x" to "b"),
            "°F" to ("(x - 32) * 5/9" to "b * 9/5 + 32"),
            "K" to ("x - 273.15" to "b + 273.15"),
            "°R" to ("(x - 491.67) * 5/9" to "b * 9/5 + 491.67"),
        )
        val a = map[from.sym] ?: ("x" to "b")
        val b = map[to.sym] ?: ("x" to "b")
        val rule = "base = ${a.first};  out = ${b.second.replace("b", "base")}"
        val baseV = if (isValid) toBase(from, parsed) else Double.NaN
        val subst = if (isValid) "base = ${fmt(baseV)} °C;  out = ${fmt(fromBase(to, baseV))} ${to.sym}" else "—"
        val result = if (isValid) "${fmt(parsed)} ${from.sym} = ${fmt(convert(from, to, parsed))} ${to.sym}" else "—"
        return FormulaResult(rule, subst, result)
    }
    val ratio = from.factor / to.factor
    return FormulaResult(
        rule = "out = in * (${from.factor} / ${to.factor})",
        subst = if (isValid) "out = ${fmt(parsed)} * ${fmt(ratio)}" else "—",
        result = if (isValid) "${fmt(parsed)} ${from.sym} = ${fmt(parsed * ratio)} ${to.sym}" else "—",
    )
}
