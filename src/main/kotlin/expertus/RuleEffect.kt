package expertus

import kotlin.IllegalStateException

/**
 * TODO Class Description
 *
 * @author Marat Gumerov
 * @since 07.03.2024
 */
interface RuleEffect {
    fun apply(variables: MutableMap<String, Any>)

    val variable: String

    companion object{
        @JvmStatic
        fun Set(variable: String, value: Any) = SetEffect(variable, value)

        @JvmStatic
        fun Set(enumValue: Enum<*>) = Set(enumValue.javaClass.simpleName, enumValue)
    }
}

class ResoningConflictException : IllegalStateException()

class SetEffect(override val variable: String, val value: Any): RuleEffect{
    override fun apply(variables: MutableMap<String, Any>) {
        if(variables.containsKey(variable)){
            throw ResoningConflictException()
        }
        variables[variable] = value
    }
}