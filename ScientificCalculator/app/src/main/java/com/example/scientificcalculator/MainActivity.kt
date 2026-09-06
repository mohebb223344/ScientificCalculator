package com.example.scientificcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

private data class HistoryItem(val expression: String, val result: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorApp() }
    }
}

@Composable
fun CalculatorApp() {
    var dark by remember { mutableStateOf(true) }
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf(true) }
    var inverse by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(0.0) }
    var history by remember { mutableStateOf(listOf<HistoryItem>()) }
    var showHistory by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Surface(Modifier.fillMaxSize()) {
            if (showHistory) {
                HistoryScreen(history, { showHistory = false }, {
                    expression = it
                    result = ""
                    showHistory = false
                }, {
                    history = emptyList()
                })
            } else {
                CalculatorScreen(
                    expression, result, degree, inverse, memory,
                    { expression = it }, { result = it },
                    { degree = !degree }, { inverse = !inverse },
                    { memory = it }, { history = history + it },
                    { showHistory = true }, { dark = !dark }
                )
            }
        }
    }
}

@Composable
private fun CalculatorScreen(
    expression: String, result: String, degree: Boolean, inverse: Boolean,
    memory: Double,
    setExpression: (String) -> Unit, setResult: (String) -> Unit,
    toggleDegree: () -> Unit, toggleInverse: () -> Unit,
    setMemory: (Double) -> Unit, addHistory: (HistoryItem) -> Unit,
    showHistory: () -> Unit, toggleDark: () -> Unit
) {
    val scroll = rememberScrollState()

    fun add(s: String) = setExpression(expression + s)
    fun eval() {
        if (expression.isBlank()) return
        try {
            val v = Parser(expression, degree).parse()
            val out = format(v)
            setResult(out)
            addHistory(HistoryItem(expression, out))
        } catch (_: Exception) {
            setResult("Error")
        }
    }
    fun current(): Double? = try {
        if (expression.isBlank()) null else Parser(expression, degree).parse()
    } catch (_: Exception) { null }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight
        if (landscape) {
            Row(Modifier.fillMaxSize().padding(10.dp)) {
                Display(expression, result, Modifier.weight(0.9f))
                Spacer(Modifier.width(10.dp))
                Keypad(
                    modifier = Modifier.weight(1.5f),
                    degree, inverse, memory, ::add, { setExpression("") ; setResult("") },
                    { if (expression.isNotEmpty()) setExpression(expression.dropLast(1)) },
                    ::eval, toggleDegree, toggleInverse, setMemory, current, showHistory, toggleDark
                )
            }
        } else {
            Column(Modifier.fillMaxSize().padding(10.dp)) {
                Display(expression, result, Modifier.weight(1f))
                Keypad(
                    Modifier.fillMaxWidth(), degree, inverse, memory, ::add,
                    { setExpression(""); setResult("") },
                    { if (expression.isNotEmpty()) setExpression(expression.dropLast(1)) },
                    ::eval, toggleDegree, toggleInverse, setMemory, current, showHistory, toggleDark
                )
            }
        }
    }
}

