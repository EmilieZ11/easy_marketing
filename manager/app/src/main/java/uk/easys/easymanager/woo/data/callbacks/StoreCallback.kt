package uk.easys.easymanager.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easymanager.woo.models.Store

class StoreCallback {
    @SerializedName("store")
    lateinit var store: Store
}
