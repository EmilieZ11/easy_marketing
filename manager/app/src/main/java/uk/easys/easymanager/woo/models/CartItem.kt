package uk.easys.easymanager.woo.models

import java.util.ArrayList

class CartItem {
    lateinit var product: Product
    lateinit var options: ArrayList<Option>
    var qty: Int = 0
}
