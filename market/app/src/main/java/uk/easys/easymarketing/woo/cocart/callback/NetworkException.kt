package uk.easys.easymarketing.woo.cocart.callback

class NetworkException : Exception {

    constructor() : super() {}

    constructor(message: String) : super(message) {}

    constructor(message: String, cause: Throwable) : super(message, cause) {}

    constructor(cause: Throwable) : super(cause) {}
}
