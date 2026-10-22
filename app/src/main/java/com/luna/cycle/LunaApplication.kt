package com.luna.cycle

import android.app.Application
import com.luna.cycle.data.CycleDatabase
import com.luna.cycle.data.CycleRepository

class LunaApplication : Application() {
    val repository: CycleRepository by lazy {
        CycleRepository(CycleDatabase.getInstance(this))
    }
}
