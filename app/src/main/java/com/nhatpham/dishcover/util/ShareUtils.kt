package com.nhatpham.dishcover.util

object ShareUtils {

    private const val SHARE_BASE_URL = "https://dishcover.app"
    private const val APP_SCHEME = "dishcover"

    fun generateWebShareLink(recipeId: String): String {
        return "$SHARE_BASE_URL/recipe/$recipeId"
    }

    fun generateAppShareLink(recipeId: String): String {
        return "$APP_SCHEME://recipe/$recipeId"
    }

    fun extractRecipeIdFromUrl(url: String): String? {
        return try {
            val parts = url.split("/")
            val recipeIndex = parts.indexOf("recipe")
            if (recipeIndex != -1 && recipeIndex + 1 < parts.size) {
                parts[recipeIndex + 1]
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun buildShareText(
        title: String,
        description: String?,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        difficulty: String,
        shareLink: String
    ): String {
        return buildString {
            appendLine("🍽️ $title")
            if (!description.isNullOrBlank()) {
                appendLine()
                appendLine(description)
            }
            appendLine()
            appendLine("⏱️ Prep: ${prepTime} mins | Cook: ${cookTime} mins")
            appendLine("👥 Serves: $servings")
            appendLine("📊 Difficulty: $difficulty")
            appendLine()
            appendLine("View the full recipe here:")
            appendLine(shareLink)
            appendLine()
            appendLine("Shared via Dishcover 📱")
        }
    }
}