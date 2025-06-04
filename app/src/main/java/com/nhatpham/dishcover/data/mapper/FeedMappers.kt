package com.nhatpham.dishcover.data.mapper

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.model.dto.feed.CommentDto
import com.nhatpham.dishcover.data.model.dto.feed.CommentLikeDto
import com.nhatpham.dishcover.data.model.dto.feed.PostActivityDto
import com.nhatpham.dishcover.data.model.dto.feed.PostCookbookReferenceDto
import com.nhatpham.dishcover.data.model.dto.feed.PostDto
import com.nhatpham.dishcover.data.model.dto.feed.PostLikeDto
import com.nhatpham.dishcover.data.model.dto.feed.PostRecipeReferenceDto
import com.nhatpham.dishcover.data.model.dto.feed.PostShareDto
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.model.user.User

// Post mapping
fun PostDto.toDomain(): Post {
    return Post(
        postId = this.postId ?: "",
        userId = this.userId ?: "",
        content = this.content ?: "",
        imageUrls = this.imageUrls ?: emptyList(),
        videoUrl = this.videoUrl,
        postType = when (this.postType) {
            "TEXT" -> PostType.TEXT
            "IMAGE" -> PostType.IMAGE
            "VIDEO" -> PostType.VIDEO
            "RECIPE_SHARE" -> PostType.RECIPE_SHARE
            "COOKBOOK_SHARE" -> PostType.COOKBOOK_SHARE
            "COOKING_PROGRESS" -> PostType.COOKING_PROGRESS
            "REVIEW" -> PostType.REVIEW
            else -> PostType.TEXT
        },
        recipeReferences = emptyList(), // Will be populated separately
        cookbookReferences = emptyList(), // Will be populated separately
        taggedUsers = this.taggedUsers ?: emptyList(),
        hashtags = this.hashtags ?: emptyList(),
        location = this.location,
        isPublic = this.public != false, // Map from 'public' to 'isPublic'
        allowComments = this.allowComments != false,
        allowShares = this.allowShares != false,
        likeCount = this.likeCount ?: 0,
        commentCount = this.commentCount ?: 0,
        shareCount = this.shareCount ?: 0,
        viewCount = this.viewCount ?: 0,
        createdAt = this.createdAt ?: Timestamp.now(),
        updatedAt = this.updatedAt ?: Timestamp.now(),
        isEdited = this.edited == true, // Map from 'edited' to 'isEdited'
        isPinned = this.pinned == true, // Map from 'pinned' to 'isPinned'
        isArchived = this.archived == true // Map from 'archived' to 'isArchived'
    )
}

fun Post.toDto(): PostDto {
    return PostDto(
        postId = this.postId,
        userId = this.userId,
        content = this.content,
        imageUrls = this.imageUrls,
        videoUrl = this.videoUrl,
        postType = this.postType.name,
        taggedUsers = this.taggedUsers,
        hashtags = this.hashtags,
        location = this.location,
        public = this.isPublic, // Map from 'isPublic' to 'public'
        allowComments = this.allowComments,
        allowShares = this.allowShares,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        shareCount = this.shareCount,
        viewCount = this.viewCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        edited = this.isEdited, // Map from 'isEdited' to 'edited'
        pinned = this.isPinned, // Map from 'isPinned' to 'pinned'
        archived = this.isArchived // Map from 'isArchived' to 'archived'
    )
}

// Post to PostListItem mapping
fun Post.toListItem(
    username: String = "",
    userProfilePicture: String? = null,
    isLikedByCurrentUser: Boolean = false,
    isSharedByCurrentUser: Boolean = false,
    isFollowingAuthor: Boolean = false
): PostListItem {
    return PostListItem(
        postId = this.postId,
        userId = this.userId,
        username = username,
        userProfilePicture = userProfilePicture,
        content = this.content,
        firstImageUrl = this.imageUrls.firstOrNull(),
        postType = this.postType,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        shareCount = this.shareCount,
        isLikedByCurrentUser = isLikedByCurrentUser,
        isSharedByCurrentUser = isSharedByCurrentUser,
        hasRecipeReferences = this.recipeReferences.isNotEmpty(),
        hasCookbookReferences = this.cookbookReferences.isNotEmpty(),
        createdAt = this.createdAt,
        isFollowingAuthor = isFollowingAuthor
    )
}

