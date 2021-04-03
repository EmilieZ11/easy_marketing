package uk.easys.easymanager.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easymanager.woo.models.ProductReview

import java.util.ArrayList

class ReviewsCallback {
    @SerializedName("product_reviews")
    lateinit var productReviews: ArrayList<ProductReview>
}
