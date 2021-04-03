package uk.easys.easydelivery.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easydelivery.woo.models.Store

class StoreCallback {
    @SerializedName("store")
    lateinit var store: Store
}
