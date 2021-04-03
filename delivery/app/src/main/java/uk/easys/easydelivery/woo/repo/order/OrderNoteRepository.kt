package uk.easys.easydelivery.woo.repo.order

import uk.easys.easydelivery.woo.data.api.OrderNoteAPI
import uk.easys.easydelivery.woo.models.Order
import uk.easys.easydelivery.woo.models.OrderNote
import uk.easys.easydelivery.woo.models.filters.OrderNoteFilter
import uk.easys.easydelivery.woo.repo.WooRepository

class OrderNoteRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: OrderNoteAPI = retrofit.create(OrderNoteAPI::class.java)

    fun create(order: Order, note: OrderNote) = apiService.create(order.id, note)

    fun note(order: Order, id: Int) = apiService.view(order.id, id)

    fun notes(order: Order) = apiService.list(order.id)

    fun notes(order: Order, orderNoteFilter: OrderNoteFilter) =
        apiService.filter(order.id, orderNoteFilter.filters)

    fun delete(order: Order, id: Int) = apiService.delete(order.id, id)

    fun delete(order: Order, id: Int, force: Boolean) = apiService.delete(order.id, id, force)
}
