package com.mypasswordgen.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

fun HttpClientConfig<*>.installContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}
