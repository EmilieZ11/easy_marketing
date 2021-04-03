package uk.easys.easymanager.woo.repo

import uk.easys.easymanager.woo.data.api.CustomerAPI
import uk.easys.easymanager.woo.models.Customer
import uk.easys.easymanager.woo.models.filters.CustomerFilter

class CustomerRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: CustomerAPI

    init {
        apiService = retrofit.create(CustomerAPI::class.java)
    }

    fun create(customer: Customer) = apiService.create(customer)


    fun customer(id: Int) = apiService.view(id)

    fun customers() = apiService.list()

    fun customers(customerFilter: CustomerFilter) = apiService.filter(customerFilter.filters)

    fun update(id: Int, customer: Customer) = apiService.update(id, customer)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
