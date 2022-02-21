package calculator

import java.math.BigInteger
class UnknownVariableException: Exception("Unknown variable")

object Task {
    private val varMap = mutableMapOf<String, String>()

    private fun groupOperators(input: MutableList<String>): MutableList<String> {
        for (i in input.indices) {
            val number = Regex("""\d+""").find(input[i])?.value ?: continue
            val resultMember = if (input[i].toList().count { it == '-' } % 2 == 0) number else "-$number"
            input[i] = resultMember
        }
        return input
    }

    private fun getData(str: String): MutableList<String> {
        val result = mutableListOf<String>()
        val matchResult = Regex("""[+-]*\s*[0-9]+\s*""").findAll(str)
        matchResult.forEach { result.add(it.value) }
        return groupOperators(result)
    }

    fun setValue(input: String) {
        val name = input.substringBefore('=').trim()
        val value = input.substringAfter('=').trim()
        if (!name.matches(Regex("[a-zA-Z]+"))) {
            println("Invalid identifier"); return
        }
        if (value in varMap.keys) varMap[name] = varMap[value]!! else
            if (value.matches(Regex("[-+]?\\d+")))
                varMap[name] = value
            else println("Invalid assignment")
    }

    fun cleanSpaces(input: String) = input.replace(Regex("\\s+"), "")

    fun containVar(input: String): Boolean {
        var matchResult = Regex("[a-zA-Z]\\w*").findAll(input)
        for (v in matchResult) if (v.value !in varMap.keys) return false
        return true
    }

    fun varTask(input: String): String {
        var result: String = input
        for (v in varMap) {
            result = result.replace(Regex("${v.key}"), v.value)
        }
        return result
    }

    fun calcDigital(input: String) {
        val task = getData(input)
        try {
            println(task.sumOf { it.toInt() })
        } catch (e:Exception) {
            println(task.sumOf { it.toBigInteger() })
        }
    }


    fun analysis(input: String): String {
        return if ("+$input".matches(Regex("(([+-]\\s*)+[0-9]+\\s*)+"))) "digital"
        else if (input.matches("[a-zA-Z]\\w*\\s*=.*".toRegex())) "setVal"
        else if ("+$input".matches("(([+-]\\s*)+\\w+\\s*)+".toRegex())) "withVar"
        else if (input == "/exit") "exit"
        else if (input == "/help") "help"
        else if (input == "") "nothing"
        else if (input.first() == '/') "Unknown"
        else "Invalid"
    }

    private fun isGreat(operator1: String, operator2: String): Int {
        val priority = mutableMapOf("+" to 0, "-" to 0, "*" to 1, "/" to 1)
        if (operator1 !in priority.keys || operator2 !in priority.keys) return -1
        return if (priority[operator1]!! > priority[operator2]!!) 1 else 0
    }


    fun toPostfix(input: String): List<String> {
        var task: String = input.replace("\\-{2}".toRegex(), "+")
        task = task.replace("\\++".toRegex(), "+")
        task = task.replace("\\+-|-\\+".toRegex(), "-")
        val elements = mutableListOf<String>()
        var el = ""
        for (i in  task.indices) {
            if (task[i] in '0'..'9' || task[i] in 'a'..'z' || task[i] in 'A'..'Z') el += task[i]
            else {
                if (el != "") elements.add(el)
                elements.add(task[i].toString())
                el = ""
            }
        }
        if (el != "") elements.add(el)
        // minus
        for(i in 0 until elements.size-1) {
            if (elements[i] == "-" && elements[i+1] in "0".."9" ) {
                elements[i+1] = "-"+elements[i+1]
                if (i != 0) {
                    if (elements[i-1] != "(") elements[i] = "+"
                } else elements.removeAt(i)
            }
        }

        val stack = mutableListOf<String>()
        val result = mutableListOf<String>()
        for (el in elements) {
            if (el.matches(Regex("-?\\d+|-?\\w+"))) result.add(el)
            else if (stack.isEmpty() || stack.last() == "(") stack.add(el)
            else if (isGreat(el, stack.last()) == 1) stack.add(el) else
                if (isGreat(el, stack.last()) == 0) {
                    while (stack.size != 0) {
                        result.add(stack.removeAt(stack.lastIndex))
                        if (stack.isEmpty() || stack.last() == "(" || isGreat(el, stack.last()) == 1) break
                    }
                    stack.add(el)
                } else if (el == "(") stack.add(el)
                else if (el == ")") {
                    while (stack.size != 0) {
                        result.add(stack.removeAt(stack.lastIndex))
                        if (stack.isEmpty()) result.add("???") else
                            if ( stack.last() == "(") {stack.removeAt(stack.lastIndex); break}
                    }
                }
        }
        while (stack.size != 0) {
            result.add(stack.removeAt(stack.lastIndex))
        }
        return result
    }

    fun calcFromPostfix(input: List<String>) {
        val stack = mutableListOf<String>()
        try {
            for (i in input.indices) {
                if (input[i].matches(Regex("-?\\d+"))) stack.add(input[i])
                else if (input[i].matches(Regex("-?\\w+"))) stack.add(varMap[input[i]] ?: throw UnknownVariableException()) else
                {
                    val a = stack.removeAt(stack.lastIndex)
                    val b = stack.removeAt(stack.lastIndex)
                    var result = ""
                    result = when (input[i]) {
                        "+" -> try { (b.toInt() + a.toInt()).toString() } catch (e:Exception) {(b.toBigInteger() + a.toBigInteger()).toString() }
                        "-" -> try { (b.toInt() - a.toInt()).toString() } catch (e:Exception) {(b.toBigInteger() - a.toBigInteger()).toString() }
                        "*" -> try { (b.toInt() * a.toInt()).toString() } catch (e:Exception) {(b.toBigInteger() * a.toBigInteger()).toString() }
                        "/" -> try { (b.toInt() / a.toInt()).toString() } catch (e:Exception) {(b.toBigInteger() / a.toBigInteger()).toString() }
                        else -> throw Exception()
                    }
                    stack.add(result)
                }
            }
            println(stack.last())
        } catch (e: UnknownVariableException) {
            println("Unknown variable")
        }
        catch (e: Exception) {
            println("Invalid expression")
        }
    }
}

fun main() {
    while (true) {
        val input = Task.cleanSpaces(readLine()!!)

        when (Task.analysis(input)) {
            "digital" -> {
                Task.calcDigital(input)
            }
            "setVal" -> {
                if (input.matches("[a-zA-Z]+\\s*=.*".toRegex())) "setVal"
                Task.setValue(input)
            }
            "withVar" -> {
                if (!Task.containVar(input)) println("Unknown variable")
                else {
                    val digitalTask = Task.varTask(input)
                    Task.calcDigital(digitalTask)
                }
            }
            "exit" -> {
                println("Bye!"); break
            }
            "help" -> {
                println("The program support (+, -, *, /) operators of numbers,BigInteger and variables \n For example, input: " +
                        "\n -2 + 4 - 5 + 6 \n a = 5 \n b=10 \n a + b - (10 * b - a)")
            }
            "nothing" -> {}
            "Unknown" -> {
                println("Unknown command")
            }
            else -> Task.calcFromPostfix(Task.toPostfix(input))
        }
    }
}

