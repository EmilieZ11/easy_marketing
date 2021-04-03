package uk.easys.easymanager.woo.models.filters

enum class Role {
    ALL {
        override fun toString() = "all"
    },
    ADMINISTRATOR {
        override fun toString() = "administrator"
    },
    EDITOR {
        override fun toString() = "editor"
    },
    AUTHOR {
        override fun toString() = "author"
    },
    CONTRIBUTOR {
        override fun toString() = "contributor"
    },
    SUBSCRIBER {
        override fun toString() = "subscriber"
    },
    CUSTOMER {
        override fun toString() = "customer"
    },
    SHOP_MANAGER {
        override fun toString() = "shop_manager"
    },
    DELIVERY_DRIVER {
        override fun toString() = "delivery_driver"
    }
}
