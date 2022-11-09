package com.mypasswordgen.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.logging.*

fun HttpClientConfig<*>.installLogging() {
    install(Logging) {
        level = LogLevel.ALL
    }
}
