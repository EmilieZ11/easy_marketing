package uk.easys.easymanager.woo.models

import com.google.gson.annotations.SerializedName

class ShippingLine {
    @SerializedName("method_id")
    var id: String? = null
    @SerializedName("method_title")
    var methodTitle: String? = null
    var total: Int = 0
}
