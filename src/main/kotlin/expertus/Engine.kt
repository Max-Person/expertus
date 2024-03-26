package expertus

import kotlin.reflect.KClass

/**
 * TODO Class Description
 *
 * @author Marat Gumerov
 * @since 07.03.2024
 */
class Engine {
    private val rules = mutableListOf<Rule>()
    fun addRules(vararg rules: Rule){
        rules.forEachIndexed{i, rule ->
            rule.number = i+1
            this.rules.add(rule)
        }
    }

    val variables = mutableMapOf<String, Any>()
    fun init(vararg initials: RuleEffect){
        initials.forEach { it.apply(variables) }
    }

    fun run(){
        var applied = true
        val rulePool = rules.toMutableSet()
        var count = 1
        while(applied){
            println("${count++}-й цикл прохода по базе знаний")
            println("Происходит поиск правил, которые могут выполниться " +
                    "для текущих значений переменных: ${variables.keys.varState()}")
            applied = false
            val ruleQueue = rulePool.toList()
            ruleQueue.forEach{rule ->
                val prevVarState = rule.condition.allVariables().varState()
                if(rule.tryApplying(variables)){
                    println("Для $rule имеем $prevVarState.")
                    println("Соответственно, $rule признано истинным, " +
                            "и в соответствии с ним переменная ${rule.effects.first().variable} " +
                            "получает значение ${variables[rule.effects.first().variable]}.")
                    println("Исключаем $rule из дальнейшего рассмотрения")
                    rulePool.remove(rule)
                    applied = true
                }
            }
        }
        println("Больше выполнимых правил нет, поэтому прямая цепочка рассуждений завершается")
    }

    fun <E: Enum<E>> tryDetermineValue(enum: KClass<E>) = tryDetermineValue(enum.java.simpleName)
    fun tryDetermineValue(variable: String) = tryDetermineValue(variable, listOf()).value

    private fun tryDetermineValue(variable: String, ruleStack: List<Rule>): Returned {
        if(variables.containsKey(variable)) {
            println("Значение переменной $variable известно - ${variables[variable]}")
            return variables[variable].r
        }

        val applicableRules = rules.filter{it.effects.any { effect -> effect.variable == variable}}
        println("Для определения значения переменной $variable, найдем правила, " +
                "в заключительной части которых она находится. " +
                "Такими правилами являются: ${applicableRules.joinToString()}")

        for(rule in applicableRules){
            if(ruleStack.contains(rule)) {
                if(rule.tryApplying(variables)){
                    println("Т.к. более ранние правила не задали переменной $variable значение, то применим $rule - " +
                            "переменная $variable получает значение ${variables[variable]}")
                    return variables[variable].returnUntil(rule)
                }
                println("т.к. $rule уже содержится в стеке, оно не может быть применено, пропускаем")
                continue
            }

            val usedVariables = rule.condition.allVariables()
            println("$rule помещается в стек. Определим переменные в ее условной части. " +
                    "Такими переменными являются: ${usedVariables.joinToString()}")
            usedVariables.forEach{
                println("Рассмотрим переменную $it")
                val r = tryDetermineValue(it, ruleStack.plus(rule))
                if(r.returnUntil != null){
                    return if(r.returnUntil != rule) r else r.value.r
                }
            }

            println("Таким образом, для $rule имеем ${usedVariables.varState()}")
            if(rule.tryApplying(variables)){
                println("Соответственно, $rule признано истинным, " +
                        "и в соответствии с ним переменная $variable получает значение ${variables[variable]}")
                break
            }
            else{
                println("Соответственно, $rule не срабатывает ")
            }
        }

        if(!variables.containsKey(variable)){
            println("Т.к. ни одно правило связанное с переменной $variable не сработало, то ее значение неопределено")
        }
        println()
        return variables[variable].r
    }

    private val Any?.r: Returned
        get() = Returned(this)
    private fun Any?.returnUntil(rule: Rule) = Returned(this, rule)
    private class Returned(val value: Any?, val returnUntil : Rule? = null)

    private fun Collection<String>.varState() =
        this.map { "$it=${variables[it]?:"Неизвестно"}" }
            .joinToString(", ")

}