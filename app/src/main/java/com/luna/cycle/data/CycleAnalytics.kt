package com.luna.cycle.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.sqrt

data class CycleHistory(
    val periodStarts: List<LocalDate>,
    val cycleLengths: List<Int>,
    val averageCycleLength: Int?,
    val averagePeriodLength: Int?,
    val regularityScore: Int?,
    val irregularCycleLengths: List<Int>
)

data class CycleCorrelation(
    val symptom: String,
    val factor: String,
    val observedDays: Int,
    val comparisonDays: Int,
    val differencePercent: Int,
    val affectedGroupRate: Int,
    val comparisonGroupRate: Int
)

fun analyzeCycleHistory(
    logs: List<CycleLog>,
    latestPeriodStart: LocalDate,
    configuredLength: Int,
    averagingCycles: Int
): CycleHistory {
    val flowDates = logs.asSequence().filter { it.flow.isNotBlank() }.map { LocalDate.parse(it.date) }.toSortedSet()
    val starts = periodStarts(logs, latestPeriodStart)
    val allLengths = starts.zipWithNext { start, next ->
        ChronoUnit.DAYS.between(start, next).toInt()
    }.filter { it in 15..60 }
    val selectedLengths = if (configuredLength == 0) allLengths.takeLast(averagingCycles) else emptyList()
    val average = if (configuredLength > 0) configuredLength else selectedLengths.takeIf { it.isNotEmpty() }?.average()?.toInt()
    val variations = allLengths.map { it.toDouble() }
    val deviation = if (variations.size >= 2) {
        val mean = variations.average()
        sqrt(variations.sumOf { (it - mean) * (it - mean) } / variations.size)
    } else null
    val periodLengths = mutableListOf<Int>()
    var current = 0
    var previous: LocalDate? = null
    flowDates.forEach { date ->
        if (previous != null && ChronoUnit.DAYS.between(previous, date) != 1L) {
            periodLengths += current
            current = 0
        }
        current++
        previous = date
    }
    if (current > 0) periodLengths += current
    return CycleHistory(
        periodStarts = starts,
        cycleLengths = allLengths,
        averageCycleLength = average,
        averagePeriodLength = periodLengths.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        regularityScore = deviation?.let { (100 - (it * 10).toInt()).coerceIn(0, 100) },
        irregularCycleLengths = allLengths.filter { it !in 21..35 }
    )
}

fun periodStarts(logs: List<CycleLog>, latestPeriodStart: LocalDate? = null): List<LocalDate> {
    val flowDates = logs.asSequence().filter { it.flow.isNotBlank() }.map { LocalDate.parse(it.date) }.toSortedSet()
    val loggedStarts = flowDates.filter { it.minusDays(1) !in flowDates }
    return (loggedStarts + listOfNotNull(latestPeriodStart)).distinct().sorted()
}

fun wellbeingCorrelations(logs: List<CycleLog>): List<CycleCorrelation> {
    val correlations = mutableListOf<CycleCorrelation>()
    listOf(
        Factor("Lower water intake", CycleLog::waterGlasses, 0),
        Factor("Lower sleep quality", CycleLog::sleepQuality, 0)
    ).forEach { factor ->
        val days = logs.filter { factor.value(it) > factor.minimum }
        if (days.size < 8) return@forEach
        val median = days.map(factor.value).sorted()[days.size / 2]
        val lower = days.filter { factor.value(it) < median }
        val higher = days.filter { factor.value(it) >= median }
        if (lower.size < 3 || higher.size < 3) return@forEach
        days.flatMap { it.symptomSet() }.distinct().forEach symptomLoop@ { symptom ->
            val lowerRate = lower.count { symptom in it.symptomSet() }.toDouble() / lower.size
            val higherRate = higher.count { symptom in it.symptomSet() }.toDouble() / higher.size
            if (lowerRate <= higherRate || lowerRate < 0.25) return@symptomLoop
            correlations += CycleCorrelation(
                symptom = symptom,
                factor = factor.label,
                observedDays = lower.size,
                comparisonDays = higher.size,
                differencePercent = (((lowerRate - higherRate) / lowerRate) * 100).toInt(),
                affectedGroupRate = (lowerRate * 100).toInt(),
                comparisonGroupRate = (higherRate * 100).toInt()
            )
        }
    }
    return correlations.sortedByDescending { it.differencePercent }.take(5)
}

private data class Factor(
    val label: String,
    val value: (CycleLog) -> Int,
    val minimum: Int
)

fun symptomIntensity(log: CycleLog): Int =
    (log.symptomSet().size + when (log.flow) {
        "Heavy" -> 3
        "Medium" -> 2
        "Light", "Spotting" -> 1
        else -> 0
    }).coerceAtMost(10)
