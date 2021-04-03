package uk.easys.easymarketing.woo.repo.order

import uk.easys.easymarketing.woo.data.api.RefundAPI
import uk.easys.easymarketing.woo.models.Order
import uk.easys.easymarketing.woo.models.Refund
import uk.easys.easymarketing.woo.models.filters.RefundFilter
import uk.easys.easymarketing.woo.repo.WooRepository

class RefundRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: RefundAPI = retrofit.create(RefundAPI::class.java)

    fun create(order: Order, refund: Refund) = apiService.create(order.id, refund)

    fun refund(order: Order, id: Int) = apiService.view(order.id, id)

    fun refunds(order: Order) = apiService.list(order.id)

    fun refunds(order: Order, refundFilter: RefundFilter) = apiService.filter(order.id, refundFilter.filters)

    fun delete(order: Order, id: Int) = apiService.delete(order.id, id)

    fun delete(order: Order, id: Int, force: Boolean) = apiService.delete(order.id, id, force)
}
