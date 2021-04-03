package uk.easys.easymanager.woo.repo

import retrofit2.Call
import uk.easys.easymanager.woo.data.api.ProductAPI
import uk.easys.easymanager.woo.models.Product
import uk.easys.easymanager.woo.models.filters.ProductFilter

import java.util.HashMap

class ProductRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ProductAPI

    init {
        apiService = retrofit.create(ProductAPI::class.java)
    }

    fun create(product: Product) = apiService.create(product)


    fun product(id: Int) = apiService.view(id)

    fun products() = apiService.list()

    fun filter(filters: Map<String, String>) = apiService.filter(filters)

    fun products(productFilter: ProductFilter) = filter(productFilter.filters)

    fun search(term: String): Call<List<Product>> {
        val productFilter = ProductFilter()
        productFilter.search = term

        return filter(productFilter.filters)
    }

    fun products(page: Int, per_page: Int): Call<List<Product>> {
        val productFilter = ProductFilter()
        productFilter.page = page
        productFilter.per_page = per_page

        return filter(productFilter.filters)
    }

    fun products(page: Int): Call<List<Product>> {
        val productFilter = ProductFilter()
        productFilter.page = page

        return filter(productFilter.filters)
    }

    fun update(id: Int, product: Product) = apiService.update(id, product)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)

    fun products(filters: HashMap<String, String>) = apiService.filter(filters)
}
