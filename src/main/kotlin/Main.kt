import expertus.Engine
import expertus.RuleCondition.Companion.And
import expertus.RuleCondition.Companion.Eq
import expertus.RuleCondition.Companion.Exists
import expertus.RuleCondition.Companion.If
import expertus.RuleCondition.Companion.Not
import expertus.RuleCondition.Companion.Or
import expertus.RuleEffect.Companion.Set

enum class VillageHappiness{ sad, fine, happy,}

enum class VillageFoodSupply{ none, some, a_lot, }
enum class VillageHasFarmers{ yes, no }

enum class VillageWeaponStash{ none, some, a_lot, }
enum class VillageTower{ none, present }
enum class VillageRaid{ none, incoming }

enum class VillageHunger{ hungry, sated, fed, }
enum class VillageRaidOutcome{ none, pillaged, destroyed }
enum class VillageEnding{ destroyed, pillaged, starved, living, thriving }


fun main() {
    val engine = Engine()

    val canEnd = And(
        Exists(VillageHappiness::class),
        Exists(VillageHunger::class),
        Exists(VillageRaidOutcome::class),
    )
    engine.addRules(

        //---1
        If(
            Eq(VillageHasFarmers.yes),
            Eq(VillageFoodSupply.a_lot),
        )
        .Then(Set(VillageHunger.fed)),

        //---2
        If(
            Or(
                And(
                    Eq(VillageHasFarmers.yes),
                    Eq(VillageFoodSupply.some),
                ),
                And(
                    Eq(VillageHasFarmers.no),
                    Eq(VillageFoodSupply.a_lot),
                )
            )
        )
        .Then(Set(VillageHunger.sated)),

        //---3
        If(
            Or(
                Eq(VillageHasFarmers.no),
                Eq(VillageFoodSupply.none),
            )
        )
        .Then(Set(VillageHunger.hungry)),

        //---4
        If(
            Eq(VillageTower.present),
            Not(Exists(VillageWeaponStash::class))
        )
            .Then(Set(VillageWeaponStash.some)),

        //---5
        If(
            Eq(VillageRaid.none),
        )
            .Then(Set(VillageRaidOutcome.none)),

        //---6
        If(
            Eq(VillageRaid.incoming),
            Eq(VillageTower.present),
            Not(Eq(VillageWeaponStash.none)),
        )
            .Then(Set(VillageRaidOutcome.none)),

        //---7
        If(
            Eq(VillageRaid.incoming),
            Eq(VillageWeaponStash.a_lot),
        )
            .Then(Set(VillageRaidOutcome.none)),

        //---8
        If(
            Eq(VillageRaid.incoming),
            Not(Eq(VillageTower.present)),
            Eq(VillageWeaponStash.some),
        )
            .Then(Set(VillageRaidOutcome.pillaged)),

        //---9
        If(
            Eq(VillageRaid.incoming),
            Eq(VillageWeaponStash.none),
        )
            .Then(Set(VillageRaidOutcome.destroyed)),

        //---10
        If(
            canEnd,
            Eq(VillageRaidOutcome.destroyed),
        )
            .Then(Set(VillageEnding.destroyed)),

        //---11
        If(
            canEnd,
            Eq(VillageRaidOutcome.pillaged),
        )
            .Then(Set(VillageEnding.pillaged)),

        //---12
        If(
            canEnd,
            Eq(VillageHunger.hungry),
            Not(Eq(VillageHappiness.happy)),
        )
            .Then(Set(VillageEnding.starved)),

        //---13
        If(
            canEnd,
            Not(Eq(VillageHunger.hungry)),
            Eq(VillageHappiness.happy),
        )
            .Then(Set(VillageEnding.thriving)),

        //---14
        If(
            canEnd,
            Or(
                Not(Eq(VillageHunger.hungry)),
                Eq(VillageHappiness.happy),
            )
        )
            .Then(Set(VillageEnding.living)),


        //---Defaults
        If(Not(Exists(VillageHappiness::class))).Then(Set(VillageHappiness.fine)),
        If(Not(Exists(VillageFoodSupply::class))).Then(Set(VillageFoodSupply.some)),
        If(Not(Exists(VillageHasFarmers::class))).Then(Set(VillageHasFarmers.yes)),
        If(Not(Exists(VillageWeaponStash::class))).Then(Set(VillageWeaponStash.none)),
        If(Not(Exists(VillageTower::class))).Then(Set(VillageTower.none)),
        If(Not(Exists(VillageRaid::class))).Then(Set(VillageRaid.none)),

    )

    engine.init(
        Set(VillageTower.present),
        Set(VillageRaid.incoming),
        Set(VillageHasFarmers.no),
    )
    engine.tryDetermineValue(VillageEnding::class)
    println(engine.variables)
}