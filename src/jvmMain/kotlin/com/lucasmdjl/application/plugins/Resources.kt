package com.lucasmdjl.application.plugins

import io.ktor.server.application.*
import io.ktor.server.resources.*

fun Application.installResources() {
    install(Resources)
}