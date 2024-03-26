package expertus

import kotlin.reflect.KClass

/**
 * TODO Class Description
 *
 * @author Marat Gumerov
 * @since 07.03.2024
 */
interface RuleCondition {
    fun isSatisfied(variables: Map<String, Any>): Boolean

    fun allVariables(): Set<String>

    fun Then(vararg effect: RuleEffect): Rule{
        return Rule(this, effect.asList())
    }

    companion object{

        @JvmStatic
        fun If(vararg conditions: RuleCondition): RuleCondition {
            val condition = if (conditions.size == 1) conditions.single() else And(*conditions)
            return condition
        }

        @JvmStatic
        fun And(vararg conditions: RuleCondition) = AndCondition(conditions.asList())

        @JvmStatic
        fun Or(vararg conditions: RuleCondition) = OrCondition(conditions.asList())

        @JvmStatic
        fun Not(condition: RuleCondition) = NotCondition(condition)

        fun <E : Enum<E>> Exists(enum: KClass<E>) = ExistsCondition(enum.java.simpleName)
        fun Exists(variable: String) = ExistsCondition(variable)

        fun Eq(enumVal: Enum<*>) = Eq(enumVal.javaClass.simpleName, enumVal)
        fun Eq(variable: String, value: Any) = IsCondition(variable, IsCondition.Operator.EQ, value)

        fun Neq(variable: String, value: Any) = IsCondition(variable, IsCondition.Operator.NEQ, value)

        fun Gt(variable: String, value: Any) = IsCondition(variable, IsCondition.Operator.GT, value)

        fun Gte(variable: String, value: Any) = IsCondition(variable, IsCondition.Operator.GTE, value)

        fun Lt(variable: String, value: Any) = IsCondition(variable, IsCondition.Operator.LT, value)

        fun Lte(variable: String, value: Any) = IsCondition(variable, IsCondition.Operator.LTE, value)
    }
}

abstract class GroupCondition(val conditions: List<RuleCondition>) : RuleCondition{
    override fun allVariables(): Set<String> {
        return conditions.map { it.allVariables() }.flatten().toSet()
    }
}

class AndCondition(conditions: List<RuleCondition>) : GroupCondition(conditions){
    override fun isSatisfied(variables: Map<String, Any>): Boolean {
        return conditions.all { it.isSatisfied(variables) }
    }
}

class OrCondition(conditions: List<RuleCondition>) : GroupCondition(conditions){
    override fun isSatisfied(variables: Map<String, Any>): Boolean {
        return conditions.any { it.isSatisfied(variables) }
    }
}

class NotCondition(val condition: RuleCondition): RuleCondition{
    override fun isSatisfied(variables: Map<String, Any>): Boolean {
        return !condition.isSatisfied(variables)
    }

    override fun allVariables(): Set<String> {
        return condition.allVariables()
    }

}

open class ExistsCondition(val variable: String) : RuleCondition{
    override fun isSatisfied(variables: Map<String, Any>): Boolean {
        return variables.containsKey(variable)
    }

    override fun allVariables(): Set<String> {
        return setOf(variable)
    }

}

class IsCondition(variable: String, val operator: Operator, val value: Any) : ExistsCondition(variable){
    enum class Operator{
        EQ {
            override fun compare(variableValue: Any, expected: Any): Boolean {
                return variableValue.toString() == expected.toString()
            }
        },
        NEQ {
            override fun compare(variableValue: Any, expected: Any): Boolean {
                return variableValue.toString() != expected.toString()
            }
        },
        GT {
            override fun compare(variableValue: Any, expected: Any): Boolean {
                return (variableValue as Number).toDouble() > (expected as Number).toDouble()
            }
        },
        GTE {
            override fun compare(variableValue: Any, expected: Any): Boolean {
                return (variableValue as Number).toDouble() >= (expected as Number).toDouble()
            }
        },
        LT {
            override fun compare(variableValue: Any, expected: Any): Boolean {
                return (variableValue as Number).toDouble() < (expected as Number).toDouble()
            }
        },
        LTE {
            override fun compare(variableValue: Any, expected: Any): Boolean {
                return (variableValue as Number).toDouble() <= (expected as Number).toDouble()
            }
        },

        ;
        abstract fun compare(variableValue: Any, expected: Any): Boolean
    }

    override fun isSatisfied(variables: Map<String, Any>): Boolean {
        return super.isSatisfied(variables) && operator.compare(variables[variable]!!, value)
    }
}