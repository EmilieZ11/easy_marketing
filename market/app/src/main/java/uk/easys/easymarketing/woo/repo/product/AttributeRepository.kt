package uk.easys.easymarketing.woo.repo.product

import uk.easys.easymarketing.woo.data.api.ProductAttributeAPI
import uk.easys.easymarketing.woo.models.ProductAttribute
import uk.easys.easymarketing.woo.models.filters.ProductAttributeFilter
import uk.easys.easymarketing.woo.repo.WooRepository

class AttributeRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductAttributeAPI = retrofit.create(ProductAttributeAPI::class.java)

    fun create(productAttribute: ProductAttribute) = apiService.create(productAttribute)


    fun attribute(id: Int) = apiService.view(id)

    fun attributes() = apiService.list()

    fun attributes(productAttributeFilter: ProductAttributeFilter) =
        apiService.filter(productAttributeFilter.filters)

    fun update(id: Int, productAttribute: ProductAttribute) =
        apiService.update(id, productAttribute)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
