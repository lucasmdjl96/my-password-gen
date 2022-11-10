package com.mypasswordgen.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*

fun HttpClientConfig<*>.installDefaultRequest() {
    install(DefaultRequest) {
        url { protocol = URLProtocol.HTTPS }
    }
}