fun PostDto.toListItem(
    username: String = "",
    userProfilePicture: String? = null,
    isLikedByCurrentUser: Boolean = false,
    isSharedByCurrentUser: Boolean = false,
    isFollowingAuthor: Boolean = false,
    hasRecipeReferences: Boolean = false,
    hasCookbookReferences: Boolean = false
): PostListItem {
    return PostListItem(
        postId = this.postId ?: "",
        userId = this.userId ?: "",
        username = username,
        userProfilePicture = userProfilePicture,
        content = this.content ?: "",
        firstImageUrl = this.imageUrls?.firstOrNull(),
        postType = when (this.postType) {
            "TEXT" -> PostType.TEXT
            "IMAGE" -> PostType.IMAGE
            "VIDEO" -> PostType.VIDEO
            "RECIPE_SHARE" -> PostType.RECIPE_SHARE
            "COOKBOOK_SHARE" -> PostType.COOKBOOK_SHARE
            "COOKING_PROGRESS" -> PostType.COOKING_PROGRESS
            "REVIEW" -> PostType.REVIEW
            else -> PostType.TEXT
        },
        likeCount = this.likeCount ?: 0,
        commentCount = this.commentCount ?: 0,
        shareCount = this.shareCount ?: 0,
        isLikedByCurrentUser = isLikedByCurrentUser,
        isSharedByCurrentUser = isSharedByCurrentUser,
        hasRecipeReferences = hasRecipeReferences,
        hasCookbookReferences = hasCookbookReferences,
        createdAt = this.createdAt ?: Timestamp.now(),
        isFollowingAuthor = isFollowingAuthor
    )
}

// Comment mapping
fun CommentDto.toDomain(): Comment {
    return Comment(
        commentId = this.commentId ?: "",
        postId = this.postId ?: "",
        userId = this.userId ?: "",
        parentCommentId = this.parentCommentId,
        content = this.content ?: "",
        imageUrl = this.imageUrl,
        taggedUsers = this.taggedUsers ?: emptyList(),
        likeCount = this.likeCount ?: 0,
        replyCount = this.replyCount ?: 0,
        createdAt = this.createdAt ?: Timestamp.now(),
        updatedAt = this.updatedAt ?: Timestamp.now(),
        isEdited = this.edited == true, // Map from 'edited' to 'isEdited'
        isDeleted = this.deleted == true // Map from 'deleted' to 'isDeleted'
    )
}

fun Comment.toDto(): CommentDto {
    return CommentDto(
        commentId = this.commentId,
        postId = this.postId,
        userId = this.userId,
        parentCommentId = this.parentCommentId,
        content = this.content,
        imageUrl = this.imageUrl,
        taggedUsers = this.taggedUsers,
        likeCount = this.likeCount,
        replyCount = this.replyCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        edited = this.isEdited, // Map from 'isEdited' to 'edited'
        deleted = this.isDeleted // Map from 'isDeleted' to 'deleted'
    )
}

