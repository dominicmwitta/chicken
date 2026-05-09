package com.example.poultryfarmmanager.ui.screens.dashboard

data class FarmAnalytics(
    val selectedDays: Int = 30,

    // Revenue
    val eggRevenue: Double = 0.0,
    val birdRevenue: Double = 0.0,

    // Egg production
    val totalEggs: Int = 0,
    val eggTrend: List<EggDataPoint> = emptyList(),

    // Bird inventory
    val birdBreakdown: List<BreedCount> = emptyList(),
    val totalBirdsSold: Int = 0,
    val totalBirdsSlaughtered: Int = 0,

    // Feed
    val totalFeedCost: Double = 0.0,
    val feedTypeCount: Int = 0,
    val lowStockCount: Int = 0
) {
    val totalRevenue: Double get() = eggRevenue + birdRevenue
}

data class EggDataPoint(val label: String, val count: Int)
data class BreedCount(val breed: String, val remaining: Int)
