package com.mypasswordgen.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.resources.*

fun HttpClientConfig<*>.installResources() {
    install(Resources)
}