@Composable
private fun Display(expression: String, result: String, modifier: Modifier) {
    Column(
        modifier.padding(8.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            expression.ifEmpty { "0" },
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        )
        Text(
            result,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Keypad(
    modifier: Modifier, degree: Boolean, inverse: Boolean, memory: Double,
    add: (String) -> Unit, clear: () -> Unit, back: () -> Unit,
    eval: () -> Unit, toggleDegree: () -> Unit, toggleInverse: () -> Unit,
    setMemory: (Double) -> Unit, current: () -> Double?, history: () -> Unit,
    theme: () -> Unit
) {
    fun b(s: String, action: () -> Unit = { add(s) }) =
        CalcButton(s, action, Modifier.weight(1f))

    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b(if (degree) "DEG" else "RAD", toggleDegree)
            b(if (inverse) "INV" else "NORM", toggleInverse)
            b("HIST", history)
            b("☀/☾", theme)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("MC") { setMemory(0.0) }
            b("MR") { add(format(memory)) }
            b("M+") { current()?.let { setMemory(memory + it) } }
            b("M−") { current()?.let { setMemory(memory - it) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b(if (inverse) "asin" else "sin") { add((if (inverse) "asin(" else "sin(")) }
            b(if (inverse) "acos" else "cos") { add((if (inverse) "acos(" else "cos(")) }
            b(if (inverse) "atan" else "tan") { add((if (inverse) "atan(" else "tan(")) }
            b("√") { add("sqrt(") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("ln") { add("ln(") }; b("log") { add("log(") }
            b("10ˣ") { add("10^(") }; b("eˣ") { add("exp(") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("π") { add("pi") }; b("e") { add("e") }
            b("("); b(")"); b("^")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("7"); b("8"); b("9"); b("÷") { add("/") }; b("%") { add("%") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("4"); b("5"); b("6"); b("×") { add("*") }; b("−") { add("-") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("1"); b("2"); b("3"); b("+"); b("!") 
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            b("0"); b("."); b("±") { add("-(") }
            b("⌫", back); b("C", clear)
            b("=", eval)
        }
    }
}

@Composable
private fun CalcButton(text: String, action: () -> Unit, modifier: Modifier) {
    val op = text in listOf("+","−","×","÷","^","%","!","=","C","⌫")
    Button(
        onClick = action,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (op) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        contentPadding = PaddingValues(2.dp)
    ) { Text(text, fontSize = 15.sp) }
}

@Composable
private fun HistoryScreen(
    history: List<HistoryItem>, close: () -> Unit,
    use: (String) -> Unit, clear: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Calculation History", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            TextButton(close) { Text("CLOSE") }
        }
        if (history.isEmpty()) {
            Text("No calculations yet.", modifier = Modifier.padding(top = 30.dp))
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                history.asReversed().forEach {
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { use(it.expression) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(it.expression, fontSize = 17.sp)
                            Text(it.result, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Button(clear, Modifier.fillMaxWidth()) { Text("CLEAR HISTORY") }
        }
    }
}

private fun format(x: Double): String {
    if (!x.isFinite()) return "Error"
    if (abs(x) < 1e-12) return "0"
    if (x == x.toLong().toDouble()) return x.toLong().toString()
    return "%.12f".format(x).trimEnd('0').trimEnd('.')
}

/* -------------------- Scientific Parser -------------------- */

private class Parser(
    private val s: String,
    private val degrees: Boolean
) {
    private var p = 0

    fun parse(): Double {
        val x = expression()
        skip()
        if (p != s.length) error("Unexpected input")
        return x
    }

    private fun expression(): Double {
        var x = term()
        while (true) {
            skip()
            x = when {
                eat('+') -> x + term()
                eat('-') -> x - term()
                else -> return x
            }
        }
    }

    private fun term(): Double {
        var x = power()
        while (true) {
            skip()
            x = when {
                eat('*') -> x * power()
                eat('/') -> {
                    val d = power()
                    if (d == 0.0) error("Division by zero")
                    x / d
                }
                else -> return x
            }
        }
    }

    private fun power(): Double {
        var x = unary()
        skip()
        if (eat('^')) x = x.pow(power())
        return x
    }

    private fun unary(): Double {
        skip()
        return when {
            eat('+') -> unary()
            eat('-') -> -unary()
            else -> postfix()
        }
    }

    private fun postfix(): Double {
        var x = primary()
        while (true) {
            skip()
            when {
                eat('!') -> x = factorial(x)
                eat('%') -> x /= 100.0
                else -> return x
            }
        }
    }

    private fun primary(): Double {
        skip()
        if (eat('(')) {
            val x = expression()
            if (!eat(')')) error("Missing )")
            return x
        }
        if (p >= s.length) error("Expected value")

        if (s[p].isDigit() || s[p] == '.') return number()

        if (s[p].isLetter()) {
            val name = identifier().lowercase()
            return when (name) {
                "pi" -> PI
                "e" -> E
                else -> {
                    val arg = argument()
                    val a = if (degrees) Math.toRadians(arg) else arg
                    when (name) {
                        "sqrt" -> sqrt(arg)
                        "sin" -> sin(a)
                        "cos" -> cos(a)
                        "tan" -> tan(a)
                        "asin" -> {
                            val v = asin(arg)
                            if (degrees) Math.toDegrees(v) else v
                        }
                        "acos" -> {
                            val v = acos(arg)
                            if (degrees) Math.toDegrees(v) else v
                        }
                        "atan" -> {
                            val v = atan(arg)
                            if (degrees) Math.toDegrees(v) else v
                        }
                        "ln" -> ln(arg)
                        "log" -> log10(arg)
                        "exp" -> exp(arg)
                        "abs" -> abs(arg)
                        "floor" -> floor(arg)
                        "ceil" -> ceil(arg)
                        else -> error("Unknown function $name")
                    }
                }
            }
        }
        error("Invalid input")
    }

    private fun argument(): Double {
        skip()
        if (eat('(')) {
            val x = expression()
            if (!eat(')')) error("Missing )")
            return x
        }
        return primary()
    }

    private fun number(): Double {
        val start = p
        while (p < s.length && (s[p].isDigit() || s[p] == '.')) p++
        if (p < s.length && (s[p] == 'e' || s[p] == 'E')) {
            p++
            if (p < s.length && (s[p] == '+' || s[p] == '-')) p++
            while (p < s.length && s[p].isDigit()) p++
        }
        return s.substring(start, p).toDouble()
    }

    private fun identifier(): String {
        val start = p
        while (p < s.length && (s[p].isLetter() || s[p].isDigit())) p++
        return s.substring(start, p)
    }

    private fun factorial(x: Double): Double {
        if (x < 0 || x != floor(x) || x > 170) error("Invalid factorial")
        var r = 1.0
        for (i in 2..x.toInt()) r *= i
        return r
    }

    private fun eat(c: Char): Boolean {
        skip()
        if (p < s.length && s[p] == c) {
            p++
            return true
        }
        return false
    }

    private fun skip() {
        while (p < s.length && s[p].isWhitespace()) p++
    }
}
