package uk.easys.easymarketing.woo.data.callbacks

import uk.easys.easymarketing.woo.models.Product
import java.util.ArrayList

class ProductCallback {
    var products = ArrayList<Product>()
    lateinit var product: Product
}
