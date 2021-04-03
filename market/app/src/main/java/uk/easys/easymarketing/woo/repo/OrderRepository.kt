package uk.easys.easymarketing.woo.repo

import retrofit2.Call
import uk.easys.easymarketing.woo.data.api.OrderAPI
import uk.easys.easymarketing.woo.models.LineItem
import uk.easys.easymarketing.woo.models.Order
import uk.easys.easymarketing.woo.models.OrderNote
import uk.easys.easymarketing.woo.models.filters.OrderFilter
import uk.easys.easymarketing.woo.repo.order.OrderNoteRepository
import uk.easys.easymarketing.woo.repo.order.RefundRepository

class OrderRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: OrderAPI

    internal var orderNoteRepository: OrderNoteRepository
    internal var refundRepository: RefundRepository

    init {
        apiService = retrofit.create(OrderAPI::class.java)

        orderNoteRepository = OrderNoteRepository(baseUrl, consumerKey, consumerSecret)
        refundRepository = RefundRepository(baseUrl, consumerKey, consumerSecret)
    }

    fun create(order: Order) = apiService.create(order)

    fun addToCart(productId: Int, cartOrder: Order?): Call<Order> {
        var cartOrder = cartOrder
        val lineItem = LineItem()
        lineItem.productId = productId
        lineItem.quantity = 1

        if (cartOrder != null) {
            cartOrder.addLineItem(lineItem)
            return apiService.update(cartOrder.id, cartOrder)
        } else {
            cartOrder = Order()
            cartOrder.orderNumber = "Cart"
            cartOrder.status = "on-hold"
            cartOrder.addLineItem(lineItem)
            return apiService.create(cartOrder)
        }
    }

    fun cart(): Call<List<Order>> {
        val orderFilter = OrderFilter()
        orderFilter.status = "on-hold"

        return apiService.filter(orderFilter.filters)
    }

    fun order(id: Int) = apiService.view(id)

    fun orders() = apiService.list()

    fun orders(orderFilter: OrderFilter) = apiService.filter(orderFilter.filters)

    fun update(id: Int, order: Order) = apiService.update(id, order)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)


    fun createNote(order: Order, note: OrderNote) = orderNoteRepository.create(order, note)

    fun note(order: Order, id: Int) = orderNoteRepository.note(order, id)

    fun notes(order: Order) = orderNoteRepository.notes(order)

    fun deleteNote(order: Order, id: Int) = orderNoteRepository.delete(order, id)

    fun deleteNote(order: Order, id: Int, force: Boolean) =
        orderNoteRepository.delete(order, id, force)
}
