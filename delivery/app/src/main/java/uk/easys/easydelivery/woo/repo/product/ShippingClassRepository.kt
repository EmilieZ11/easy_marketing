package uk.easys.easydelivery.woo.repo.product

import uk.easys.easydelivery.woo.data.api.ShippingClassAPI
import uk.easys.easydelivery.woo.models.ShippingClass
import uk.easys.easydelivery.woo.models.filters.ShippingClassesFilter
import uk.easys.easydelivery.woo.repo.WooRepository

class ShippingClassRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ShippingClassAPI

    init {
        apiService = retrofit.create(ShippingClassAPI::class.java)
    }

    fun create(shippingClass: ShippingClass) = apiService.create(shippingClass)


    fun shippingClass(id: Int) = apiService.view(id)

    fun shippingClasses() = apiService.list()

    fun shippingClasses(shippingClassesFilter: ShippingClassesFilter) = apiService.filter(shippingClassesFilter.filters)

    fun update(id: Int, shippingClass: ShippingClass) = apiService.update(id, shippingClass)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
