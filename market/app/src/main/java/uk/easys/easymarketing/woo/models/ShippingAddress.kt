package uk.easys.easymarketing.woo.models

import com.google.gson.annotations.SerializedName

class ShippingAddress {
    var id: Int = 0
    @SerializedName("first_name")
    lateinit var firstName: String
    @SerializedName("last_name")
    lateinit var lastName: String
    lateinit var company: String
    @SerializedName("address_1")
    lateinit var address1: String
    @SerializedName("address_2")
    lateinit var address2: String
    lateinit var city: String
    lateinit var state: String
    lateinit var postcode: String
    lateinit var country: String

    override fun toString() = (firstName + " " + lastName + "\n" +
            address1 + " " + address2 + "\n"
            + city + ", " + state + " " + postcode + "\n"
            + country)
}
