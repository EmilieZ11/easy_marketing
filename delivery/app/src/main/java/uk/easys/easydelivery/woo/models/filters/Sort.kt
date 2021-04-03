package uk.easys.easydelivery.woo.models.filters

enum class Sort {
    ASCENDING {
        override fun toString() = "asc"
    },
    DESCENDING {
        override fun toString() = "desc"
    }
}
