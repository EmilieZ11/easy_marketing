package uk.easys.easydelivery.woo.data.api

import retrofit2.Call
import retrofit2.http.*
import uk.easys.easydelivery.woo.models.PaymentGateway

interface PaymentGatewayAPI {

    @GET("payment_gateways/{id}")
    fun view(@Path("id") id: Int): Call<PaymentGateway>

    @GET("payment_gateways")
    fun list(): Call<List<PaymentGateway>>

    @Headers("Content-Type: application/json")
    @PUT("payment_gateways")
    fun update(@Path("id") id: String, @Body body: PaymentGateway): Call<PaymentGateway>

}