package uk.easys.easydelivery.woo.data.callbacks

import uk.easys.easydelivery.woo.models.Product
import java.util.ArrayList

class ProductCallback {
    var products = ArrayList<Product>()
    lateinit var product: Product
}
