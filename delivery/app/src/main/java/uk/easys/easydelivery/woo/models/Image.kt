package uk.easys.easydelivery.woo.models

import java.io.Serializable

class Image : Serializable {
    var id: Int = 0
    var date_created: String? = null
    var date_created_gmt: String? = null
    var date_modified: String? = null
    var date_modified_gmt: String? = null
    var src: String? = null
    var name: String? = null
    var alt: String? = null
    var position: Int = 0
}
