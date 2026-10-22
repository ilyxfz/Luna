package com.luna.cycle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_logs")
data class CycleLog(
    @PrimaryKey val date: String,
    val flow: String = "",
    val symptoms: String = "",
    val mood: String = "",
    val note: String = "",
    val waterGlasses: Int = 0,
    val sleepQuality: Int = 0,
    val activity: String = "",
    val sexualActivity: String = "",
    val protection: String = "",
    val cervicalMucus: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun symptomSet(): Set<String> = symptoms.split("|").filter(String::isNotBlank).toSet()
}
