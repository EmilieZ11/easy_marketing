package uk.easys.easymanager.woo.models

class Store {
    var wc_version: String? = null
    var description: String? = null
    var name: String? = null
    var url: String? = null
    var meta: Meta? = null

    var version: String? = null

    override fun toString() = "ClassPojo [wc_version = $wc_version, description = $description, name = $name, URL = $url, meta = $meta, version = $version]"
}
