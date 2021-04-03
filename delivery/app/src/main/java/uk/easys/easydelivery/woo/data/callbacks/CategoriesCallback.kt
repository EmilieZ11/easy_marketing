package uk.easys.easydelivery.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easydelivery.woo.models.Category

import java.util.ArrayList

class CategoriesCallback {
    @SerializedName("product_categories")
    lateinit var categories: ArrayList<Category>
}
