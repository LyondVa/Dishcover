
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import com.nhatpham.dishcover.domain.repository.FeedRepository
import javax.inject.Inject

class AddRecipeReferenceUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(reference: PostRecipeReference) = 
        feedRepository.addRecipeReference(reference)
}