// PostLike mapping
fun PostLikeDto.toDomain(): PostLike {
    return PostLike(
        likeId = this.likeId ?: "",
        postId = this.postId ?: "",
        userId = this.userId ?: "",
        likeType = when (this.likeType) {
            "LIKE" -> LikeType.LIKE
            "LOVE" -> LikeType.LOVE
            "LAUGH" -> LikeType.LAUGH
            "WOW" -> LikeType.WOW
            "SAD" -> LikeType.SAD
            "ANGRY" -> LikeType.ANGRY
            else -> LikeType.LIKE
        },
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun PostLike.toDto(): PostLikeDto {
    return PostLikeDto(
        likeId = this.likeId,
        postId = this.postId,
        userId = this.userId,
        likeType = this.likeType.name,
        createdAt = this.createdAt
    )
}

// CommentLike mapping
fun CommentLikeDto.toDomain(): CommentLike {
    return CommentLike(
        likeId = this.likeId ?: "",
        commentId = this.commentId ?: "",
        userId = this.userId ?: "",
        likeType = when (this.likeType) {
            "LIKE" -> LikeType.LIKE
            "LOVE" -> LikeType.LOVE
            "LAUGH" -> LikeType.LAUGH
            "WOW" -> LikeType.WOW
            "SAD" -> LikeType.SAD
            "ANGRY" -> LikeType.ANGRY
            else -> LikeType.LIKE
        },
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun CommentLike.toDto(): CommentLikeDto {
    return CommentLikeDto(
        likeId = this.likeId,
        commentId = this.commentId,
        userId = this.userId,
        likeType = this.likeType.name,
        createdAt = this.createdAt
    )
}

// PostShare mapping
fun PostShareDto.toDomain(): PostShare {
    return PostShare(
        shareId = this.shareId ?: "",
        originalPostId = this.originalPostId ?: "",
        sharedByUserId = this.sharedByUserId ?: "",
        shareMessage = this.shareMessage,
        shareType = when (this.shareType) {
            "REPOST" -> ShareType.REPOST
            "STORY" -> ShareType.STORY
            "DIRECT_MESSAGE" -> ShareType.DIRECT_MESSAGE
            "EXTERNAL" -> ShareType.EXTERNAL
            else -> ShareType.REPOST
        },
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun PostShare.toDto(): PostShareDto {
    return PostShareDto(
        shareId = this.shareId,
        originalPostId = this.originalPostId,
        sharedByUserId = this.sharedByUserId,
        shareMessage = this.shareMessage,
        shareType = this.shareType.name,
        createdAt = this.createdAt
    )
}

// PostActivity mapping
fun PostActivityDto.toDomain(): PostActivity {
    return PostActivity(
        activityId = this.activityId ?: "",
        postId = this.postId ?: "",
        userId = this.userId ?: "",
        activityType = when (this.activityType) {
            "VIEW" -> PostActivityType.VIEW
            "LIKE" -> PostActivityType.LIKE
            "UNLIKE" -> PostActivityType.UNLIKE
            "COMMENT" -> PostActivityType.COMMENT
            "SHARE" -> PostActivityType.SHARE
            "CLICK_RECIPE_REFERENCE" -> PostActivityType.CLICK_RECIPE_REFERENCE
            "CLICK_COOKBOOK_REFERENCE" -> PostActivityType.CLICK_COOKBOOK_REFERENCE
            "SAVE" -> PostActivityType.SAVE
            "REPORT" -> PostActivityType.REPORT
            else -> PostActivityType.VIEW
        },
        metadata = this.metadata ?: emptyMap(),
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun PostActivity.toDto(): PostActivityDto {
    return PostActivityDto(
        activityId = this.activityId,
        postId = this.postId,
        userId = this.userId,
        activityType = this.activityType.name,
        metadata = this.metadata,
        createdAt = this.createdAt
    )
}

// PostRecipeReference mapping
fun PostRecipeReferenceDto.toDomain(): PostRecipeReference {
    return PostRecipeReference(
        referenceId = this.referenceId ?: "",
        postId = this.postId ?: "",
        recipeId = this.recipeId ?: "",
        displayText = this.displayText ?: "",
        position = this.position ?: 0,
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun PostRecipeReference.toDto(): PostRecipeReferenceDto {
    return PostRecipeReferenceDto(
        referenceId = this.referenceId,
        postId = this.postId,
        recipeId = this.recipeId,
        displayText = this.displayText,
        position = this.position,
        createdAt = this.createdAt
    )
}

// PostCookbookReference mapping
fun PostCookbookReferenceDto.toDomain(): PostCookbookReference {
    return PostCookbookReference(
        referenceId = this.referenceId ?: "",
        postId = this.postId ?: "",
        cookbookId = this.cookbookId ?: "",
        displayText = this.displayText ?: "",
        position = this.position ?: 0,
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun PostCookbookReference.toDto(): PostCookbookReferenceDto {
    return PostCookbookReferenceDto(
        referenceId = this.referenceId,
        postId = this.postId,
        cookbookId = this.cookbookId,
        displayText = this.displayText,
        position = this.position,
        createdAt = this.createdAt
    )
}

// FeedItem mapping - Note: FeedItem doesn't have a DTO since it's assembled from other entities
fun buildFeedItem(
    post: Post,
    author: User? = null,
    sharedPost: PostShare? = null,
    originalPost: Post? = null,
    sharedBy: User? = null,
    isLikedByCurrentUser: Boolean = false,
    isSharedByCurrentUser: Boolean = false,
    isFollowingAuthor: Boolean = false
): FeedItem {
    val itemType = when {
        sharedPost != null -> FeedItemType.SHARED_POST
        else -> FeedItemType.POST
    }

    return FeedItem(
        feedItemId = "", // Generated at runtime
        itemType = itemType,
        post = post,
        sharedPost = sharedPost,
        originalPost = originalPost,
        author = author,
        sharedBy = sharedBy,
        isLikedByCurrentUser = isLikedByCurrentUser,
        isSharedByCurrentUser = isSharedByCurrentUser,
        isFollowingAuthor = isFollowingAuthor,
        displayPriority = 0,
        createdAt = sharedPost?.createdAt ?: post.createdAt
    )
}