package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.domain.model.feed.PostActivity
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostAnalyticsRepository {
    fun trackPostActivity(activity: PostActivity): Flow<Resource<Boolean>>
    fun getPostAnalytics(postId: String): Flow<Resource<Map<String, Any>>>
    fun getUserPostAnalytics(userId: String, dateRange: String): Flow<Resource<Map<String, Any>>>
}