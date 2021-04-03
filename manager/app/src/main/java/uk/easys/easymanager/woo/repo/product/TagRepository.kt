package uk.easys.easymanager.woo.repo.product

import uk.easys.easymanager.woo.data.api.ProductTagAPI
import uk.easys.easymanager.woo.models.Tag
import uk.easys.easymanager.woo.models.filters.ProductTagFilter
import uk.easys.easymanager.woo.repo.WooRepository

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
