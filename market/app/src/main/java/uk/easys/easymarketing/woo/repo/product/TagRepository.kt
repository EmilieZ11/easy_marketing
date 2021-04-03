package uk.easys.easymarketing.woo.repo.product

import uk.easys.easymarketing.woo.data.api.ProductTagAPI
import uk.easys.easymarketing.woo.models.Tag
import uk.easys.easymarketing.woo.models.filters.ProductTagFilter
import uk.easys.easymarketing.woo.repo.WooRepository

class TagRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductTagAPI

    init {
        apiService = retrofit.create(ProductTagAPI::class.java)
    }

    fun create(tag: Tag) = apiService.create(tag)


    fun tag(id: Int) = apiService.view(id)

    fun tags() = apiService.list()

    fun tags(productTagFilter: ProductTagFilter) = apiService.filter(productTagFilter.filters)

    fun update(id: Int, tag: Tag) = apiService.update(id, tag)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
