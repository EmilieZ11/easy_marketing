package uk.easys.easymarketing.woo.data

enum class ApiVersion {
    API_VERSION1 {
        override fun toString(): String {
            return "1"
        }
    },
    API_VERSION2 {
        override fun toString(): String {
            return "2"
        }
    },
    API_VERSION3 {
        override fun toString(): String {
            return "3"
        }
    }
}