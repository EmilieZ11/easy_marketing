package uk.easys.easymarketing.woo.models.filters

enum class Sort {
    ASCENDING {
        override fun toString() = "asc"
    },
    DESCENDING {
        override fun toString() = "desc"
    }
}
