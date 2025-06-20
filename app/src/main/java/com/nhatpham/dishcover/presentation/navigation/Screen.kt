package com.nhatpham.dishcover.presentation.navigation

sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    // Main screens
    object Home : Screen("home_screen")
    object Search : Screen("search_screen")
    object Notifications : Screen("notifications_screen")

    // Recipe screens
    object Recipes : Screen("recipes_screen")
    object RecipeDetail : Screen("recipe_detail_screen")
    object CreateRecipe : Screen("create_recipe_screen")
    object EditRecipe : Screen("edit_recipe_screen")
    object Category : Screen("category_screen")
    object SharedRecipe : Screen("shared_recipe_screen")
    object Favorites : Screen("favorites_screen")
    object RecentlyViewed : Screen("recently_viewed_screen")

    // Profile screens
    object Profile : Screen("profile_screen")
    object EditProfile : Screen("edit_profile_screen")
    object Settings : Screen("settings_screen")
    object PrivacySettings : Screen("privacy_settings_screen")
    object NotificationSettings : Screen("notification_settings_screen")
    object AccountSettings : Screen("account_settings_screen")
    object Followers : Screen("followers_screen")
    object Following : Screen("following_screen")

    // Feed screens
    object Feed : Screen("feed_screen")
    object PostDetail : Screen("post_detail_screen")
    object CreatePost : Screen("create_post_screen")
    object EditPost : Screen("edit_post_screen")

    // Cookbook screens
    object CreateCookbook : Screen("create_cookbook_screen")
    object CookbookDetail : Screen("cookbook_detail_screen/{cookbookId}") {
        fun createRoute(cookbookId: String) = "cookbook_detail/$cookbookId"
    }
    object EditCookbook : Screen("edit_cookbook_screen") {
        fun createRoute(cookbookId: String) = "edit_cookbook_screen/$cookbookId"
    }

    object AddRecipesToCookbook : Screen("add_recipes_to_cookbook/{cookbookId}") {
        fun createRoute(cookbookId: String) = "add_recipes_to_cookbook/$cookbookId"
    }

    object Chatbot : Screen("chatbot_screen")

    object Admin : Screen("admin_screen")
}