// ServingAdjustment.kt
package com.nhatpham.dishcover.domain.model.recipe

data class ServingAdjustment(
    val originalServings: Int,
    val targetServings: Int,
    val scaleFactor: Double = targetServings.toDouble() / originalServings.toDouble()
) {
    fun scaleQuantity(originalQuantity: String): String {
        return try {
            val quantity = originalQuantity.toDoubleOrNull() ?: return originalQuantity
            val scaled = quantity * scaleFactor

            // Format nicely - use fractions for common values
            when (scaled) {
                scaled.toInt().toDouble() -> scaled.toInt().toString()
                0.25 -> "1/4"
                0.33 -> "1/3"
                0.5 -> "1/2"
                0.67 -> "2/3"
                0.75 -> "3/4"
                1.25 -> "1 1/4"
                1.33 -> "1 1/3"
                1.5 -> "1 1/2"
                1.67 -> "1 2/3"
                1.75 -> "1 3/4"
                else -> String.format("%.2f", scaled).trimEnd('0').trimEnd('.')
            }
        } catch (_: Exception) {
            originalQuantity
        }
    }
}