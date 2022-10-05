package com.lucasmdjl.passwordgenerator.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.resources.*

fun HttpClientConfig<*>.installResources() {
    install(Resources)
}
