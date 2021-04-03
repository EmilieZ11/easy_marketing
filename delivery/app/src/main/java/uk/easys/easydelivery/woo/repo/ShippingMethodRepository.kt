package uk.easys.easydelivery.woo.repo

import uk.easys.easydelivery.woo.data.api.ShippingMethodAPI

class ShippingMethodRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ShippingMethodAPI

    init {
        apiService = retrofit.create(ShippingMethodAPI::class.java)
    }

    fun shippingMethod(id: String) = apiService.view(id)

    fun shippingMethods() = apiService.list()
}
