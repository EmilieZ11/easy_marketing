package uk.easys.easymanager.woo.repo

import uk.easys.easymanager.woo.data.api.ShippingMethodAPI

class ShippingMethodRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ShippingMethodAPI

    init {
        apiService = retrofit.create(ShippingMethodAPI::class.java)
    }

    fun shippingMethod(id: String) = apiService.view(id)

    fun shippingMethods() = apiService.list()
}
