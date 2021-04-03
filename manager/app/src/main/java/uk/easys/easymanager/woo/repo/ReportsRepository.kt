package uk.easys.easymanager.woo.repo

import uk.easys.easymanager.woo.data.api.ReportAPI
import uk.easys.easymanager.woo.models.filters.ReportsDateFilter

class ReportsRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: ReportAPI

    init {
        apiService = retrofit.create(ReportAPI::class.java)
    }

    fun sales() = apiService.sales()

    fun sales(reportsDateFilter: ReportsDateFilter) = apiService.sales(reportsDateFilter.filters)

    fun top_sellers() = apiService.top_sellers()

    fun top_sellers(reportsDateFilter: ReportsDateFilter) = apiService.top_sellers(reportsDateFilter.filters)

    fun coupons_totals() = apiService.coupons_totals()

    fun customer_totals() = apiService.customers_totals()

    fun order_totals() = apiService.orders_totals()

    fun product_totals() = apiService.products_totals()

    fun review_totals() = apiService.reviews_totals()
}
