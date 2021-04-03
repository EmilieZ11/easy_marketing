package uk.easys.easymarketing.woo.models.filters

class OrderNoteFilter : Filter() {

    internal lateinit var type: String

    fun getType(): String = type

    fun setType(type: String) {
        this.type = type

        addFilter("type", type)
    }
}
