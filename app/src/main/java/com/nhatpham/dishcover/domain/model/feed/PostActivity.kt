
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class PostActivity(
    val activityId: String = "",
    val postId: String = "",
    val userId: String = "",
    val activityType: PostActivityType = PostActivityType.VIEW,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Timestamp = Timestamp.now()
)

enum class PostActivityType {
    VIEW,
    LIKE,
    UNLIKE,
    COMMENT,
    SHARE,
    CLICK_RECIPE_REFERENCE,
    CLICK_COOKBOOK_REFERENCE,
    SAVE,
    REPORT
}
