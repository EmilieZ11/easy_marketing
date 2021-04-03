package uk.easys.easymarketing.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easymarketing.woo.models.Category

import java.util.ArrayList

class CategoriesCallback {
    @SerializedName("product_categories")
    lateinit var categories: ArrayList<Category>
}
