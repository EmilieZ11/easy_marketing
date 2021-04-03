package uk.easys.easymarketing.woo.repo

import uk.easys.easymarketing.woo.data.api.PaymentGatewayAPI
import uk.easys.easymarketing.woo.models.PaymentGateway

class PaymentGatewayRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: PaymentGatewayAPI

    init {
        apiService = retrofit.create(PaymentGatewayAPI::class.java)
    }

    fun paymentGateway(id: Int) = apiService.view(id)

    fun paymentGateways() = apiService.list()

    fun update(id: String, paymentGateway: PaymentGateway) = apiService.update(id, paymentGateway)
}
