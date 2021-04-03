package uk.easys.easymanager.woo.models

import java.io.Serializable

class Download : Serializable {
    lateinit var id: String
    lateinit var name: String
    lateinit var file: String
}
