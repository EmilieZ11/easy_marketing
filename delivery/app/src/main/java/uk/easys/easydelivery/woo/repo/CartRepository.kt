package uk.easys.easydelivery.woo.repo

import uk.easys.easydelivery.woo.cocart.repo.CoCartRepository

class CartRepository(internal var baseUrl: String, consumerKey: String, consumerSecret: String) {

    private var cartRepository: CoCartRepository = CoCartRepository(baseUrl, consumerKey, consumerSecret)

    fun addToCart(productId: Int, quantity: Int) = cartRepository.addToCart(productId, quantity)

    fun cart(customerId: String) = cartRepository.getCustomerCart(customerId = customerId)
}
