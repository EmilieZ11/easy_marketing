package uk.easys.easydelivery.woo.data.api

import retrofit2.Call
import retrofit2.http.*
import uk.easys.easydelivery.woo.models.LineItem
import uk.easys.easydelivery.woo.models.filters.CartFilter

interface CartAPI {

    @Headers("Content-Type: application/json")
    @POST("clear")
    fun clear(): Call<String>

    @GET("count-items")
    fun count(): Call<Int>

    @GET("cart")
    fun list(): Call<Map<String, LineItem>>

    @Headers("Content-Type: application/json")
    @POST("cart/add")
    fun addToCart(@Body body: LineItem): Call<Map<String, LineItem>>

    @DELETE("cart/cart-item")
    fun delete(@Body body: CartFilter): Call<String>

    @GET("cart/cart-item")
    fun restore(@Body body: CartFilter): Call<String>

    @Headers("Content-Type: application/json")
    @POST("cart/cart-item")
    fun update(@Body body: CartFilter): Call<String>


}