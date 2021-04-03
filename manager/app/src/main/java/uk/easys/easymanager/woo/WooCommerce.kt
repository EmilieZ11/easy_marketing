package uk.easys.easymanager.woo

import android.content.Context
import uk.easys.easymanager.woo.data.ApiVersion
import uk.easys.easymanager.woo.repo.*
import uk.easys.easymanager.woo.repo.order.OrderNoteRepository
import uk.easys.easymanager.woo.repo.order.RefundRepository
import uk.easys.easymanager.woo.repo.product.*

class WooCommerce(
    siteUrl: String,
    apiVerion: ApiVersion,
    consumerKey: String,
    consumerSecret: String
) {
    companion object {
        val API_V1 = ApiVersion.API_VERSION1
        val API_V2 = ApiVersion.API_VERSION2
        val API_V3 = ApiVersion.API_VERSION3
    }

    private val orderNoteRepository: OrderNoteRepository
    private val refundRepository: RefundRepository
    private val attributeRepository: AttributeRepository
    private val attributeTermRepository: AttributeTermRepository
    private val categoryRepository: CategoryRepository
    private val shippingClassRepository: ShippingClassRepository
    private val tagRepository: TagRepository
    private val variationRepository: VariationRepository
    private val couponRepository: CouponRepository
    private val customerRepository: CustomerRepository
    private val orderRepository: OrderRepository
    private val productRepository: ProductRepository
    private val reviewRepository: ReviewRepository
    private val reportsRepository: ReportsRepository
    private val cartRepository: CartRepository
    private val paymentGatewayRepository: PaymentGatewayRepository
    private val settingsRepository: SettingsRepository
    private val shippingMethodRepository: ShippingMethodRepository


    init {
        val baseUrl = "$siteUrl/wp-json/wc/v$apiVerion/"
        val cartBaseUrl = "$siteUrl/wp-json/cocart/v1/"

        orderNoteRepository = OrderNoteRepository(baseUrl, consumerKey, consumerSecret)
        refundRepository = RefundRepository(baseUrl, consumerKey, consumerSecret)
        attributeRepository = AttributeRepository(baseUrl, consumerKey, consumerSecret)
        attributeTermRepository = AttributeTermRepository(baseUrl, consumerKey, consumerSecret)
        categoryRepository = CategoryRepository(baseUrl, consumerKey, consumerSecret)
        shippingClassRepository = ShippingClassRepository(baseUrl, consumerKey, consumerSecret)
        tagRepository = TagRepository(baseUrl, consumerKey, consumerSecret)
        variationRepository = VariationRepository(baseUrl, consumerKey, consumerSecret)
        couponRepository = CouponRepository(baseUrl, consumerKey, consumerSecret)
        customerRepository = CustomerRepository(baseUrl, consumerKey, consumerSecret)
        orderRepository = OrderRepository(baseUrl, consumerKey, consumerSecret)
        productRepository = ProductRepository(baseUrl, consumerKey, consumerSecret)
        reportsRepository = ReportsRepository(baseUrl, consumerKey, consumerSecret)
        cartRepository = CartRepository(cartBaseUrl, consumerKey, consumerSecret)
        reviewRepository = ReviewRepository(baseUrl, consumerKey, consumerSecret)
        paymentGatewayRepository = PaymentGatewayRepository(baseUrl, consumerKey, consumerSecret)
        settingsRepository = SettingsRepository(baseUrl, consumerKey, consumerSecret)
        shippingMethodRepository = ShippingMethodRepository(baseUrl, consumerKey, consumerSecret)

    }

    fun OrderNoteRepository() = orderNoteRepository

    fun RefundRepository() = refundRepository

    fun AttributeRepository() = attributeRepository

    fun AttributeTermRepository() = attributeTermRepository

    fun CategoryRepository() = categoryRepository

    fun ShippingClassRepository() = shippingClassRepository

    fun TagRepository() = tagRepository

    fun VariationRepository() = variationRepository

    fun CouponRepository() = couponRepository

    fun CustomerRepository() = customerRepository

    fun OrderRepository() = orderRepository

    fun ProductRepository() = productRepository

    fun ReviewRepository() = reviewRepository

    fun ReportsRepository() = reportsRepository

    fun PaymentGatewayRepository() = paymentGatewayRepository

    fun SettingsRepository() = settingsRepository

    fun ShippingMethodRepository() = shippingMethodRepository

    fun CartRepository(context: Context) = cartRepository

    class Builder {
        private lateinit var siteUrl: String
        private lateinit var apiVersion: ApiVersion
        private lateinit var consumerKey: String
        private lateinit var consumerSecret: String

        fun setSiteUrl(siteUrl: String): Builder {
            this.siteUrl = siteUrl
            return this
        }

        fun setApiVersion(apiVerion: ApiVersion): Builder {
            this.apiVersion = apiVerion
            return this
        }

        fun setConsumerKey(consumerKey: String): Builder {
            this.consumerKey = consumerKey
            return this
        }

        fun setConsumerSecret(consumerSecret: String): Builder {
            this.consumerSecret = consumerSecret
            return this
        }

        fun build() = WooCommerce(siteUrl, apiVersion, consumerKey, consumerSecret)
    }
}
