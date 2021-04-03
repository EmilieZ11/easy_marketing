package uk.easys.easydelivery.woo.repo.product

import uk.easys.easydelivery.woo.data.api.ProductVariationAPI
import uk.easys.easydelivery.woo.models.Variation
import uk.easys.easydelivery.woo.models.filters.ProductVariationFilter
import uk.easys.easydelivery.woo.repo.WooRepository

class VariationRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductVariationAPI

    init {
        apiService = retrofit.create(ProductVariationAPI::class.java)
    }

    fun create(product_id: Int, variation: Variation) = apiService.create(product_id, variation)


    fun variation(product_id: Int, id: Int) = apiService.view(product_id, id)

    fun variations(product_id: Int) = apiService.list(product_id)

    fun variations(product_id: Int, productVariationFilter: ProductVariationFilter) = apiService.filter(product_id, productVariationFilter.filters)

    fun update(product_id: Int, id: Int, variation: Variation) = apiService.update(product_id, id, variation)

    fun delete(product_id: Int, id: Int) = apiService.delete(product_id, id)

    fun delete(product_id: Int, id: Int, force: Boolean) = apiService.delete(product_id, id, force)
}
