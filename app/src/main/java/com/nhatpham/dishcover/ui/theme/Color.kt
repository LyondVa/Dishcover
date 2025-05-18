package com.nhatpham.dishcover.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val PrimaryColor = Color(0xFFFF5A60)
val PrimaryDarkColor = Color(0xFFE14146)
val PrimaryLightColor = Color(0xFFFF8085)

// Secondary Colors
val SecondaryColor = Color(0xFF4CAF50)
val SecondaryDarkColor = Color(0xFF388E3C)
val SecondaryLightColor = Color(0xFF81C784)

// Accent Colors
val AccentYellow = Color(0xFFFFC107)
val AccentOrange = Color(0xFFFF9800)
val AccentTeal = Color(0xFF009688)
val AccentBlue = Color(0xFF2196F3)
val AccentPurple = Color(0xFF9C27B0)

// Background Colors
val BackgroundColor = Color(0xFFFAFAFA)
val SurfaceColor = Color(0xFFFFFFFF)
val CardBackgroundColor = Color(0xFFFFFFFF)

// Text Colors
val TextPrimaryColor = Color(0xFF212121)
val TextSecondaryColor = Color(0xFF757575)
val TextHintColor = Color(0xFF9E9E9E)

// Semantic Colors
val SuccessColor = Color(0xFF4CAF50)
val InfoColor = Color(0xFF2196F3)
val WarningColor = Color(0xFFFFC107)
val ErrorColor = Color(0xFFF44336)

// Food Category Colors
val BreakfastCategoryColor = Color(0xFFFFB74D)
val LunchCategoryColor = Color(0xFF4DB6AC)
val DinnerCategoryColor = Color(0xFF7986CB)
val DessertCategoryColor = Color(0xFFF06292)
val SnackCategoryColor = Color(0xFFAED581)
val DrinkCategoryColor = Color(0xFF4FC3F7)

// Recipe Difficulty Colors
val EasyDifficultyColor = Color(0xFF81C784)
val MediumDifficultyColor = Color(0xFFFFD54F)
val HardDifficultyColor = Color(0xFFFF8A65)

// Gradients for Featured Recipe
val FeaturedRecipeGradientStart = Color(0x00000000)
val FeaturedRecipeGradientEnd = Color(0xCC000000)

// Ingredient Type Colors
val ProteinColor = Color(0xFFE57373)
val VegetablesColor = Color(0xFF81C784)
val FruitsColor = Color(0xFFFFB74D)
val DairyColor = Color(0xFFFFEE58)
val GrainColor = Color(0xFFA1887F)
val SpicesColor = Color(0xFFFF8A65)

// Category-specific colors for circle backgrounds
val TartColor = Color(0xFFEC407A)
val PancakeColor = Color(0xFFFFB74D)
val PastaColor = Color(0xFFFFF176)
val CookieColor = Color(0xFFBA68C8)
val NoneColor = Color(0xFFBDBDBD)

// Function to get color based on recipe name/type
fun getRecipeColor(name: String): Color {
    return when {
        name.contains("tart", ignoreCase = true) -> TartColor
        name.contains("pancake", ignoreCase = true) -> PancakeColor
        name.contains("pasta", ignoreCase = true) -> PastaColor
        name.contains("cookie", ignoreCase = true) -> CookieColor
        name.contains("breakfast", ignoreCase = true) -> BreakfastCategoryColor
        name.contains("lunch", ignoreCase = true) -> LunchCategoryColor
        name.contains("dinner", ignoreCase = true) -> DinnerCategoryColor
        name.contains("dessert", ignoreCase = true) -> DessertCategoryColor
        name.contains("snack", ignoreCase = true) -> SnackCategoryColor
        name.contains("drink", ignoreCase = true) -> DrinkCategoryColor
        else -> NoneColor
    }
}

// Function to get color based on category
fun getCategoryColor(category: String?): Color {
    return when (category?.lowercase()) {
        "breakfast" -> BreakfastCategoryColor
        "lunch" -> LunchCategoryColor
        "dinner" -> DinnerCategoryColor
        "dessert" -> DessertCategoryColor
        "snack" -> SnackCategoryColor
        "drink" -> DrinkCategoryColor
        else -> PrimaryColor
    }
}