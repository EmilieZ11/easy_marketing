package uk.easys.easydelivery.woo.repo.product

import uk.easys.easydelivery.woo.data.api.ProductCategoryAPI
import uk.easys.easydelivery.woo.models.Category
import uk.easys.easydelivery.woo.models.filters.ProductCategoryFilter
import uk.easys.easydelivery.woo.repo.WooRepository

class CategoryRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductCategoryAPI

    init {
        apiService = retrofit.create(ProductCategoryAPI::class.java)
    }

    fun create(category: Category) = apiService.create(category)


    fun category(id: Int) = apiService.view(id)

    fun categories() = apiService.list()

    fun categories(productCategoryFilter: ProductCategoryFilter) =
        apiService.filter(productCategoryFilter.filters)

    fun update(id: Int, category: Category) = apiService.update(id, category)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
