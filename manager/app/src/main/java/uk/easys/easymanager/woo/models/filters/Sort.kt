package uk.easys.easymanager.woo.models.filters

enum class Sort {
    ASCENDING {
        override fun toString() = "asc"
    },
    DESCENDING {
        override fun toString() = "desc"
    }
}
