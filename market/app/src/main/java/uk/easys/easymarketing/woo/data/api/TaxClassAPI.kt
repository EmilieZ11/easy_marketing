package uk.easys.easymarketing.woo.data.api

import retrofit2.Call
import retrofit2.http.*
import uk.easys.easymarketing.woo.models.TaxClass

import java.util.ArrayList

interface TaxClassAPI {

    @Headers("Content-Type: application/json")
    @POST("taxes/classes")
    fun create(@Body body: TaxClass): Call<TaxClass>

    @GET("taxes/classes")
    fun list(): Call<List<TaxClass>>

    @DELETE("taxes/classes/{id}")
    fun delete(@Path("id") id: Int): Call<TaxClass>

    @DELETE("taxes/classes/{id}")
    fun delete(@Path("id") id: Int, @Query("force") force: Boolean): Call<TaxClass>

    @GET("coupons")
    fun filter(@QueryMap filter: Map<String, String>): Call<ArrayList<TaxClass>>

}