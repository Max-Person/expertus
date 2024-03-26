package expertus

/**
 * TODO Class Description
 *
 * @author Marat Gumerov
 * @since 07.03.2024
 */
class Rule(val condition: RuleCondition, val effects: List<RuleEffect>) {
    var number = -1

    override fun toString() = "П.$number"

    fun tryApplying(variables: MutableMap<String, Any>): Boolean{
        if(condition.isSatisfied(variables)){
            effects.forEach{it.apply(variables) }
            return true
        }
        return false
    }
}