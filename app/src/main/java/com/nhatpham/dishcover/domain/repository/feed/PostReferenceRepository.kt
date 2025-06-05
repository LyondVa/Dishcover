package com.nhatpham.dishcover.domain.repository.feed

import com.nhatpham.dishcover.domain.model.feed.PostCookbookReference
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface PostReferenceRepository {
    // Recipe Reference Operations
    fun addRecipeReference(reference: PostRecipeReference): Flow<Resource<PostRecipeReference>>
    fun removeRecipeReference(referenceId: String): Flow<Resource<Boolean>>
    fun getPostRecipeReferences(postId: String): Flow<Resource<List<PostRecipeReference>>>

    // Cookbook Reference Operations
    fun addCookbookReference(reference: PostCookbookReference): Flow<Resource<PostCookbookReference>>
    fun removeCookbookReference(referenceId: String): Flow<Resource<Boolean>>
    fun getPostCookbookReferences(postId: String): Flow<Resource<List<PostCookbookReference>>>
}