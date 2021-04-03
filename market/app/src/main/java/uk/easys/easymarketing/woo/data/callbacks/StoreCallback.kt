package uk.easys.easymarketing.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easymarketing.woo.models.Store

class StoreCallback {
    @SerializedName("store")
    lateinit var store: Store
}
