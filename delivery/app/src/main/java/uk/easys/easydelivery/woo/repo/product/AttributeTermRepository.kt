package uk.easys.easydelivery.woo.repo.product

import uk.easys.easydelivery.woo.data.api.ProductAttributeTermAPI
import uk.easys.easydelivery.woo.models.AttributeTerm
import uk.easys.easydelivery.woo.models.filters.ProductAttributeFilter
import uk.easys.easydelivery.woo.repo.WooRepository

class AttributeTermRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductAttributeTermAPI = retrofit.create(ProductAttributeTermAPI::class.java)

    fun create(attribute_id: Int, term: AttributeTerm) = apiService.create(attribute_id, term)

    fun term(attribute_id: Int, id: Int) = apiService.view(attribute_id, id)

    fun terms(attribute_id: Int) = apiService.list(attribute_id)

    fun terms(attribute_id: Int, productAttributeFilter: ProductAttributeFilter) = apiService.filter(attribute_id, productAttributeFilter.filters)

    fun update(attribute_id: Int, id: Int, term: AttributeTerm) = apiService.update(attribute_id, id, term)

    fun delete(attribute_id: Int, id: Int) = apiService.delete(attribute_id, id)

    fun delete(attribute_id: Int, id: Int, force: Boolean) = apiService.delete(attribute_id, id, force)
}
