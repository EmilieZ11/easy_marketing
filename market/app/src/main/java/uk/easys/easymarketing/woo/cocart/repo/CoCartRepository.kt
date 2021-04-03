package uk.easys.easymarketing.woo.cocart.repo

import me.gilo.cocart.model.CartItem
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.easys.easymarketing.woo.cocart.data.api.ItemsAPI
import uk.easys.easymarketing.woo.cocart.data.requests.CartItemRequest
import uk.easys.easymarketing.woo.cocart.data.requests.CartRequest
import java.util.concurrent.TimeUnit

class CoCartRepository(private var baseUrl: String, consumerKey: String, consumerSecret: String) {

    private var apiService: ItemsAPI
    private var retrofit: Retrofit

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(ItemsAPI::class.java)
    }

    fun addToCart(productId: Int, quantity: Int): Call<CartItem> {
        val cartItemRequest = CartItemRequest(productId = productId, quantity = quantity)

        return apiService.addToCart(cartItemRequest)
    }

    fun cart() = apiService.list()

    fun cart(customerId: Int, thumb: Boolean = false) = apiService.list(thumb)

    fun getCustomerCart(customerId: String, thumb: Boolean = false) =
        apiService.getCustomerCart(CartRequest(customerId = customerId, thumb = true))
}
