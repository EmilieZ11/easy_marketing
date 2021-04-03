package uk.easys.easydelivery.woo.repo

import uk.easys.easydelivery.woo.data.api.CouponAPI
import uk.easys.easydelivery.woo.models.Coupon
import uk.easys.easydelivery.woo.models.filters.CouponFilter

class CouponRepository(baseUrl: String, consumerKey: String, consumerSecret: String) :
    WooRepository(baseUrl, consumerKey, consumerSecret) {

    private val apiService: CouponAPI

    init {
        apiService = retrofit.create(CouponAPI::class.java)
    }

    fun create(coupon: Coupon) = apiService.create(coupon)


    fun coupon(id: Int) = apiService.view(id)

    fun coupons() = apiService.list()

    fun coupons(couponFilter: CouponFilter) = apiService.filter(couponFilter.filters)

    fun update(id: Int, coupon: Coupon) = apiService.update(id, coupon)

    fun delete(id: Int) = apiService.delete(id)

    fun delete(id: Int, force: Boolean) = apiService.delete(id, force)
}
