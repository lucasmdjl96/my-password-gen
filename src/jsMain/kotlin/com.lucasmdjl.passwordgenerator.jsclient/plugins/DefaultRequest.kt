package com.lucasmdjl.passwordgenerator.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*

fun HttpClientConfig<*>.installDefaultRequest() {
    install(DefaultRequest) {
        host = "localhost"
        port = 8443
        url { protocol = URLProtocol.HTTPS }
    }
}
