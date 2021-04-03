package uk.easys.easymanager.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easymanager.woo.models.Category

import java.util.ArrayList

class CategoriesCallback {
    @SerializedName("product_categories")
    lateinit var categories: ArrayList<Category>
}
