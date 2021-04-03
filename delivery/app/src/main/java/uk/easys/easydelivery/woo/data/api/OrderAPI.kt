package uk.easys.easydelivery.woo.data.api

import retrofit2.Call
import retrofit2.http.*
import uk.easys.easydelivery.woo.models.Order

interface OrderAPI {

    @Headers("Content-Type: application/json")
    @POST("orders")
    fun create(@Body body: Order): Call<Order>

    @GET("orders/{id}")
    fun view(@Path("id") id: Int): Call<Order>

    @GET("orders")
    fun list(): Call<List<Order>>

    @Headers("Content-Type: application/json")
    @PUT("orders/{id}")
    fun update(@Path("id") id: Int, @Body body: Order): Call<Order>

    @DELETE("orders/{id}")
    fun delete(@Path("id") id: Int): Call<Order>

    @DELETE("orders/{id}")
    fun delete(@Path("id") id: Int, @Query("force") force: Boolean): Call<Order>

    @POST("orders/batch")
    fun batch(@Body body: Order): Call<String>

    @GET("orders")
    fun filter(@QueryMap filter: Map<String, String>): Call<List<Order>>

}