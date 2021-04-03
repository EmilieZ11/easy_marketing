package uk.easys.easymanager.woo.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import uk.easys.easymanager.woo.models.ShippingMethod

interface ShippingMethodAPI {

    @GET("shipping_methods/{id}")
    fun view(@Path("id") id: String): Call<ShippingMethod>

    @GET("shipping_methods")
    fun list(): Call<List<ShippingMethod>>

}