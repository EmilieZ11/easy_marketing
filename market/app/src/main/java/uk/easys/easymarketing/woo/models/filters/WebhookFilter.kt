package uk.easys.easymarketing.woo.models.filters

class WebhookFilter : ListFilter() {

    internal lateinit var status: String

    fun getStatus() = status

    fun setStatus(status: String) {
        this.status = status
        addFilter("status", status)
    }
}
