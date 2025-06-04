
package com.nhatpham.dishcover.domain.model.feed

import com.google.firebase.Timestamp

data class PostCookbookReference(
    val referenceId: String = "",
    val postId: String = "",
    val cookbookId: String = "",
    val displayText: String = "",
    val position: Int = 0, // Position in the post content
    val createdAt: Timestamp = Timestamp.now()
)
