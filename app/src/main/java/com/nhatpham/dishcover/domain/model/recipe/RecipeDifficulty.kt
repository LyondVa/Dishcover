// RecipeDifficulty.kt
package com.nhatpham.dishcover.domain.model.recipe

enum class RecipeDifficulty(
    val displayName: String,
    val level: Int,
    val description: String,
    val estimatedTime: String,
    val skillRequired: String
) {
    BEGINNER(
        displayName = "Beginner",
        level = 1,
        description = "Perfect for first-time cooks",
        estimatedTime = "15-30 min",
        skillRequired = "Basic kitchen skills"
    ),
    EASY(
        displayName = "Easy",
        level = 2,
        description = "Simple techniques, minimal prep",
        estimatedTime = "30-45 min",
        skillRequired = "Some cooking experience"
    ),
    INTERMEDIATE(
        displayName = "Intermediate",
        level = 3,
        description = "Multiple steps, some technique required",
        estimatedTime = "45-90 min",
        skillRequired = "Comfortable in kitchen"
    ),
    ADVANCED(
        displayName = "Advanced",
        level = 4,
        description = "Complex techniques and timing",
        estimatedTime = "90+ min",
        skillRequired = "Experienced cook"
    ),
    EXPERT(
        displayName = "Expert",
        level = 5,
        description = "Professional techniques, precision required",
        estimatedTime = "2+ hours",
        skillRequired = "Advanced culinary skills"
    );

    companion object {
        fun fromString(difficulty: String): RecipeDifficulty {
            return RecipeDifficulty.entries.find {
                it.displayName.equals(difficulty, ignoreCase = true)
            } ?: EASY
        }

        fun fromLevel(level: Int): RecipeDifficulty {
            return RecipeDifficulty.entries.find { it.level == level } ?: EASY
        }
    }
}