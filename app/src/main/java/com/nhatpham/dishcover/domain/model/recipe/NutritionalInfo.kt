// NutritionalInfo.kt
package com.nhatpham.dishcover.domain.model.recipe

import com.google.firebase.Timestamp

data class NutritionalInfo(
    val recipeId: String = "",
    val calories: Int = 0,
    val protein: Double = 0.0, // grams
    val carbohydrates: Double = 0.0, // grams
    val fat: Double = 0.0, // grams
    val fiber: Double = 0.0, // grams
    val sugar: Double = 0.0, // grams
    val sodium: Double = 0.0, // milligrams
    val cholesterol: Double = 0.0, // milligrams
    val iron: Double = 0.0, // milligrams
    val calcium: Double = 0.0, // milligrams
    val vitaminC: Double = 0.0, // milligrams
    val perServing: Boolean = true,
    val servingSize: String = "",
    val isEstimated: Boolean = true,
    val lastCalculated: Timestamp = Timestamp.now()
)