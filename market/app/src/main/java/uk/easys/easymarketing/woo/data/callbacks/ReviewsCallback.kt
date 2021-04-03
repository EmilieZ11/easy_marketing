package uk.easys.easymarketing.woo.data.callbacks

import com.google.gson.annotations.SerializedName
import uk.easys.easymarketing.woo.models.ProductReview

import java.util.ArrayList

class ReviewsCallback {
    @SerializedName("product_reviews")
    lateinit var productReviews: ArrayList<ProductReview>
}
