package uk.easys.easymarketing.woo.data.api

import retrofit2.Call
import retrofit2.http.*
import uk.easys.easymarketing.woo.models.TaxRate

import java.util.ArrayList

interface TaxRateAPI {

    @Headers("Content-Type: application/json")
    @POST("taxes")
    fun create(@Body body: TaxRate): Call<TaxRate>

    @GET("taxes/{id}")
    fun view(@Path("id") id: Int): Call<TaxRate>

    @GET("taxes")
    fun list(): Call<List<TaxRate>>

    @Headers("Content-Type: application/json")
    @PUT("taxes/{id}")
    fun update(@Path("id") id: Int, @Body body: TaxRate): Call<TaxRate>

    @DELETE("taxes/{id}")
    fun delete(@Path("id") id: Int): Call<TaxRate>

    @DELETE("taxes/{id}")
    fun delete(@Path("id") id: Int, @Query("force") force: Boolean): Call<TaxRate>

    @POST("taxes/batch")
    fun batch(@Body body: TaxRate): Call<String>

    @GET("coupons")
    fun filter(@QueryMap filter: Map<String, String>): Call<ArrayList<TaxRate>>

}