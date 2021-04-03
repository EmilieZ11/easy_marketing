package uk.easys.easymarketing.woo.repo.product

import uk.easys.easymarketing.woo.data.api.ProductReviewAPI
import uk.easys.easymarketing.woo.models.ProductReview
import uk.easys.easymarketing.woo.models.filters.ProductReviewFilter
import uk.easys.easymarketing.woo.repo.WooRepository

class ReviewRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductReviewAPI

    init {
        apiService = retrofit.create(ProductReviewAPI::class.java)
    }

    fun create(review: ProductReview) = apiService.create(review)


    fun review(id: Int) = apiService.view(id)

    fun reviews() = apiService.list()

    fun reviews(productReviewFilter: ProductReviewFilter) = apiService.filter(productReviewFilter.filters)

    fun update(id: Int, review: ProductReview) = apiService.update(id, review)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
